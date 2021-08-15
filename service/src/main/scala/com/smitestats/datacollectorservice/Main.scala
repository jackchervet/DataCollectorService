package com.smitestats.datacollectorservice

import cats.effect._
import cats.syntax.all._
import scalacache._
import scalacache.guava._
import com.smitestats.datacollectorservice.config.AppConfig
import scala.concurrent.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.http4s.client.blaze.BlazeClientBuilder
import com.smitestats.datacollectorservice.clients.SmiteApiClient
import com.smitestats.datacollectorservice.helpers.SessionHelper
import com.smitestats.datacollectorservice.helpers.SignatureHelper
import com.smitestats.datacollectorservice.core.Processor
import java.io.InputStream
import java.io.OutputStream
import com.amazonaws.services.lambda.runtime.Context
import scala.io.Source

import io.circe.parser._
import io.circe.syntax._
import software.amazon.awssdk.services.s3.S3AsyncClient

class Main {

    val logger: Logger = LoggerFactory.getLogger("Main")
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ec) 
    implicit val sessionCache: Cache[String] = GuavaCache[String]
    implicit val s3: S3AsyncClient = S3AsyncClient.create()
    implicit val blocker: Blocker = Blocker.liftExecutionContext(ec)

    def run(input: InputStream, output: OutputStream, context: Context): Unit = {
        (for {
            _ <- IO { logger.info("Retrieving AppConfig...") }
            config <- AppConfig.loaded
            _ <- IO { logger.info("Beginning processing...") }
            _ <- BlazeClientBuilder[IO](ec).resource.use { client =>
                Processor.process(outputToDDB = true)(ec, cs, config, client, sessionCache, blocker, s3)
            }
        } yield ()).unsafeRunSync
    }
}