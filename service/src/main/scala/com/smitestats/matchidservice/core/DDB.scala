package com.smitestats.matchidservice.core

import fs2.{Pipe, Chunk, Stream}
import cats.effect._
import io.circe.syntax._
import com.smitestats.matchidservice.models.GetMatchDetailsBatchResponse

import scala.collection.JavaConverters._
import com.smitestats.matchidservice.config.AppConfig
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest
import software.amazon.awssdk.services.dynamodb.model.PutRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DDB {
    val logger: Logger = LoggerFactory.getLogger("DDB")
    private final val DDB_MAX_BATCH_WRITE: Int = 25

    def batchWriteItems(items: List[GetMatchDetailsBatchResponse])(implicit config: AppConfig, client: DynamoDbClient): IO[Unit] = {
        Stream.emits(items)
            .chunkLimit(DDB_MAX_BATCH_WRITE)
            .evalMapChunk { chunk =>
                buildBatchWriteItemRequest(config.matchDataTableName, chunk.toList)
            }
            .evalMap { req =>
                IO(client.batchWriteItem(req)).handleErrorWith { e => 
                    IO(logger.error(s"[ERROR] Failed to write items to DDB: ${e}")) *> IO(BatchWriteItemResponse.builder().build())
                } 
            }
            .compile
            .drain
    }

    def buildBatchWriteItemRequest(tableName: String, items: List[GetMatchDetailsBatchResponse]): IO[BatchWriteItemRequest] = {
        for {
            writeRequests <- IO { items
                .filter(md => md.Match != -1L)
                .map { md => 
                    WriteRequest.builder().putRequest(    
                        PutRequest.builder().item(matchDetailTransformer(md).asJava).build()
                    ).build
                }.asJava}
            batchRequest <- IO { 
                BatchWriteItemRequest.builder()
                    .requestItems(Map(tableName -> writeRequests).asJava).build()
                }
            _ <- IO(logger.info(s"BatchRequest Items: ${batchRequest.requestItems()}"))
        } yield batchRequest
    }

    def matchDetailTransformer(matchDetail: GetMatchDetailsBatchResponse): Map[String, AttributeValue] = {
        Map(
            "matchId" -> AttributeValue.builder.s(s"${matchDetail.Match.toString}-${matchDetail.playerId}").build,
            "timestamp" -> AttributeValue.builder.n(
                LocalDateTime.parse(
                    matchDetail.Entry_Datetime, 
                    DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a")
                ).toInstant(ZoneOffset.UTC).toEpochMilli.toString
            ).build,
            "MatchData" -> AttributeValue.builder.s(matchDetail.asJson.deepDropNullValues.noSpaces).build
        )
    }
}
