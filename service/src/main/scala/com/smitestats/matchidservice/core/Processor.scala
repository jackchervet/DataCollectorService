package com.smitestats.matchidservice.core

import org.http4s.client.Client
import cats.effect.IO
import scala.concurrent.ExecutionContext
import cats.effect.ContextShift
import com.smitestats.matchidservice.config.AppConfig
import scalacache._
import scalacache.guava._
import com.smitestats.matchidservice.clients.SmiteApiClient
import com.smitestats.matchidservice.models.QueueType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.amazonaws.services.sqs.AmazonSQS
import fs2._
import com.smitestats.matchidservice.models.GetMatchDetailsBatchResponse

object Processor {
    val logger: Logger = LoggerFactory.getLogger("Processor")

    def process(implicit 
        ec: ExecutionContext, 
        cs: ContextShift[IO], 
        config: AppConfig, 
        client: Client[IO],
        sessionCache: Cache[String]
    ): IO[Unit] = {
        for {
            _ <- IO(logger.info("Getting Match Ids..."))
            matchIds <- SmiteApiClient.getMatchIdsByQueue(QueueType.CONQUEST)
            _ <- Stream.emits(matchIds)
                .chunkN(10)
                .evalMap { chunk => 
                    for {
                        _ <- IO(logger.info(s"Getting MatchDetails for Ids: ${chunk.toList}"))
                        matchDetails <- SmiteApiClient.getMatchDetailsBatch(chunk.toList).handleErrorWith { e =>
                            IO(logger.error(s"[ERROR] Failed to retrieve match details for matchIds ${chunk.toList}... ${e}")) *> IO(List(GetMatchDetailsBatchResponse()))
                        }
                    } yield ()
                }
                .compile
                .drain
        } yield ()
    }

}
