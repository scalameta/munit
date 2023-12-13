package munit.internal

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext

object FutureCompat {
  implicit class ExtensionFuture[T](f: Future[T]) {
    def flattenCompat[S](
        ec: ExecutionContext
    )(implicit ev: T <:< Future[S]): Future[S] =
      f.flatten
    def transformCompat[B](
        fn: Try[T] => Try[B]
    )(implicit ec: ExecutionContext): Future[B] = {
      f.transform(fn)
    }
    def transformWithCompat[B](
        fn: Try[T] => Future[B]
    )(implicit ec: ExecutionContext): Future[B] = {
      f.transformWith(fn)
    }
  }
}
