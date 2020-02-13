package munit

class InterceptException extends Exception("Foo")
class InterceptFrameworkSuite extends FunSuite {
  test("not-implemented") {
    intercept[NotImplementedError](???)
  }
  test("type-mismatch") {
    intercept[InterceptException](???)
  }
  test("intercept-message-match") {
    interceptMessage[NotImplementedError]("boom") { ??? }
  }
}

object InterceptFrameworkSuite
    extends FrameworkTest(
      classOf[InterceptFrameworkSuite],
      """|==> success munit.InterceptFrameworkSuite.not-implemented
         |==> failure munit.InterceptFrameworkSuite.type-mismatch - intercept failed, exception 'scala.NotImplementedError' is not a subtype of 'munit.InterceptException
         |==> failure munit.InterceptFrameworkSuite.intercept-message-match - intercept failed, exception 'scala.NotImplementedError' had message 'an implementation is missing', which was different from expected message 'boom'
         |""".stripMargin
    )
