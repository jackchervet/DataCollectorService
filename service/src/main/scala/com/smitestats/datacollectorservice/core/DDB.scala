package com.smitestats.datacollectorservice.core

import fs2.{Pipe, Chunk, Stream}
import cats.effect._
import io.circe.syntax._
import scala.collection.JavaConverters._
import com.smitestats.datacollectorservice.config.AppConfig
import software.amazon.awssdk.services.dynamodb.model.{ AttributeValue, BatchWriteItemRequest, BatchWriteItemResponse, WriteRequest, PutRequest }
import org.slf4j.{ Logger, LoggerFactory }
import java.time.{ LocalDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter
import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse
import com.smitestats.datacollectorservice.helpers.{ StatHelpers, IOUtils }
import scala.util.Random
import scala.concurrent.ExecutionContext
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object DDB extends IOUtils {
    val logger: Logger = LoggerFactory.getLogger("DDB")
    private final val DDB_MAX_BATCH_WRITE: Int = 25
    private final val MAX_CONCURRENT = 20

    def batchWriteItems(items: List[List[GetMatchDetailsBatchResponse]])(implicit config: AppConfig, client: DynamoDbAsyncClient, cs: ContextShift[IO], ec: ExecutionContext): IO[Unit] = {
        Stream.emits(items)
            .evalMap { matchData => IO {
                 matchDataTransformer(matchData)
                    .chunkLimit(DDB_MAX_BATCH_WRITE)
                    .evalMapChunk { chunk =>
                        for {
                            req <- buildBatchWriteItemRequest(config.matchDataTableName, chunk.toList)
                            _ <- IO { logger.info(s"Writing to DDB: ${req}")}
                            _ <- fromJavaFuture(client.batchWriteItem(req)).handleErrorWith { e => 
                                IO(logger.error(s"[ERROR] Failed to write items to DDB: ${e}")) *> IO(BatchWriteItemResponse.builder().build())
                            } 
                        } yield ()
                    }
                }
            }
            .parJoin(MAX_CONCURRENT)
            .compile
            .drain
    }

    def buildBatchWriteItemRequest(tableName: String, items: List[Map[String, AttributeValue]]): IO[BatchWriteItemRequest] = {
        for {
            writeRequests <- IO { items
                .map { md => 
                    WriteRequest.builder().putRequest(    
                        PutRequest.builder().item(md.asJava).build()
                    ).build
                }.asJava}
            batchRequest <- IO { 
                BatchWriteItemRequest.builder()
                    .requestItems(Map(tableName -> writeRequests).asJava).build()
                }
            _ <- IO(logger.info(s"BatchRequest Items: ${batchRequest.requestItems()}"))
        } yield batchRequest
    }

    def avN(value: String): AttributeValue = AttributeValue.builder.n(value).build
    def avS(value: String): AttributeValue = AttributeValue.builder.s(value).build

    // DDB Items can't have the same partition + sort key, so add random noise to the timestamp.
    def getRandomMillis(): Long = Random.between(0, 60000)
    
    def matchDataTransformer(matchData: List[GetMatchDetailsBatchResponse]): Stream[IO, Map[String, AttributeValue]] = {
        val tuple = (matchData, StatHelpers.findMaxValues(matchData))       
        Stream.emits(tuple._1.map { playerData =>
            Map(
                "date" -> avS(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toString),
                "timestamp" -> avN(LocalDateTime.parse(playerData.Entry_Datetime, DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a")).toInstant(ZoneOffset.UTC).plusMillis(getRandomMillis).toEpochMilli.toString),
                "Match_Id" -> avS(s"${playerData.Match}"),
                "God_Name" -> avS(playerData.Reference_Name.getOrElse("na")),
                "God_Id" -> avS(s"${playerData.GodId.getOrElse(-1L)}"),
                "Role" -> avS(s"${playerData.Role.getOrElse("na")}"),
                "Item_1_Id" -> avS(s"${playerData.ItemId1.getOrElse(-1L)}"),
                "Item_1_Name" -> avS(playerData.Item_Purch_1.getOrElse("na")),
                "Item_2_Id" -> avS(s"${playerData.ItemId2.getOrElse(-1L)}"),
                "Item_2_Name" -> avS(playerData.Item_Purch_2.getOrElse("na")),
                "Item_3_Id" -> avS(s"${playerData.ItemId3.getOrElse(-1L)}"),
                "Item_3_Name" -> avS(playerData.Item_Purch_3.getOrElse("na")),
                "Item_4_Id" -> avS(s"${playerData.ItemId4.getOrElse(-1L)}"),
                "Item_4_Name" -> avS(playerData.Item_Purch_4.getOrElse("na")),
                "Item_5_Id" -> avS(s"${playerData.ItemId5.getOrElse(-1L)}"),
                "Item_5_Name" -> avS(playerData.Item_Purch_5.getOrElse("na")),
                "Item_6_Id" -> avS(s"${playerData.ItemId6.getOrElse(-1L)}"),
                "Item_6_Name" -> avS(playerData.Item_Purch_6.getOrElse("na")),
                "Active_1_Id" -> avS(s"${playerData.ActiveId1.getOrElse(-1L)}"),
                "Active_1_Name" -> avS(playerData.Item_Active_1.getOrElse("na")),
                "Active_2_Id" -> avS(s"${playerData.ActiveId2.getOrElse(-1L)}"),
                "Active_2_Name" -> avS(playerData.Item_Active_2.getOrElse("na")),
                "Active_3_Id" -> avS(s"${playerData.ActiveId3.getOrElse(-1L)}"),
                "Active_3_Name" -> avS(playerData.Item_Active_3.getOrElse("na")),
                "Active_4_Id" -> avS(s"${playerData.ActiveId4.getOrElse(-1L)}"),
                "Active_4_Name" -> avS(playerData.Item_Active_4.getOrElse("na")),
                "Player_Damage" -> avS(s"${playerData.Damage_Player.getOrElse(-1L)}"),
                "Max_Player_Damage" -> avS(s"${tuple._2("playerDamage")}"),
                "Player_Damage_Score" -> avS(s"${playerData.Damage_Player.getOrElse(0L)/tuple._2("playerDamage")}"),
                "Damage_Mitigated" -> avS(s"${playerData.Damage_Mitigated.getOrElse(-1L)}"),
                "Max_Damage_Mitigated" -> avS(s"${tuple._2("damageMitigated")}"),
                "Damage_Mitigated_Score" -> avS(s"${playerData.Damage_Mitigated.getOrElse(0L)/tuple._2("damageMitigated")}"),
                "KDA" -> avS(s"${StatHelpers.getKDA(playerData)}"),
                "Max_KDA" -> avS(s"${tuple._2("KDA")}"),
                "KDA_Score" -> avS(s"${StatHelpers.getKDA(playerData)/tuple._2("KDA")}"),
                "Gold_Earned" -> avS(s"${playerData.Gold_Earned.getOrElse(-1L)}"),
                "Max_Gold_Earned" -> avS(s"${tuple._2("goldEarned")}"),
                "Gold_Earned_Score" -> avS(s"${playerData.Gold_Earned.getOrElse(0L)/tuple._2("goldEarned")}"),
                "Structure_Damage" -> avS(s"${playerData.Structure_Damage.getOrElse(-1L)}"),
                "Max_Structure_Damage" -> avS(s"${tuple._2("structureDamage")}"),
                "Structure_Damage_Score" -> avS(s"${playerData.Structure_Damage.getOrElse(0L)/tuple._2("structureDamage")}"),
                "Auto_Attack_Damage" -> avS(s"${playerData.Damage_Done_In_Hand.getOrElse(-1L)}"),
                "Max_Auto_Attack_Damage" -> avS(s"${tuple._2("autoAttackDamage")}"),
                "Auto_Attack_Damage_Score" -> avS(s"${playerData.Damage_Done_In_Hand.getOrElse(0L)/tuple._2("autoAttackDamage")}"),
                "Magical_Damage" -> avS(s"${playerData.Damage_Done_Magical.getOrElse(-1L)}"),
                "Max_Magical_Damage" -> avS(s"${tuple._2("magicalDamage")}"),
                "Magical_Damage_Score" -> avS(s"${playerData.Damage_Done_Magical.getOrElse(0L)/tuple._2("magicalDamage")}"),
                "Physical_Damage" -> avS(s"${playerData.Damage_Done_Physical.getOrElse(-1L)}"),
                "Max_Physical_Damage" -> avS(s"${tuple._2("physicalDamage")}"),
                "Physical_Damage_Score" -> avS(s"${playerData.Damage_Done_Physical.getOrElse(0L)/tuple._2("physicalDamage")}"),
                "Win" -> avS(s"${playerData.Win_Status.map(_ match { case "Winner" => 1; case _ => 0 }).getOrElse("na")}"),
                "ttl" -> avN(s"${LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli()}")
            )
        })
    }
}
