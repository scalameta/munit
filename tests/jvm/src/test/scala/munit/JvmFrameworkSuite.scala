package munit

class JvmFrameworkSuite extends BaseFrameworkSuite {
  val tests: List[FrameworkTest] = List[FrameworkTest](
    Issue1002AfterAllFrameworkSuite,
    Issue1002TeardownFrameworkSuite,
  )
  tests.foreach(t => check(t))
}
