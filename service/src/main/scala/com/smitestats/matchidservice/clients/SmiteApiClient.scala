package com.smitestats.datacollectorservice.clients

import com.smitestats.datacollectorservice.helpers.SignatureHelper
import com.smitestats.datacollectorservice.helpers.SessionHelper
import com.smitestats.datacollectorservice.config.AppConfig
import com.smitestats.datacollectorservice.models.GetMatchIdsByQueueResponse
import com.smitestats.datacollectorservice.models.GetMatchDetailsBatchResponse

import cats._
import cats.implicits._
import cats.effect._
import scala.concurrent._

import org.http4s.client.blaze._
import org.http4s.client._
import org.http4s.circe.CirceEntityDecoder._
import scalacache._
import scalacache.guava._

import io.circe.generic.semiauto._
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.slf4j.Logger

object SmiteApiClient {

    val logger: Logger = LoggerFactory.getLogger("SmiteApiClient")

    object endpoints {
        val getMatchIdsByQueue = "getmatchidsbyqueueJson"
        val getMatchDetailsBatch = "getmatchdetailsbatchJson"
    }

    private def getDateOneHourPrev: String = LocalDateTime.now(ZoneOffset.UTC).minusHours(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    private def getHourOneHourPrev: Int = LocalDateTime.now(ZoneOffset.UTC).minusHours(1).format(DateTimeFormatter.ofPattern("HH")).toInt
    private def getCurrentMinute: Int = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("mm")).toInt

    def getHourValueFormatted: String = {
        val minute = (Math.floor(getCurrentMinute.toDouble / 10) * 10).toInt
        val hour = getHourOneHourPrev
        minute match {
            case 0 => s"${hour},00"
            case 60 => s"${hour},00"
            case _ => s"${hour},${minute}"
        }
    }

    def getMatchIdsByQueue(queue: Int)(implicit 
        client: Client[IO], 
        sessionCache: Cache[String], 
        ec: ExecutionContext, 
        cs: ContextShift[IO],
        config: AppConfig
    ): IO[List[String]] = { 
        for {
            timestamp <- IO { SignatureHelper.getCurrentTimestamp }
            signature <- SignatureHelper.generateSignature(endpoints.getMatchIdsByQueue, timestamp)
            session <- SessionHelper.getSession
            target <- IO {
                s"${config.smiteApiBaseUrl}/" +
                s"${endpoints.getMatchIdsByQueue}/" +
                s"${config.devId}/" +
                s"${signature}/" +
                s"${session}/" +
                s"${timestamp}/" +
                s"${queue}/" +
                s"${getDateOneHourPrev}/" +
                s"${getHourValueFormatted}"
            }
            resp <- client.expect[List[GetMatchIdsByQueueResponse]](target)
        } yield resp.filter(r => r.Active_Flag == "n").map(r => r.Match)
    }

    def getMatchDetailsBatch(ids: List[String])(implicit 
        client: Client[IO], 
        sessionCache: Cache[String], 
        ec: ExecutionContext, 
        cs: ContextShift[IO],
        config: AppConfig
    ): IO[List[GetMatchDetailsBatchResponse]] = { 
        for {
            timestamp <- IO { SignatureHelper.getCurrentTimestamp }
            signature <- SignatureHelper.generateSignature(endpoints.getMatchDetailsBatch, timestamp)
            session <- SessionHelper.getSession
            target <- IO {
                s"${config.smiteApiBaseUrl}/" +
                s"${endpoints.getMatchDetailsBatch}/" +
                s"${config.devId}/" +
                s"${signature}/" +
                s"${session}/" +
                s"${timestamp}/" +
                s"${ids.mkString(",")}"
            }
            resp <- client.expect[List[GetMatchDetailsBatchResponse]](target)
        } yield resp
    }
}