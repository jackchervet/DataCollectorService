package com.smitestats.DataCollectorService

import cats.effect._
import cats.syntax.all._
import scalacache._
import scalacache.guava._
import com.smitestats.DataCollectorService.config.AppConfig
import scala.concurrent.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.http4s.client.blaze.BlazeClientBuilder
import com.smitestats.DataCollectorService.clients.SmiteApiClient
import com.smitestats.DataCollectorService.helpers.SessionHelper
import com.smitestats.DataCollectorService.helpers.SignatureHelper
import com.smitestats.DataCollectorService.core.Processor

object Main extends IOApp {

    val logger: Logger = LoggerFactory.getLogger("Main")
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ec)
    implicit val sessionCache: Cache[String] = GuavaCache[String]

    def run(args: List[String]) = {
        for {
            _ <- IO { logger.info("Retrieving AppConfig...") }
            config <- AppConfig.loaded
            _ <- IO { logger.info("Beginning processing...") }
            _ <- BlazeClientBuilder[IO](ec).resource.use { client =>
                Processor.process(ec, cs, config, client, sessionCache)
            }
        } yield ExitCode.Success     
    }
}