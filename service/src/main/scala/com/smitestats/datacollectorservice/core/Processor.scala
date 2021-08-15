package com.smitestats.datacollectorservice.core

import org.http4s.client.Client
import cats.effect.IO
import scala.concurrent.ExecutionContext
import cats.effect.ContextShift
import com.smitestats.datacollectorservice.config.AppConfig
import scalacache._
import scalacache.guava._
import com.smitestats.datacollectorservice.clients.SmiteApiClient
import com.smitestats.datacollectorservice.models.QueueType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import fs2.io._
import fs2.Stream
import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse
import cats.effect.Blocker
import io.circe.syntax._
import com.smitestats.datacollectorservice.helpers.SessionHelper
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

object Processor {
    val logger: Logger = LoggerFactory.getLogger("Processor")

    def process(outputToDDB: Boolean)(implicit 
        ec: ExecutionContext,  
        cs: ContextShift[IO], 
        config: AppConfig, 
        client: Client[IO],
        sessionCache: Cache[String],
        blocker: Blocker,
        ddb: DynamoDbAsyncClient
    ): IO[Unit] = {
        for {
            session <- SessionHelper.getSession
            _ <- IO(logger.info("Getting Match Ids..."))
            matchIds <- SmiteApiClient.getMatchIdsByQueue(QueueType.CONQUEST, session)
            matchDetails <- Stream.emits(matchIds)
                .chunkN(10)
                .parEvalMapUnordered[IO, List[List[GetMatchDetailsBatchResponse]]](5) { chunk => 
                    for {
                        _ <- IO(logger.info(s"Getting MatchDetails for Ids: ${chunk.toList}"))
                        matchDetails <- SmiteApiClient.getMatchDetailsBatch(chunk.toList, session).handleErrorWith { e =>
                            IO(logger.error(s"[ERROR] Failed to retrieve match details for matchIds ${chunk.toList}... ${e}")) *> IO(List(GetMatchDetailsBatchResponse()))
                        }
                    } yield matchDetails.filter(_.Match != -1L).groupBy(_.Match).values.toList
                }
                .compile
                .fold(List.empty[List[GetMatchDetailsBatchResponse]])((aggr, list) => aggr :++ list)
            _ <- 
                if (outputToDDB) DDB.batchWriteItems(matchDetails)
                else FileWriter.writeFile(matchDetails)
        } yield ()
    }
}
