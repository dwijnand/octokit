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

  implicit class FutureW[T](private val f: Future[T]) extends AnyVal {
    @inline def result(atMost: Duration = 5.seconds): T = sc.Await.result(f, atMost)
  }

  implicit class JsValueW[T](private val json: JsValue) extends AnyVal {
    def pp: String = Json prettyPrint json
  }
}
