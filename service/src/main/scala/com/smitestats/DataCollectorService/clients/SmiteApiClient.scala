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
import com.smitestats.DataCollectorService.models.MatchIdResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SmiteApiClient {

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
            resp <- client.expect[List[MatchIdResponse]](target)
        } yield resp.filter(r => r.Active_Flag == "n").map(r => r.Match)
    }
}