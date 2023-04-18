package munit

class FrameworkSuite extends BaseFrameworkSuite {
  val tests: List[FrameworkTest] = List[FrameworkTest](
    SwallowedExceptionSuite,
    InterceptFrameworkSuite,
    CiOnlyFrameworkSuite,
    DiffProductFrameworkSuite,
    FailFrameworkSuite,
    FailSuiteFrameworkSuite,
    TestNameFrameworkSuite,
    ScalaVersionFrameworkSuite,
    FixtureOrderFrameworkSuite,
    TagsIncludeFramweworkSuite,
    TagsIncludeExcludeFramweworkSuite,
    TagsExcludeFramweworkSuite,
    SuiteTransformCrashFrameworkSuite,
    SuiteTransformFrameworkSuite,
    TestTransformCrashFrameworkSuite,
    TestTransformFrameworkSuite,
    ValueTransformCrashFrameworkSuite,
    ValueTransformFrameworkSuite,
    ScalaCheckFrameworkSuite,
    AsyncFunFixtureFrameworkSuite,
    AsyncFixtureTeardownFrameworkSuite,
    DuplicateNameFrameworkSuite,
    FullStackTraceFrameworkSuite,
    SmallStackTraceFrameworkSuite,
    AssertionsFrameworkSuite,
    Issue179FrameworkSuite,
    Issue285FrameworkSuite,
    Issue497FrameworkSuite,
    Issue583FrameworkSuite,
    ScalaCheckExceptionFrameworkSuite,
    BoxedFrameworkSuite,
    SkippedFrameworkSuite
  )
  tests.foreach { t => check(t) }
}
