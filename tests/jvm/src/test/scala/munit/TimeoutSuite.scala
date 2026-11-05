package munit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class TimeoutSuite extends munit.FunSuite {
  override val munitTimeout: FiniteDuration = Duration(100, "ms")
  override def munitExecutionContext: ExecutionContext = global
  test("fast-1")(Future(Thread.sleep(1)))
  test("slow".fail)(Future(Thread.sleep(1000)))
  test("fast-2")(Future(Thread.sleep(1)))
  // With one CPU, the global pool has one thread: the loop must not take it.
  private val loopThread = ExecutionContext.fromExecutor { r =>
    val t = new Thread(r)
    t.setDaemon(true)
    t.start()
  }
  test("infinite-loop".fail)(Future(ThrottleCpu.run())(loopThread))
  test("fast-3")(Future(Thread.sleep(1)))
  // NOTE(olafurpg): The test below times out on CI but not on my local Macbook
  // test("slow-non-future".fail) {
  //   ThrottleCpu.run()
  // }
  test("slow-non-future-sleep".fail)(Thread.sleep(1000))
  test("fast-4")(Future(Thread.sleep(1)))
}
