package munit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FutureSuite extends FunSuite {
  test("nested".fail)(Future {
    Thread.sleep(2)
    Future {
      Thread.sleep(2)
      Future {
        Thread.sleep(2)
        ???
      }
    }
  })
}
