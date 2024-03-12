package munit

class FailExceptionSuite extends BaseSuite {
  test("assertion-error") {
    val e = intercept[AssertionError] {
      fail("hello world!")
    }
    assert(clue(e).isInstanceOf[Serializable])
  }

  test("assertion-error no exception") {
    try {
      intercept[AssertionError] {
        println("throwing no exception!")
      }
    } catch {
      case e: FailException =>
        assert(
          e.getMessage.contains(
            "expected exception of type 'java.lang.AssertionError' but body evaluated successfully"
          )
        )
    }
  }
}
