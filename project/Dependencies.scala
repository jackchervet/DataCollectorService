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
}
