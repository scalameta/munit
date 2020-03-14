package munit

import scala.concurrent.Future
import munit.internal.FutureCompat._
import scala.util.Try
import munit.internal.console.StackTraces
import munit.internal.PlatformCompat
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

trait ValueTransforms { this: FunSuite =>

  final class ValueTransform(
      val name: String,
      fn: PartialFunction[Any, Future[Any]]
  ) extends Function1[Any, Option[Future[Any]]] {
    def apply(v1: Any): Option[Future[Any]] = fn.lift(v1)
  }

  def munitValueTransforms: List[ValueTransform] =
    List(
      munitFutureTransform
    )

  def munitTimeout: Duration = new FiniteDuration(30, TimeUnit.SECONDS)
  final def munitValueTransform(testValue: => Any): Future[Any] = {
    // Takes an arbitrarily nested future `Future[Future[Future[...]]]` and
    // returns a `Future[T]` where `T` is not a `Future`.
    def flattenFuture(future: Future[_]): Future[_] = {
      val nested: Future[Future[Any]] = future.map { value =>
        val transformed = munitValueTransforms.iterator
          .map(fn => fn(value))
          .collectFirst { case Some(future) => future }
        transformed match {
          case Some(f) => flattenFuture(f)
          case None    => Future.successful(value)
        }
      }(munitExecutionContext)
      nested.flattenCompat(munitExecutionContext)
    }
    val wrappedFuture = Future.fromTry(Try(StackTraces.dropOutside(testValue)))
    val flatFuture = flattenFuture(wrappedFuture)
    val awaitedFuture = PlatformCompat.waitAtMost(flatFuture, munitTimeout)
    awaitedFuture
  }

  final def munitFutureTransform: ValueTransform =
    new ValueTransform("Future", { case e: Future[_] => e })
}
