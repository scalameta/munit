package munit

class FrameworkSuite extends BaseFrameworkSuite {
  val tests: List[FrameworkTest] = List[FrameworkTest](
    InterceptFrameworkSuite,
    CiOnlyFrameworkSuite,
    DiffProductFrameworkSuite,
    FailFrameworkSuite,
    DuplicateNameFrameworkSuite
  )
  tests.foreach { t =>
    check(t.cls, t.expected)(t.location)
  }
}
