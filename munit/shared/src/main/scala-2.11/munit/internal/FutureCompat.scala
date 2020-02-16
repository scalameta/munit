package munit.internal

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise

object FutureCompat {
  implicit class ExtensionFuture[T](f: Future[T]) {
    def flattenCompat[S](
        ec: ExecutionContext
    )(implicit ev: T <:< Future[S]): Future[S] =
      f.flatMap(ev)(ec)
    def transformCompat[B](
        fn: Try[T] => Try[B]
    )(implicit ec: ExecutionContext): Future[B] = {
      val p = Promise[B]()
      f.onComplete { t =>
        p.complete(fn(t))
      }
      p.future
    }
  }
}
