package munit.internal

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * This class is not used and should not be used, but it's kept
 * for backward compatibility with munit-scalacheck.
 */
@deprecated
private[munit] object FutureCompat {
  implicit class ExtensionFuture[T](f: Future[T]) {
    @deprecated
    def flattenCompat[S](ec: ExecutionContext)(implicit
        ev: T <:< Future[S]
    ): Future[S] = f.flatten
    @deprecated
    def transformCompat[B](fn: Try[T] => Try[B])(implicit
        ec: ExecutionContext
    ): Future[B] = f.transform(fn)
    @deprecated
    def transformWithCompat[B](fn: Try[T] => Future[B])(implicit
        ec: ExecutionContext
    ): Future[B] = f.transformWith(fn)
  }
}
