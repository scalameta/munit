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
      while (true) {
        def fib(n: Int): Int = {
          if (n < 1) 0
          else if (n == 1) n
          else fib(n - 1) + fib(n - 2)
        }
        // Some computationally intensive calculation
        1.to(1000).foreach(i => fib(i))
        println("Loop")
      }
    }
  }
  test("fast-3") {
    Future {
      Thread.sleep(1)
    }
  }
}
