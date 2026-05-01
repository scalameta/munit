package munit

class FixtureEnumerationCrashFrameworkSuite extends munit.FunSuite {
  override def munitFixtures: Seq[AnyFixture[_]] =
    throw new RuntimeException("boom")

  test("hello") {}
}
object FixtureEnumerationCrashFrameworkSuite
    extends FrameworkTest(
      classOf[FixtureEnumerationCrashFrameworkSuite],
      """|==> failure munit.FixtureEnumerationCrashFrameworkSuite.unexpected error running tests - boom
         |""".stripMargin,
    )
