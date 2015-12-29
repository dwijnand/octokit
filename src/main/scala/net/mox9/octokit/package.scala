package net.mox9

// TODO: Tabulate class
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Handle HTTP codes. Abstraction?
// TODO: Figure out how to auto-stop on Ctrl-D in REPL
// TODO: Handle IO error nicer then stacktrace in your face?
// TODO: Add BasicAuth support?
// TODO: Add query params to API calls
// TODO: Add support for using Resp ETag and sending as If-None-Match
// TODO: Compare data field types with other octokits
// TODO: RateLimitClient https://developer.github.com/v3/rate_limit/
// TODO: Add caching on ETag
package object octokit
  extends ScalaKitPre
     with TabularKitPre
     with AkkaKitPre
     with PlayFunctionalKitPre with PlayJsonKitPre with PlayWsKitPre

import play.api.libs.json._

object PlayJsonCaseClass1 {
  def  reads[T](fjs:   Reads[T]):   Reads[T] = Reads.of[JsObject] map camelCaseFields andThen fjs
  def writes[T](tjs: OWrites[T]): OWrites[T] = OWrites[T](x => snakeCaseFields(tjs writes x))
  def format[T](f:   OFormat[T]): OFormat[T] = OFormat[T](reads(f), writes(f))

  private def camelCaseFields(json: JsObject) = JsObject(json.fields map (kv => camelCase(kv._1) -> kv._2))
  private def snakeCaseFields(json: JsObject) = JsObject(json.fields map (kv => snakeCase(kv._1) -> kv._2))

  private def camelCase(s: String) =
    (s.split("_").toList match {
      case head :: tail => head :: tail.map(_.capitalize)
      case x            => x
    }).mkString

  private def snakeCase(s: String) =
    s.foldLeft(new StringBuilder) {
      case (s, c) if Character isUpperCase c => s append "_" append (Character toLowerCase c)
      case (s, c)                            => s append c
    }.toString
}

object PlayJsonCaseClass2 {
  implicit class StringWithCasing(private val s: String) extends AnyVal {
    def camelCase =
      (s.split("_").toList match {
        case head :: tail => head :: tail.map(_.capitalize)
        case x            => x
      }).mkString

    def snakeCase =
      s.foldLeft(new StringBuilder) {
        case (s, c) if Character isUpperCase c => s append "_" append (Character toLowerCase c)
        case (s, c)                            => s append c
      }.toString
  }

  implicit class KvWithMap[K, V](private val kv: (K, V)) extends AnyVal {
    def fstMap[KK](f: K => KK): (KK, V) = f(kv._1) -> kv._2
    def sndMap[VV](f: V => VV): (K, VV) = kv._1 -> f(kv._2)
  }

  implicit class JsObjectWithMap(private val json: JsObject) extends AnyVal {
    def map(f: ((String, JsValue)) => (String, JsValue)): JsObject = JsObject(json.fields map f)

    def mapKeys(f: String => String): JsObject = json map (_ fstMap f)
  }

  implicit class WritesToFunction1[A](private val tjs: Writes[A]) extends AnyVal {
    def apply(x: A): JsValue = tjs writes x
  }

  implicit class OWritesToFunction1[A](private val tjs: OWrites[A]) extends AnyVal {
    def apply(x: A): JsObject = tjs writes x
  }

  implicit class OWritesWithMap[A](private val tjs: OWrites[A]) extends AnyVal {
    def map(f: JsObject => JsObject): OWrites[A] = OWrites[A](x => f(tjs writes x))
  }

  def  reads[T](fjs:   Reads[T]):   Reads[T] = Reads.of[JsObject] map (_ mapKeys (_.camelCase)) andThen fjs
  def writes[T](tjs: OWrites[T]): OWrites[T] =                tjs map (_ mapKeys (_.snakeCase))
  def format[T](f:   OFormat[T]): OFormat[T] = OFormat[T](reads(f), writes(f))
}
