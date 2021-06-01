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
import java.time.LocalDate
import com.smitestats.matchidservice.helpers.SignatureHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.amazonaws.services.sqs.AmazonSQS

object Processor {
    val logger: Logger = LoggerFactory.getLogger("Processor")

    def process(implicit 
        ec: ExecutionContext, 
        cs: ContextShift[IO], 
        config: AppConfig, 
        client: Client[IO],
        sessionCache: Cache[String],
        sqs: AmazonSQS
    ): IO[Unit] = {
        for {
            _ <- IO(logger.info("Making API call..."))
            matchIds <- SmiteApiClient.getMatchIdsByQueue(QueueType.CONQUEST)
            _ <- SQS.sendBatches(matchIds)
        } yield ()
    }

}
