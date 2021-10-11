package munit

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class TimeoutSuite extends munit.FunSuite {
  override val munitTimeout: FiniteDuration = Duration(100, "ms")
  test("slow".fail) {
    Future {
      Thread.sleep(1000)
    }
  }
  test("fast") {
    Future {
      Thread.sleep(1)
    }
  }
}
