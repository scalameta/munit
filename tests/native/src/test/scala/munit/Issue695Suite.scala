package munit

import scala.concurrent._

class Issue695Suite extends FunSuite {
  override def munitExecutionContext = ExecutionContext.global

  test("await task on global EC") {
    val p = Promise[Unit]()
    ExecutionContext.global.execute { () =>
      Thread.sleep(1000)
      p.success(())
    }
    p.future
  }

}
