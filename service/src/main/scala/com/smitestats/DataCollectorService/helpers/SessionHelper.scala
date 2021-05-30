package com.smitestats.DataCollectorService.helpers

import cats._
import cats.implicits._
import cats.effect._
import org.http4s.client.blaze._
import org.http4s.client._
import org.http4s.circe.CirceEntityDecoder._
import scala.concurrent._
import com.smitestats.DataCollectorService.config.AppConfig
import com.smitestats.DataCollectorService.models.GetSessionResponse
import scalacache._
import scalacache.guava._
import scalacache.modes.scalaFuture._
import scala.concurrent.duration._
import scala.language._

import io.circe.generic.semiauto._

object SessionHelper {
    final val endpoint = "createsessionJson"

    def getSession(implicit client: Client[IO], sessionCache: Cache[String], ec: ExecutionContext, cs: ContextShift[IO], config: AppConfig): IO[String] = {
        for {
            maybeSession <- IO.fromFuture(IO(sessionCache.get[Future]("session")))
            session <- maybeSession match {
                case None => generateSession
                case Some(session) => IO(session)
            }
        } yield session
    }

    private def generateSession(implicit client: Client[IO], sessionCache: Cache[String], ec: ExecutionContext, cs: ContextShift[IO], config: AppConfig): IO[String] = {
        for {
            timestamp <- IO { SignatureHelper.getCurrentTimestamp }
            signature <- SignatureHelper.generateSignature(endpoint, timestamp)
            target <- IO { s"${config.smiteApiBaseUrl}/${endpoint}/${config.devId}/${signature}/${timestamp}" }
            resp <- client.expect[GetSessionResponse](target)
            _ <- IO.fromFuture(IO(sessionCache.put[Future]("session")(resp.session_id, Some(15 minutes))))
        } yield resp.session_id
    }
}