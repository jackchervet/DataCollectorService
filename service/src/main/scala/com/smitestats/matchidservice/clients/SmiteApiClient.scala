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
import scala.util.Random

object SmiteApiClient {

    final val matchesPerWindow = 10;

    val logger: Logger = LoggerFactory.getLogger("SmiteApiClient")

    object endpoints {
        val getMatchIdsByQueue = "getmatchidsbyqueueJson"
        val getMatchDetailsBatch = "getmatchdetailsbatchJson"
    }

    private def getDateTwoHoursPrev: String = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    private def getHourTwoHoursPrev: Int = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).format(DateTimeFormatter.ofPattern("HH")).toInt
    private def getCurrentMinute: Int = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("mm")).toInt

    def getHourValuesFormatted: List[String] = {
        val minutes = List("00", "10", "20", "30", "40", "50")
        val hour = getHourTwoHoursPrev
        minutes.map(m => s"${hour},${m}")
    }

    def pullRandomIndicies(window: List[String]): List[String] = {
        (1 to matchesPerWindow).toList
            .map(_ => Random.nextInt(window.length))
            .toSet
            .map(i => window(i))
            .toList
    }

    def flattenResponses(resps: List[List[GetMatchIdsByQueueResponse]]): List[String] = {
        resps.map { window =>
            window.filter(r => r.Active_Flag == "n").map(r => r.Match)
        }
        .map(window => pullRandomIndicies(window))
        .flatten
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
            targets <- IO(getHourValuesFormatted.map { hour =>
                s"${config.smiteApiBaseUrl}/" +
                s"${endpoints.getMatchIdsByQueue}/" +
                s"${config.devId}/" +
                s"${signature}/" +
                s"${session}/" +
                s"${timestamp}/" +
                s"${queue}/" +
                s"${getDateTwoHoursPrev}/" +
                s"${hour}"
            })
            resps <- targets.map { target =>
                client.expect[List[GetMatchIdsByQueueResponse]](target)
            }.sequence
        } yield flattenResponses(resps)
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