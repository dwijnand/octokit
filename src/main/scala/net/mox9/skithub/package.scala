package net.mox9

import scala.language.implicitConversions

import play.api.libs.json.{ JsObject, JsError, JsValue }

package object skithub {
  type ->[+A, +B]     = scala.Product2[A, B]
  type ?=>[-A, +B]    = scala.PartialFunction[A, B]
  type Duration       = scala.concurrent.duration.Duration
  type FiniteDuration = scala.concurrent.duration.FiniteDuration
  type Future[+T]     = scala.concurrent.Future[T]
  type JsonFormat[T]  = play.api.libs.json.Format[T]

  val ->         = scala.Product2
  val Future     = scala.concurrent.Future
  val Json       = play.api.libs.json.Json
  val JsonFormat = play.api.libs.json.Format

  implicit def DurationInt(n: Int): scala.concurrent.duration.DurationInt =
    scala.concurrent.duration.DurationInt(n)

  implicit class AnyW[T](private val x: T) extends AnyVal {
    @inline def toUnit(): Unit = ()

    @inline def pipe[U](f: T => U): U     = f(x)
    @inline def sideEffect(body: Unit): T = x
    @inline def doto(f: T => Unit): T     = sideEffect(f(x))

    @inline def >>(): Unit = sideEffect(println(x)).toUnit()

    @inline def maybe[U](pf: T ?=> U): Option[U] = pf lift x
  }

  implicit class FutureW[T](private val f: Future[T]) extends AnyVal {
    @inline def result(atMost: Duration = 5.seconds): T = sc.Await.result(f, atMost)
  }

  implicit class JsValueW[T](private val json: JsValue) extends AnyVal {
    @inline def pp: String = Json prettyPrint json
  }

  implicit class JsErrorW[T](private val e: JsError) extends AnyVal {
    @inline def toFlatJson: JsObject = JsError toFlatJson e
  }
}
