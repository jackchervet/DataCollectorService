package com.smitestats.DataCollectorService.config

import com.github.andr83.scalaconfig._

case class AppConfig(
    devId: String,
    authKey: String,
    smiteApiBaseUrl: String
)

object AppConfig {
    val loaded: AppConfig = {
        val config: Config = ConfigFactory.load()

    }
}