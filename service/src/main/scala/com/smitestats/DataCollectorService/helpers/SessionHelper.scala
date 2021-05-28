package com.smitestats.DataCollectorService.helpers

import cats._
import cats.implicits._
import cats.effect._
import org.http4s.client.blaze._
import org.http4s.client._
import scala.concurrent._
import com.smitestats.DataCollectorService.config.AppConfig
import scalacache._
import scalacache.guava._
import scalacache.modes.scalaFuture._
import scala.concurrent.duration._
import scala.language._

object SessionHelper {
    final val endpoint = "createsessionJson"

    def getSession(implicit client: Client[IO], sessionCache: Cache[String], ec: ExecutionContext, cs: ContextShift[IO]): IO[String] = {
        for {
            maybeSession <- IO.fromFuture(IO(sessionCache.get[Future]("session")))
            session <- maybeSession match {
                case None => generateSession
                case Some(session) => IO(session)
            }
        } yield session
    }

    private def generateSession(implicit client: Client[IO], sessionCache: Cache[String], ec: ExecutionContext, cs: ContextShift[IO]): IO[String] = {
        for {
            target <- IO { 
                    s"${AppConfig.loaded.smiteApiBaseUrl}/" +
                    s"${AppConfig.loaded.devId}/" +
                    s"${SignatureHelper.generateSignature(endpoint)}/" +
                    s"${SignatureHelper.getCurrentTimestamp}"
                }
            resp <- client.expect[String](target)
            _ <- IO.fromFuture(IO(sessionCache.put[Future]("session")(resp, Some(15 minutes))))
        } yield resp
    }
}