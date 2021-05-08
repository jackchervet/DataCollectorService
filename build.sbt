import com.smitestats.Dependencies._

name := """data-collector-service"""

lazy val commons = Seq(
    version := "1.0-SNAPSHOT",    
    scalaVersion := "2.13.5",
    organization := "com.smitestats"    
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
            ScalaTest.core
        )
    )
