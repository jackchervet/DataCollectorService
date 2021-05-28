package com.smitestats

import sbt._

object Dependencies {
    object Cats {
        val core = "org.typelevel" %% "cats-core" % "2.1.1"
        val effect = "org.typelevel" %% "cats-effect" % "2.1.1"
    }

    object ScalaTest {
        val core = "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    }

    object Http4s {
        private val http4sVersion = "0.21.22"
        val client = "org.http4s" %% "http4s-blaze-client" % http4sVersion
        val circe = "org.http4s" %% "http4s-circe" % http4sVersion
        val dsl = "org.http4s" %% "http4s-dsl" % http4sVersion
    }

    object ScalaCache {
        val core = "com.github.cb372" %% "scalacache-guava" % "0.28.0"
    }

    object Circe {
        private val circeVersion = "0.14.0"
        val core = "io.circe" %% "circe-core" % circeVersion
        val generic = "io.circe" %% "circe-generic" % circeVersion
        val parser = "io.circe" %% "circe-parser" % circeVersion
        val literal = "io.circe" %% "circe-literal" % circeVersion
    }

    object ScalaConfig {
        val core = "com.github.andr83" %% "scalaconfig" % "0.7"
    }
}
