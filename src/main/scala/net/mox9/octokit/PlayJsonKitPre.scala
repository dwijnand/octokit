package net.mox9.octokit

import play.api.data.validation.ValidationError

// TODO: Rename to JsReads/JsWrites?
// TODO: Rename JsonFormat to JsFormat?
trait PlayJsonKitPre {
  @inline final type JsDefined         = play.api.libs.json.JsDefined
  @inline final type JsError           = play.api.libs.json.JsError
  @inline final type JsLookup          = play.api.libs.json.JsLookup
  @inline final type JsLookupResult    = play.api.libs.json.JsLookupResult
  @inline final type JsPath            = play.api.libs.json.JsPath
  @inline final type JsResultException = play.api.libs.json.JsResultException
  @inline final type JsResult[+T]      = play.api.libs.json.JsResult[T]
  @inline final type JsString          = play.api.libs.json.JsString
  @inline final type JsSuccess[T]      = play.api.libs.json.JsSuccess[T]
  @inline final type JsUndefined       = play.api.libs.json.JsUndefined
  @inline final type JsValue           = play.api.libs.json.JsValue
  @inline final type JsValueWrapper    = play.api.libs.json.Json.JsValueWrapper
  @inline final type JsonFormat[T]     = play.api.libs.json.Format[T]
  @inline final type Reads[T]          = play.api.libs.json.Reads[T]
  @inline final type Writes[-T]        = play.api.libs.json.Writes[T]

  @inline final val JsDefined         = play.api.libs.json.JsDefined
  @inline final val JsError           = play.api.libs.json.JsError
  @inline final val JsLookup          = play.api.libs.json.JsLookup
  @inline final val JsLookupResult    = play.api.libs.json.JsLookupResult
  @inline final val JsNull            = play.api.libs.json.JsNull
  @inline final val JsPath            = play.api.libs.json.JsPath
  @inline final val JsResultException = play.api.libs.json.JsResultException
  @inline final val JsString          = play.api.libs.json.JsString
  @inline final val JsSuccess         = play.api.libs.json.JsSuccess
  @inline final val JsUndefined       = play.api.libs.json.JsUndefined
  @inline final val JsValue           = play.api.libs.json.JsValue
  @inline final val Json              = play.api.libs.json.Json
  @inline final val JsonFormat        = play.api.libs.json.Format
  @inline final val Reads             = play.api.libs.json.Reads
  @inline final val Writes            = play.api.libs.json.Writes
  @inline final val __                = play.api.libs.json.__

  @inline final def jsSuccess[T](x: T)                                   : JsResult[T]       = JsSuccess(x)
  @inline final def jsError(errors: Seq[(JsPath, Seq[ValidationError])]) : JsResult[Nothing] = JsError(errors)

  @inline implicit final class Any2PlayJsonW[T](private val x: T) {
    @inline def toJson(implicit W: Writes[T]): JsValue    = Json toJson x
    @inline def js(implicit W: Writes[T]): JsValueWrapper = x

    @inline def jsSuccess: JsSuccess[T] = JsSuccess(x)
  }

  @inline implicit final class Option2PlayJsonW[T](private val x: Option[T]) {
    @inline def orJsNull(implicit z: Writes[T]): JsValueWrapper = x map (_.js) getOrElse JsNull
  }

  @inline implicit final class String2PlayJsonW(private val s: String) {
    @inline def jsonParseForce: JsValue = Json parse s
  }

  @inline implicit final class ByteArray2PlayJsonW(private val bs: Array[Byte]) {
    @inline def jsonParseForce: JsValue = Json parse bs
  }

  @inline implicit final class JsValueW(private val json: JsValue) {
    @inline def pp: String                            = Json prettyPrint json
    @inline def toJsonStr: String                     = Json stringify json
    @inline def fromJson[T](z: Reads[T]): JsResult[T] = Json.fromJson(json)(z)
 // @inline def jsValues: Seq[JsValue]                = json.castToOpt[JsArray].fold(nil[JsValue])(_.value)

    @inline def isJsNull: Boolean = json == JsNull
  }

  implicit final val JsErrorWrites = Writes[JsError](JsError.toJson)

  @inline implicit final class FutureJsResultW[T](private val f: Future[JsResult[T]]) {
    @inline def flatten(implicit ec: ExecCtx): Future[T] =
      f flatMap {
        case JsSuccess(ts, _) => ts.future
        case JsError(errors)  => JsResultException(errors).failFut
      }
  }

  @inline implicit final class Try2PlayJsonW[T](private val t: Try[T]) {
    @inline def toJsResultNow: JsResult[T] =
      t match {
        case Success(x) => JsSuccess(x)
        case Failure(e) => JsError(e.getMessage)
      }
    @inline def toJsResult(s: String): JsResult[T] =
      t match {
        case Success(x) => JsSuccess(x)
        case Failure(e) => JsError(s + ": " + e.getMessage)
      }
  }

  implicit val urlJsonFormat: JsonFormat[Url] =
    new JsonFormat[Url] {
      def reads(json: JsValue) = json.validate[String] flatMap (s => Url(s) toJsResult "Invalid URL")
      def writes(url: Url)     = url.toString.toJson
    }
}
