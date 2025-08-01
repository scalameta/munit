package munit

/** Tests that an uncaught exception in afterEach is not swallowed and aborts the suite */
class AfterEachExceptionSuite extends FunSuite {

  override def afterEach(context: AfterEach): Unit =
    throw new RuntimeException("Exception in afterEach")

  test("should run")(assertEquals(1, 1))

  test("should not run")(assertEquals(1, 1))
}
object AfterEachExceptionSuite
    extends FrameworkTest(
      classOf[AfterEachExceptionSuite],
      """==> failure munit.AfterEachExceptionSuite.should run - Teardown of test failed
        |==> skipped munit.AfterEachExceptionSuite.should not run - Suite has been aborted
        |""".stripMargin,
    )
