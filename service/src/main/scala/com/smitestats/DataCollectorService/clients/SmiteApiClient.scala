package com.smitestats.DataCollectorService.clients

import com.smitestats.DataCollectorService.models.GetMatchIdsByQueueResponse
import com.smitestats.DataCollectorService.helpers.SignatureHelper
import com.smitestats.DataCollectorService.config.AppConfig

import cats._
import cats.implicits._
import cats.effect._
import com.smitestats.DataCollectorService.helpers.SessionHelper
import scala.concurrent._

import org.http4s.client.blaze._
import org.http4s.client._
import org.http4s.circe.CirceEntityDecoder._
import scalacache._
import scalacache.guava._

import io.circe.generic.semiauto._

object SmiteApiClient {

    object endpoints {
        val getMatchIdsByQueue = "getmatchidsbyqueueJson"
    }

    def getMatchIdsByQueue(queue: String, date: String, hour: String)(implicit 
        client: Client[IO], 
        sessionCache: Cache[String], 
        ec: ExecutionContext, 
        cs: ContextShift[IO]
    ): IO[GetMatchIdsByQueueResponse] = { 
        for {
            signature <- SignatureHelper.generateSignature(endpoints.getMatchIdsByQueue)
            session <- SessionHelper.getSession
            target <- IO {
                s"${AppConfig.loaded.smiteApiBaseUrl}/" +
                s"${endpoints.getMatchIdsByQueue}/" +
                s"${AppConfig.loaded.devId}/" +
                s"${signature}/" +
                s"${session}/" +
                s"${SignatureHelper.getCurrentTimestamp}/" +
                s"${queue}/" +
                s"${date}/" +
                s"${hour}"
            }
            resp <- client.expect[GetMatchIdsByQueueResponse](target)
        } yield resp
    }
}