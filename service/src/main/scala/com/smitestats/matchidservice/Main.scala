package com.smitestats.matchidservice

import cats.effect._
import cats.syntax.all._
import scalacache._
import scalacache.guava._
import com.smitestats.matchidservice.config.AppConfig
import scala.concurrent.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.http4s.client.blaze.BlazeClientBuilder
import com.smitestats.matchidservice.clients.SmiteApiClient
import com.smitestats.matchidservice.helpers.SessionHelper
import com.smitestats.matchidservice.helpers.SignatureHelper
import com.smitestats.matchidservice.core.Processor
import java.io.InputStream
import java.io.OutputStream
import com.amazonaws.services.lambda.runtime.Context
import scala.io.Source

import io.circe.parser._
import io.circe.syntax._
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClient}

class Main {

    val logger: Logger = LoggerFactory.getLogger("Main")
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ec) 
    implicit val sessionCache: Cache[String] = GuavaCache[String]

    def run(input: InputStream, output: OutputStream, context: Context): Unit = {
        (for {
            _ <- IO { logger.info("Retrieving AppConfig...") }
            config <- AppConfig.loaded
            _ <- IO { logger.info("Beginning processing...") }
            _ <- BlazeClientBuilder[IO](ec).resource.use { client =>
                Processor.process(ec, cs, config, client, sessionCache)
            }
        } yield ()).unsafeRunSync
    }
}