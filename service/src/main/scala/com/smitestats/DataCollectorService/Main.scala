package com.smitestats.DataCollectorService

import cats.effect._
import cats.syntax.all._

object Main extends App {
    (IO(println("Hello world")) *> IO.never).unsafeRunSync
}