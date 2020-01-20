package munit

class FrameworkSuite extends BaseFrameworkSuite {
  val tests: List[FrameworkTest] = List[FrameworkTest](
    InterceptFrameworkSuite,
    CiOnlyFrameworkSuite,
    DiffProductFrameworkSuite,
    FailFrameworkSuite,
    DuplicateNameFrameworkSuite,
    ScalaVersionFrameworkSuite,
    FixtureFrameworkSuite
  )
  tests.foreach { t =>
    check(t)
  }
}
