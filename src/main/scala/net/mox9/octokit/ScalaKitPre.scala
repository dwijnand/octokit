package net.mox9.octokit

import scala.language.higherKinds
import scala.language.implicitConversions

import java.util.concurrent.TimeUnit

// TODO: Checkout more std package
trait ScalaKitPre {
  @inline final type ->[+A, +B]             = scala.Product2[A, B]
  @inline final type ?=>[-A, +B]            = scala.PartialFunction[A, B]
  @inline final type \/[+A, +B]             = scala.Either[A, B]
  @inline final type \?/[+A, +B]            = scala.concurrent.Future[scala.Either[A, B]]
  @inline final type CBF[-From, -Elem, +To] = scala.collection.generic.CanBuildFrom[From, Elem, To]
  @inline final type CTag[T]                = scala.reflect.ClassTag[T]
  @inline final type Duration               = scala.concurrent.duration.Duration
  @inline final type ExecCtx                = scala.concurrent.ExecutionContext
  @inline final type Failure[+T]            = scala.util.Failure[T]
  @inline final type FiniteDuration         = scala.concurrent.duration.FiniteDuration
  @inline final type Future[+T]             = scala.concurrent.Future[T]
  @inline final type Ref[+A]                = A with scala.AnyRef
  @inline final type Success[+T]            = scala.util.Success[T]
  @inline final type tailrec                = scala.annotation.tailrec
  @inline final type Trav[+T]               = scala.collection.Traversable[T]
  @inline final type TravOnce[+T]           = scala.collection.TraversableOnce[T]
  @inline final type Try[+T]                = scala.util.Try[T]

  @inline final val ->             = scala.Product2
  @inline final val Duration       = scala.concurrent.duration.Duration
  @inline final val ExecCtx        = scala.concurrent.ExecutionContext
  @inline final val Failure        = scala.util.Failure
  @inline final val FiniteDuration = scala.concurrent.duration.FiniteDuration
  @inline final val Future         = scala.concurrent.Future
  @inline final val HALF_UP        = scala.math.BigDecimal.RoundingMode.HALF_UP
  @inline final val NonFatal       = scala.util.control.NonFatal
  @inline final val Success        = scala.util.Success
  @inline final val Trav           = scala.collection.Traversable
  @inline final val TravOnce       = scala.collection.TraversableOnce
  @inline final val Try            = scala.util.Try

  final val ISO_8601_FMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  final val UTC_TZ = java.util.TimeZone getTimeZone "UTC"

  @inline final def idFun[T]: T => T           = t => t
  @inline final def const[T, U](x: T)(y: U): T = x

  @inline final def nanoTime(): Long = java.lang.System.nanoTime

  @inline final def classTag[T: CTag]: CTag[T]      = implicitly[CTag[T]]
//@inline final def classOf[T: CTag]: Class[_ <: T] = classTag[T].runtimeClass.asInstanceOf[Class[_ <: T]]

  @inline final def partial[T, U](pf: T ?=> U): T ?=> U = pf

  @inline final def breakOut[From, T, To](implicit cbf: CBF[Nothing, T, To]) =
    scala.collection.breakOut[From, T, To]

  final def nowIso8601() =
    new java.text.SimpleDateFormat(ISO_8601_FMT) doto (_ setTimeZone UTC_TZ) format new java.util.Date()

  final def timed[T](body: => T): T -> Duration = {
    val t0 = nanoTime()
    val t = body
    val t1 = nanoTime()
    t -> (Duration fromNanos t1 - t0)
  }

  @inline implicit final def DurationInt(n: Int) = scala.concurrent.duration.DurationInt(n)

