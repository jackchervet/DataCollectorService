import com.smitestats.Dependencies._

name := """data-collector-service"""

lazy val commons = Seq(
    version := "3.0.1",
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
            AWS.dynamodb,
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
            ScalaTest.scalatic,
            SLF4J.api,
            SLF4J.simple
        ),
        assembly / mainClass := Some("com.smitestats.datacollectorservice.Main"),
        assembly / assemblyOption := (assembly / assemblyOption).value.copy(cacheUnzip = false),
        assembly / assemblyOption := (assembly / assemblyOption).value.copy(cacheOutput = false),
        assembly / assemblyMergeStrategy := {
            case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
            case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
            case PathList("META-INF", "MANIFEST.MF")           => MergeStrategy.discard
            case PathList("META-INF", xs @ _*)                 => MergeStrategy.first
            case "module-info.class"                           => MergeStrategy.first
            case "application.conf"                            => MergeStrategy.concat
            case "unwanted.txt"                                => MergeStrategy.discard
            case x =>
                val oldStrategy = (assembly / assemblyMergeStrategy).value
                oldStrategy(x)
        }
    )
