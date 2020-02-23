package munit

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class LazyFutureSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global
  case class LazyFuture[+T](run: () => Future[T])
  object LazyFuture {
    def apply[T](thunk: => T)(implicit ec: ExecutionContext): LazyFuture[T] =
      LazyFuture(() => Future(thunk))
  }

  override def munitTestValue(
      options: TestOptions
  )(testValue: => Any): Future[Any] =
    super.munitTestValue(options)(testValue).flatMap {
      case LazyFuture(run) => run()
      case value           => Future.successful(value)
    }

  test("ok-task".fail) {
    LazyFuture {
      // Test will fail because  LazyFuture.run()` is automatically called
      throw new RuntimeException("BOOM!")
    }
  }
}
