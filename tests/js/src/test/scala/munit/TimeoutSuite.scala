package munit

import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.scalajs.js.timers._

class TimeoutSuite extends BaseSuite {
  override def munitTimeout: Duration = Duration(3, "ms")
  test("setTimeout-exceeds".fail) {
    val promise = Promise[Unit]()
    setTimeout(1000)(promise.success(()))
    promise.future
  }
  test("setTimeout-passes") {
    val promise = Promise[Unit]()
    setTimeout(1)(promise.success(()))
    promise.future
  }

  // We can't use an infinite loop because it blocks the main thread preventing the test from completing.
  //   test("infinite-loop".fail) {
  //     ThrottleCpu.run()
  //   }
}
