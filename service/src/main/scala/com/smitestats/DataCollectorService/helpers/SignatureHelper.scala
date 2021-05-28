package com.smitestats.DataCollectorService.helpers

import com.smitestats.DataCollectorService.config.AppConfig
import java.security.MessageDigest
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import cats.effect.IO

object SignatureHelper {
    def getCurrentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    def generateSignature(endpoint: String): IO[String] = 
        for {
            fullString <- IO(AppConfig.loaded.devId + endpoint + AppConfig.loaded.authKey + getCurrentTimestamp)
            digest <- IO(MessageDigest.getInstance("MD5").digest(fullString.getBytes))
            bigInt <- IO(new BigInteger(1,digest))
        } yield bigInt.toString(16) 
}