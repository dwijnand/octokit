package net.mox9

import scala.language.implicitConversions

import play.api.libs.json.{ Json, JsValue }

import scala.{ concurrent => sc }
import scala.concurrent.{ duration => scd }

package object skithub {
  type Duration       = scd.Duration
  type FiniteDuration = scd.FiniteDuration
  type Future[+T]     = sc.Future[T]

  val Future = scala.concurrent.Future

  implicit def DurationInt(n: Int): scd.DurationInt = scd.DurationInt(n)

  implicit class AnyW[T](private val x: T) extends AnyVal {
    @inline def pipe[U](f: T => U): U     = f(x)
    @inline def sideEffect(body: Unit): T = x
    @inline def doto(f: T => Unit): T     = sideEffect(x doto f)

    @inline def >> : T = x doto println
  }

  implicit class FutureW[T](private val f: Future[T]) extends AnyVal {
    @inline def result(atMost: Duration = 5.seconds): T = sc.Await.result(f, atMost)
  }

  implicit class JsValueW[T](private val json: JsValue) extends AnyVal {
    @inline def pp: String = Json prettyPrint json
  }
}
