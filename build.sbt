import com.smitestats.Dependencies._

name := """match-id-service"""

lazy val commons = Seq(
    version := "1.0.0-SNAPSHOT",    
    scalaVersion := "2.13.6",
    organization := "com.smitestats",
    scalacOptions += "-Ymacro-annotations"
)

lazy val service = project
    .in(file("service"))
    .settings(
        name := "service",
        commons,
        libraryDependencies ++= Seq(
            AWS.lambda,
            AWS.sqs,
            Cats.core,
            Cats.effect,
            Circe.core,
            Circe.generic,
            Circe.parser,
            Circe.literal,
            Circe.config,
            FS2.core,
            FS2.io,
            Http4s.dsl,
            Http4s.client,
            Http4s.circe,
            ScalaCache.core,
            ScalaTest.core,
            SLF4J.api,
            SLF4J.simple
        ),
        assembly / mainClass := Some("com.smitestats.matchidservice.Main"),
        assembly / assemblyOption := (assembly / assemblyOption).value.copy(cacheUnzip = false),
        assembly / assemblyOption := (assembly / assemblyOption).value.copy(cacheOutput = false)
    )
