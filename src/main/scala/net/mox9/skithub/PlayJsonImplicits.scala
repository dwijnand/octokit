package net.mox9.skithub

import scala.language.higherKinds
import scala.language.implicitConversions

import play.api.data.validation.ValidationError
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{ JsArray, JsNull, JsObject, JsPath, JsValue }

trait PlayJsonImplicits {
  @inline type JsError           = play.api.libs.json.JsError
  @inline type JsResultException = play.api.libs.json.JsResultException
  @inline type JsResult[+T]      = play.api.libs.json.JsResult[T]
  @inline type JsSuccess[T]      = play.api.libs.json.JsSuccess[T]
  @inline type JsonFormat[T]     = play.api.libs.json.Format[T]
  @inline type Reads[T]          = play.api.libs.json.Reads[T]
  @inline type Writes[-T]        = play.api.libs.json.Writes[T]

  @inline val JsError           = play.api.libs.json.JsError
  @inline val JsResultException = play.api.libs.json.JsResultException
  @inline val JsSuccess         = play.api.libs.json.JsSuccess
  @inline val Json              = play.api.libs.json.Json
  @inline val JsonFormat        = play.api.libs.json.Format
  @inline val Reads             = play.api.libs.json.Reads
  @inline val Writes            = play.api.libs.json.Writes

  def jsSuccess[T](x: T): JsResult[T] = JsSuccess(x)
  def jsError(errors: Seq[(JsPath, Seq[ValidationError])]): JsResult[Nothing] = JsError(errors)

  @inline implicit class Any2PlayJsonW[T](private val x: T) {
    @inline def toJson(implicit W: Writes[T]): JsValue    = Json toJson x
    @inline def js(implicit W: Writes[T]): JsValueWrapper = x
  }

  @inline implicit class Option2PlayJsonW[T](private val x: Option[T]) {
    @inline def orJsNull(implicit z: Writes[T]): JsValueWrapper = x map (_.js) getOrElse JsNull
  }

  @inline implicit class String2PlayJsonW(private val s: String) {
    @inline def jsonParse: JsValue = Json parse s
  }

  @inline implicit class ByteArray2PlayJsonW(private val bs: Array[Byte]) {
    @inline def jsonParse: JsValue = Json parse bs
  }

  @inline implicit class JsValueW(private val json: JsValue) {
    @inline def pp: String                      = Json prettyPrint json
    @inline def toJsonStr: String               = Json stringify json
    @inline def fromJson[T: Reads]: JsResult[T] = Json fromJson json
 // @inline def jsValues: Seq[JsValue]          = json.castToOpt[JsArray].fold(nil[JsValue])(_.value)
  }

  @inline implicit class JsErrorW[T](private val e: JsError) {
    @inline def toFlatJson: JsObject = JsError toFlatJson e
  }

  @inline implicit class FutureJsResultW[T](private val f: Future[JsResult[T]]) {
    @inline def flatten(implicit ec: ExecCtx): Future[T] =
      f flatMap {
        case JsSuccess(ts, _) => ts.future
        case JsError(errors)  => JsResultException(errors).failFut
      }
  }
}
