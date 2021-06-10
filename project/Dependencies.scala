package com.smitestats

import sbt._

object Dependencies {
    object AWS {
        val lambda = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
        val dynamodb = "software.amazon.awssdk" % "dynamodb" % "2.16.78" 
    }
    
    object Cats {
        val core = "org.typelevel" %% "cats-core" % "2.1.1"
        val effect = "org.typelevel" %% "cats-effect" % "2.1.1"
    }

    object ScalaTest {
       val scalatic = "org.scalactic" %% "scalactic" % "3.2.9"
       val core = "org.scalatest" %% "scalatest" % "3.2.9" % "test"
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
        val config = "io.circe" %% "circe-config" % "0.7.0"
    }

    object SLF4J {
        private val slf4jVersion = "1.7.5"
        val api = "org.slf4j" % "slf4j-api" % slf4jVersion
        val simple = "org.slf4j" % "slf4j-simple" % slf4jVersion
    }

    object FS2 {
        private val fs2Version = "2.5.3"
        val core = "co.fs2" %% "fs2-core" % fs2Version
        val io = "co.fs2" %% "fs2-io" % fs2Version
    }
}
