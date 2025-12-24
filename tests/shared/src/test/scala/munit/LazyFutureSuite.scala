package munit

import munit.internal.PlatformCompat

import scala.concurrent.{ExecutionContext, Future}

class LazyFutureSuite extends FunSuite {
  implicit val ec: ExecutionContext = PlatformCompat.executionContext
  case class LazyFuture[+T](run: () => Future[T])
  object LazyFuture {
    def apply[T](thunk: => T)(implicit ec: ExecutionContext): LazyFuture[T] =
      LazyFuture(() => Future(thunk))
  }

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms ++
      List(new ValueTransform("LazyFuture", { case LazyFuture(run) => run() }))

  test("ok-task".fail)(LazyFuture(
    // Test will fail because  LazyFuture.run()` is automatically called
    throw new RuntimeException("BOOM!")
  ))

  test("nested".fail)(LazyFuture(LazyFuture(
    // Test will fail because  LazyFuture.run()` is automatically called
    throw new RuntimeException("BOOM!")
  )))
}
