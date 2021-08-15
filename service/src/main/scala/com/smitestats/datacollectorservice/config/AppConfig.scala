package com.smitestats.datacollectorservice.config

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.config.syntax._
import io.circe.config.parser

case class AppConfig(
    devId: String,
    authKey: String,
    smiteApiBaseUrl: String,
    downstreamS3: String
)

object AppConfig {
    lazy val loaded: IO[AppConfig] = parser.decodeF[IO, AppConfig]
}