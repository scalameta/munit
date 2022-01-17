package munit

class Issue478FrameworkSuite extends FunSuite {
  override def munitFlakyOK: Boolean = true
  test("boom".flaky.fail) {}
}

object Issue478FrameworkSuite
    extends FrameworkTest(
      classOf[Issue478FrameworkSuite],
      """|==> skipped munit.Issue478FrameworkSuite.boom - ignoring flaky test failure
         |""".stripMargin
    )
