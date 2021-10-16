package munit

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext

class TimeoutSuite extends munit.FunSuite {
  override val munitTimeout: FiniteDuration = Duration(100, "ms")
  override def munitExecutionContext: ExecutionContext = global
  test("fast-1") {
    Future {
      Thread.sleep(1)
    }
  }
  test("slow".fail) {
    Future {
      Thread.sleep(1000)
    }
  }
  test("fast-2") {
    Future {
      Thread.sleep(1)
    }
  }
  test("infinite-loop".fail) {
    Future {
      ThrottleCpu.run()
    }
  }
  test("fast-3") {
    Future {
      Thread.sleep(1)
    }
  }
  test("slow-non-future".fail) {
    ThrottleCpu.run()
  }
  test("slow-non-future-sleep".fail) {
    Thread.sleep(1000)
  }
  test("fast-4") {
    Future {
      Thread.sleep(1)
    }
  }
}
