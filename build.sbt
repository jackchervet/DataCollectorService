import com.smitestats.Dependencies._

name := """data-collector-service"""

lazy val commons = Seq(
    version := "0.1-SNAPSHOT",    
    scalaVersion := "2.13.5",
    organization := "com.smitestats",
    scalacOptions += "-Ymacro-annotations"
)

lazy val root = project
    .in(file("."))
    .settings(commons)

lazy val service = project
    .in(file("service"))
    .settings(
        name := "service",
        commons,
        libraryDependencies ++= Seq(
            Cats.core,
            Cats.effect,
            Circe.core,
            Circe.generic,
            Circe.parser,
            Circe.literal,
            Circe.config,
            Http4s.dsl,
            Http4s.client,
            Http4s.circe,
            ScalaCache.core,
            ScalaTest.core,
            SLF4J.api,
            SLF4J.simple
        )
    )
