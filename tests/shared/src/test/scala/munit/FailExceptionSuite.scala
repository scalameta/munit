package munit

class FailExceptionSuite extends BaseSuite {
  test("assertion-error") {
    intercept[AssertionError] {
      fail("hello world!")
    }
  }
}
