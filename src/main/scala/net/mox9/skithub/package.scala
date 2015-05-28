package net.mox9

import scala.language.higherKinds
import scala.language.implicitConversions

import play.api.libs.json.{ JsObject, JsValue }

import java.util.concurrent.TimeUnit

// TODO: Rename to octokit.scala?
// TODO: tabulate/Tabulate class/.tt ext method
// TODO: tabulate implicits trait
// TODO: look into NSME: commit 612b11957b
// TODO: Migrate to Play 2.4
// TODO: Explore github.orgs("org-name").repos  .get?
// TODO: Make aliases final?
// TODO: Switch to WSClient
// TODO: Handle HTTP codes. Abstraction?
package object skithub
  extends    ScalaImplicits
     with PlayJsonImplicits
     with   PlayWsImplicits

package skithub {
  trait ScalaImplicits {
    @inline type ->[+A, +B]             = scala.Product2[A, B]
    @inline type ?=>[-A, +B]            = scala.PartialFunction[A, B]
    @inline type CBF[-From, -Elem, +To] = scala.collection.generic.CanBuildFrom[From, Elem, To]
    @inline type Duration               = scala.concurrent.duration.Duration
    @inline type ExecCtx                = scala.concurrent.ExecutionContext
    @inline type FiniteDuration         = scala.concurrent.duration.FiniteDuration
    @inline type Future[+T]             = scala.concurrent.Future[T]
    @inline type Trav[+A]               = scala.collection.Traversable[A]
    @inline type TravOnce[+A]           = scala.collection.TraversableOnce[A]

    @inline val ->               = scala.Product2
    @inline val Duration         = scala.concurrent.duration.Duration
    @inline val ExecCtx          = scala.concurrent.ExecutionContext
    @inline val FiniteDuration   = scala.concurrent.duration.FiniteDuration
    @inline val Future           = scala.concurrent.Future
    @inline val Trav             = scala.collection.Traversable
    @inline val TravOnce         = scala.collection.TraversableOnce

    val ISO_8601_FMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val UTC_TZ = java.util.TimeZone getTimeZone "UTC"

    @inline def idFun[T] = (t: T) => t
    @inline def const[T, U](x: T)(y: U): T = x
    @inline def nanoTime(): Long = java.lang.System.nanoTime

    def nowIso8601() =
      new java.text.SimpleDateFormat(ISO_8601_FMT) doto (_ setTimeZone UTC_TZ) format new java.util.Date()

    def timed[T](body: => T): T -> Duration = {
      val t0 = nanoTime()
      val t = body
      val t1 = nanoTime()
      t -> (Duration fromNanos t1 - t0)
    }

    @inline implicit def DurationInt(n: Int) = scala.concurrent.duration.DurationInt(n)

    @inline implicit class AnyW[T](private val x: T) {
      @inline def toUnit(): Unit = ()

      @inline def pipe[U](f: T => U): U  = f(x)
      @inline def sideEffect(u: Unit): T = x
      @inline def doto(f: T => Unit): T  = sideEffect(f(x))

      @inline def >>(): Unit = println(x)

      @inline def requiring(p: T => Boolean): Option[T] = if (p(x)) Some(x) else None
      @inline def isOr(p: T => Boolean)(alt: => T): T   = if (p(x)) x else alt

      @inline def maybe[U](pf: T ?=> U): Option[U]      = pf lift x
      @inline def matchOr[U](alt: => U)(pf: T ?=> U): U = pf.applyOrElse(x, const(alt))
    }

    @inline implicit class DurationW(private val d: Duration) {
      def toHHmmssSSS = {
        import TimeUnit._
        val l = d.toMillis

        val hrs  = MILLISECONDS toHours   l
        val mins = MILLISECONDS toMinutes l - (HOURS toMillis hrs)
        val secs = MILLISECONDS toSeconds l - (HOURS toMillis hrs) - (MINUTES toMillis mins)
        val ms   = MILLISECONDS toMillis  l - (HOURS toMillis hrs) - (MINUTES toMillis mins) - (SECONDS toMillis secs)

        f"$hrs%02dh$mins%02dm$secs%02ds$ms%03d"
      }
    }

    @inline implicit class FutureW[T](private val f: Future[T]) {
      @inline def await(atMost: Duration): T = scala.concurrent.Await.result(f, atMost)
      @inline def await5s: T                 = f await 5.seconds
      @inline def await30s: T                = f await 30.seconds
    }

    @inline implicit class FutureTravOnceW[T, M[X] <: TravOnce[X]](private val f: Future[M[T]]) {
      @inline def foldMap[U >: T](z: U)(op: (U, T) => U)(implicit ec: ExecCtx): Future[U] = foldLeftMap(z)(op)
      @inline def foldLeftMap[U] (z: U)(op: (U, T) => U)(implicit ec: ExecCtx): Future[U] = f map (_.foldLeft(z)(op))
    }

    @inline implicit class TravOnceW[T, M[X] <: TravOnce[X]](private val xs: M[T]) {
      @inline def traverse[U](f: T => Future[U])(implicit cbf: CBF[M[T], U, M[U]], ec: ExecCtx): Future[M[U]] =
        Future.traverse(xs)(f)
    }

    @inline implicit class TravOneFutureW[T, M[X] <: TravOnce[X]](private val fs: M[Future[T]]) {
      @inline def sequence(implicit cbf: CBF[M[Future[T]], T, M[T]], ec: ExecCtx): Future[M[T]]      = Future sequence fs
      @inline def firstCompletedOf                         (implicit ec: ExecCtx): Future[T]         = Future firstCompletedOf fs
      @inline def findFut(p: T => Boolean)                 (implicit ec: ExecCtx): Future[Option[T]] = Future.find(fs)(p)
      @inline def foldFut[R](z: R)(op: (R, T) => R)        (implicit ec: ExecCtx): Future[R]         = Future.fold(fs)(z)(op)
    }

    @inline implicit class IntWithAlign(private val x: Int) {
      @inline def lalign: String = if (x == 0) "%s" else s"%-${x}s"
      @inline def ralign: String = if (x == 0) "%s" else s"%${x}s"
    }
    @inline def lalign(width: Int): String = width.lalign
    @inline def ralign(width: Int): String = width.ralign

    private def trimHeader(h: String): Int => String = {
      case i if i >= h.length => h
      case i if i > 5         => h.substring(0, i - 2) + ".."
      case i if i > 1         => h.substring(0, i - 1) + "-"
      case _                  => h.substring(0, 1)
    }

    @inline implicit class TravProdWithTabular[T <: Product](private val xs: Trav[T]) {
      @inline def tabularps = {
        xs.headOption match {
          case None    => Nil
          case Some(h) =>
            val rows = xs.toVector map (_.productIterator.toVector map (_.toString))
            val cols = (0 until h.productArity).toVector map (idx => xs map (_.productElement(idx).toString))

            val widths = cols map (col => col map (_.length) max)

            val headers0 = h.getClass.getDeclaredFields.toVector map (_.getName)
            val headers = headers0 zip widths map Function.uncurried(trimHeader _).tupled

            val rowFormat = widths map ralign mkString " "
            (headers +: rows) map (row => rowFormat.format(row.seq: _*))
        }
      }
      @inline def showps()  = tabularps foreach println
    }

    @inline implicit class MapWithTabular[K, V](private val xs: Trav[K -> V]) {
      @inline def maxKeyLen = xs.toIterator.map(_._1.toString.length).max
      @inline def tabularKV = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2))
      @inline def showKV()  = tabularKV foreach println
    }

    @inline implicit class MultimapWithTabular[K, V](private val xs: Trav[K -> Trav[V]]) {
      @inline def tabularKVs = xs map (kv => s"%${xs.maxKeyLen}s %s".format(kv._1, kv._2.mkString("[", "],[", "]")))
      @inline def showKVs()  = tabularKVs foreach println
    }
  }

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

    @inline implicit class Any2PlayJsonW[T](private val x: T) {
      @inline def toJson(implicit W: Writes[T]): JsValue = Json toJson x
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
    }

    @inline implicit class JsErrorW[T](private val e: JsError) {
      @inline def toFlatJson: JsObject = JsError toFlatJson e
    }
  }

  trait PlayWsImplicits {
    @inline type WSResponse = play.api.libs.ws.WSResponse

    @inline val WS = play.api.libs.ws.WS
  }
}
