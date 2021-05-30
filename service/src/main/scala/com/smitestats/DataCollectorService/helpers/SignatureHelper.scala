package com.smitestats.DataCollectorService.helpers

import com.smitestats.DataCollectorService.config.AppConfig
import java.security.MessageDigest
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import cats.effect.IO
import java.time.Instant
import java.time.ZoneOffset

object SignatureHelper {
    def getCurrentTimestamp: String = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

    def generateSignature(endpoint: String, timestamp: String)(implicit config: AppConfig): IO[String] = 
        for {
            fullString <- IO(config.devId + endpoint.replace("Json", "") + config.authKey + timestamp)
            digest <- IO(MessageDigest.getInstance("MD5").digest(fullString.getBytes))
            bigInt <- IO(new BigInteger(1,digest))
        } yield bigInt.toString(16) 
}