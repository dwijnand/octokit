package net.mox9

import scala.language.implicitConversions

import play.api.libs.json.{ JsObject, JsValue }

// TODO: Rename to octokit.scala?
package object skithub {
  @inline type ->[+A, +B]     = scala.Product2[A, B]
  @inline type ?=>[-A, +B]    = scala.PartialFunction[A, B]
  @inline type Duration       = scala.concurrent.duration.Duration
  @inline type FiniteDuration = scala.concurrent.duration.FiniteDuration
  @inline type Future[+T]     = scala.concurrent.Future[T]
  @inline type JsonFormat[T]  = play.api.libs.json.Format[T]
  @inline type JsError        = play.api.libs.json.JsError
  @inline type JsResult[+T]   = play.api.libs.json.JsResult[T]
  @inline type JsSuccess[T]   = play.api.libs.json.JsSuccess[T]

  @inline val ->         = scala.Product2
  @inline val Future     = scala.concurrent.Future
  @inline val Json       = play.api.libs.json.Json
  @inline val JsonFormat = play.api.libs.json.Format
  @inline val JsError    = play.api.libs.json.JsError
  @inline val JsSuccess  = play.api.libs.json.JsSuccess

  @inline implicit def DurationInt(n: Int): scala.concurrent.duration.DurationInt =
    scala.concurrent.duration.DurationInt(n)

  @inline implicit class AnyW[T](private val x: T) extends AnyVal {
    @inline def toUnit(): Unit = ()

    @inline def pipe[U](f: T => U): U     = f(x)
    @inline def sideEffect(body: Unit): T = x
    @inline def doto(f: T => Unit): T     = sideEffect(f(x))

    @inline def >>(): Unit = sideEffect(println(x)).toUnit()

    @inline def maybe[U](pf: T ?=> U): Option[U] = pf lift x
  }

  @inline implicit class FutureW[T](private val f: Future[T]) extends AnyVal {
    @inline def await(atMost: Duration): T = scala.concurrent.Await.result(f, atMost)
    @inline def await5s: T                 = f await 5.seconds
    @inline def await30s: T                = f await 30.seconds
  }

  @inline implicit class JsValueW[T](private val json: JsValue) extends AnyVal {
    @inline def pp: String        = Json prettyPrint json
    @inline def toJsonStr: String = Json stringify json
  }

  @inline implicit class JsErrorW[T](private val e: JsError) extends AnyVal {
    @inline def toFlatJson: JsObject = JsError toFlatJson e
  }
}
