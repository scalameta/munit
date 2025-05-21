package munit

import munit.internal.FutureCompat._
import munit.internal.console.StackTraces

import scala.concurrent.Future
import scala.util.Try

trait ValueTransforms {
  this: BaseFunSuite =>

  final class ValueTransform(
      val name: String,
      fn: PartialFunction[Any, Future[Any]],
  ) extends Function1[Any, Option[Future[Any]]] {
    def apply(v1: Any): Option[Future[Any]] = fn.lift(v1)
  }

  def munitValueTransforms: List[ValueTransform] = List(munitFutureTransform)

  final def munitValueTransform(testValue: => Any): Future[Any] = {
    // Takes an arbitrarily nested future `Future[Future[Future[...]]]` and
    // returns a `Future[T]` where `T` is not a `Future`.
    def flattenFuture(future: Future[_]): Future[_] = future.map { value =>
      val transformed = munitValueTransforms.iterator.map(fn => fn(value))
        .collectFirst { case Some(future) => future }
      transformed match {
        case Some(f) => flattenFuture(f)
        case None => Future.successful(value)
      }
    }(munitExecutionContext).flattenCompat(munitExecutionContext)
    val wrappedFuture = Future.fromTry(Try(StackTraces.dropOutside(testValue)))
    flattenFuture(wrappedFuture)
  }

  final def munitFutureTransform: ValueTransform =
    new ValueTransform("Future", { case e: Future[_] => e })
}
