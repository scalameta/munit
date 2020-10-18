package munit

class FailExceptionSuite extends BaseSuite {
  test("assertion-error") {
    val error: AssertionError = new FailException("", Location.generate)
  }
}
