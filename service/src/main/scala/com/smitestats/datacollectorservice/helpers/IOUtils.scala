package com.smitestats.datacollectorservice.helpers

import java.util.concurrent.{ CompletableFuture, CancellationException, CompletionException }
import cats.effect.IO
import java.util.function.BiFunction

trait IOUtils {
    
   def fromJavaFuture[A](makeCf: => CompletableFuture[A]): IO[A] = 
    IO.cancelable(cb => {
      val cf = makeCf
      cf.handle[Unit](new BiFunction[A, Throwable, Unit] {
        override def apply(result: A, err: Throwable): Unit = {
          err match {
            case null =>
              cb(Right(result))
            case _: CancellationException =>
              ()
            case ex: CompletionException if ex.getCause ne null =>
              cb(Left(ex.getCause))
            case ex =>
              cb(Left(ex))
          }
        }
      })
      IO(cf.cancel(true))
    })
}
