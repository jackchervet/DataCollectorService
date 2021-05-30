package com.smitestats.DataCollectorService.core

import org.http4s.client.Client
import cats.effect.IO
import scala.concurrent.ExecutionContext
import cats.effect.ContextShift
import com.smitestats.DataCollectorService.config.AppConfig
import scalacache._
import scalacache.guava._
import com.smitestats.DataCollectorService.clients.SmiteApiClient
import com.smitestats.DataCollectorService.models.QueueType
import java.time.LocalDate
import com.smitestats.DataCollectorService.helpers.SignatureHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
            matchIds <- SmiteApiClient.getMatchIdsByQueue(QueueType.CONQUEST)
            _ <- IO(logger.info("MatchIds: " + matchIds.take(20)))
        } yield ()
    }

}
