package munit

import scala.util.control.NoStackTrace

class SwallowedExceptionSuite extends FunSuite {
  test("issue-51") {
    throw new Exception("i am not reported") with NoStackTrace
  }

  test("issue-650") {
    throw new IllegalAccessError("i am reported")
  }
}
object SwallowedExceptionSuite
    extends FrameworkTest(
      classOf[SwallowedExceptionSuite],
      """|==> failure munit.SwallowedExceptionSuite.issue-51 - i am not reported
         |==> failure munit.SwallowedExceptionSuite.issue-650 - i am reported
         |""".stripMargin
    )