  @inline implicit final class AnyW[T](private val x: T) {
    @inline def toUnit(): Unit = ()
    @inline def >>(): Unit = println(x)

    @inline def const[S]   :  S => T =  _ => x
    @inline def constThunk : () => T = () => x

    @inline def |>[U](f: T => U): U    = f(x)
    @inline def pipe[U](f: T => U): U  = f(x)
    @inline def sideEffect(u: Unit): T = x
    @inline def doto(f: T => Unit): T  = sideEffect(f(x))

    @inline def requiring(p: T => Boolean): Option[T] = if (p(x)) Some(x) else None
    @inline def isOr(p: T => Boolean)(alt: => T): T   = if (p(x)) x else alt

    @inline def maybe[U](pf: T ?=> U): Option[U]             = pf lift x
    @inline def matchOr[U](alt: => U)(pf: T ?=> U): U        = pf.applyOrElse(x, alt.const)
    @inline def flatMaybe[U](pf: T ?=> Option[U]): Option[U] = x.matchOr(none[U])(pf)
    @inline def maybeUnit(pf: T ?=> Unit): Unit              = x.matchOr(())(pf)

 // @inline def isClass[U: CTag]              = classOf[U] isAssignableFrom x.getClass
 // @inline def castToOpt[U: CTag]: Option[U] = if (x.isClass[U]) Some(x.asInstanceOf[U]) else None
 // @inline def toRef: Ref[T]                 = asInstanceOf[Ref[T]]
 // @inline def isNull: Boolean               = toRef eq null

 // @inline def reflect[B](m: java.lang.reflect.Method)(args: Any*): B =
 //   m.invoke(x, args.map(_.asInstanceOf[AnyRef]): _*).asInstanceOf[B]

    @inline def some : Option[T] = Some(x)
    @inline def opt  : Option[T] = Option(x)

    @inline def left[B]  : T \/ B = Left(x)
    @inline def right[A] : A \/ T = Right(x)

    @inline def list : List[T]   = List(x)
    @inline def vec  : Vector[T] = Vector(x)

    @inline def future: Future[T] = Future successful x
  }

  @inline final def none[T]: Option[T] = None
  @inline final def nil[T]: Seq[T] = Nil

  @inline implicit final class ThrowableW[T <: Throwable](private val t: T) {
    @inline def failFut[U]: Future[U] = Future failed t
  }

  @inline implicit final class IntW(private val i: Int) {
    @inline def bd: BigDecimal = BigDecimal(i)

    /** Integer division, rounding up */
    @inline def divUp(j: Int) = (i + j - 1) / j
  }

  @inline implicit final class LongW(private val l: Long) {
    @inline def bd: BigDecimal = BigDecimal(l)
  }

  @inline implicit final class StringW(private val s: String) {
    @inline def bd: BigDecimal = BigDecimal(s)
  }

  @inline implicit final class BigDecimalW(private val bd: BigDecimal) {
    @inline def divOpt(i: Int): Option[BigDecimal] = if (i == 0) None else Some(bd / i)
    @inline def /?    (i: Int): Option[BigDecimal] = bd divOpt i
  }

  @inline implicit final class TryW[T](private val x: Try[T]) {
    @inline def fold[U](s: T => U, f: Throwable => U): U =
      x match {
        case Success(v) => s(v)
        case Failure(e) => f(e)
      }
    @inline def valueOr[B >: T](f: Throwable => B): B = fold(identity, f)
  }

  @inline implicit final class DurationW(private val d: Duration) {
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

  @inline implicit final class FutureW[T](private val f: Future[T]) {
    @inline def await(atMost: Duration): T = scala.concurrent.Await.result(f, atMost)
    @inline def await5s: T                 = f await 5.seconds
    @inline def await30s: T                = f await 30.seconds
  }

  @inline implicit final class FutureTravOnceW[T, M[X] <: TravOnce[X]](private val f: Future[M[T]]) {
    @inline def foldMap[U >: T](z: U)(op: (U, T) => U)(implicit ec: ExecCtx): Future[U] = foldLeftMap(z)(op)
    @inline def foldLeftMap[U] (z: U)(op: (U, T) => U)(implicit ec: ExecCtx): Future[U] = f map (_.foldLeft(z)(op))
  }

  @inline implicit final class TravOnceW[T, M[X] <: TravOnce[X]](private val xs: M[T]) {
    @inline def traverse[U](f: T => Future[U])(implicit cbf: CBF[M[T], U, M[U]], ec: ExecCtx): Future[M[U]] =
      Future.traverse(xs)(f)
  }

  @inline implicit final class TravOneFutureW[T, M[X] <: TravOnce[X]](private val fs: M[Future[T]]) {
    @inline def sequence(implicit cbf: CBF[M[Future[T]], T, M[T]], ec: ExecCtx): Future[M[T]]      = Future sequence fs
    @inline def firstCompletedOf                         (implicit ec: ExecCtx): Future[T]         = Future firstCompletedOf fs
    @inline def findFut(p: T => Boolean)                 (implicit ec: ExecCtx): Future[Option[T]] = Future.find(fs)(p)
    @inline def foldFut[R](z: R)(op: (R, T) => R)        (implicit ec: ExecCtx): Future[R]         = Future.fold(fs)(z)(op)
  }

  @inline implicit final class IntWithAlign(private val x: Int) {
    @inline def lalign: String = if (x == 0) "%s" else s"%-${x}s"
    @inline def ralign: String = if (x == 0) "%s" else s"%${x}s"
  }
  @inline final def lalign(width: Int): String = width.lalign
  @inline final def ralign(width: Int): String = width.ralign
}
