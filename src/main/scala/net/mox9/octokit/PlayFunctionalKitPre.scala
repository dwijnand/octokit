package net.mox9.octokit

import play.api.libs.functional._
import play.api.libs.functional.syntax._

trait PlayFunctionalKitPre {
  @inline implicit final def seqMonoid[A] = new Monoid[Seq[A]] {
    def identity: Seq[A]                       = Nil
    def append(a1: Seq[A], a2: Seq[A]): Seq[A] = a1 ++ a2
  }

  @inline implicit final def jsResultMonoid[T](implicit M: Monoid[T]) = new Monoid[JsResult[T]] {
    def append(res1: JsResult[T], res2: JsResult[T]): JsResult[T] = {
      (res1, res2) match {
        case (JsSuccess(a, _), JsSuccess(b, _)) => JsSuccess(a |+| b)
        case (JsError(e1), JsError(e2))         => JsError(JsError.merge(e1, e2))
        case (JsError(e), _)                    => JsError(e)
        case (_, JsError(e))                    => JsError(e)
      }
    }
    def identity: JsResult[T] = JsSuccess(M.identity)
  }

  @inline implicit final class TravOnceWithMonoid[T, M[X] <: TravOnce[X]](val xs: M[T]) {
    def foldZ(implicit M: Monoid[T]): T = xs.foldLeft(M.identity)(M.append)
  }

  @inline implicit final class TravOnceFutureWithMonoid[T, M[X] <: TravOnce[X]](private val fs: M[Future[T]]) {
    def foldZ(implicit ec: ExecCtx, M: Monoid[T]): Future[T] = Future.fold(fs)(M.identity)(M.append)
  }

  @inline implicit final class TravOnceWithMonoidFutureOps[T, M[X] <: TravOnce[X]](private val xs: M[T]) {
    def parFoldMap[U: Monoid](f: T => Future[U])(implicit cbf: CBF[M[T], U, M[U]], ec: ExecCtx): Future[U] =
      xs traverse f map (_.foldZ)
    def seqFoldMap[U: Monoid](f: T => Future[U])(implicit cbf: CBF[M[T], U, M[U]], ec: ExecCtx): Future[U] =
      (xs map f).foldZ
  }
}
