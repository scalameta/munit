package munit

import scala.util.control.NoStackTrace

class SwallowedExceptionSuite extends FunSuite {
  test("issue-51") {
    throw new Exception("i am not reported") with NoStackTrace
  }

  test("issue-650") {
    throw new IllegalAccessError("i am reported")
  }

  test("should not be executed") {
    assertEquals(1, 1)
  }
}
object SwallowedExceptionSuite
    extends FrameworkTest(
      classOf[SwallowedExceptionSuite],
      """|==> failure munit.SwallowedExceptionSuite.issue-51 - i am not reported
         |==> failure munit.SwallowedExceptionSuite.issue-650 - i am reported
         |==> skipped munit.SwallowedExceptionSuite.should not be executed - Suite has been aborted
         |""".stripMargin
    )
