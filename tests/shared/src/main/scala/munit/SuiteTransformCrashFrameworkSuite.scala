package munit

class SuiteTransformCrashFrameworkSuite extends munit.FunSuite {
  override val munitSuiteTransforms: List[SuiteTransform] =
    List(new SuiteTransform("boom", tests => ???))

  test("hello") {}
}
object SuiteTransformCrashFrameworkSuite
    extends FrameworkTest(
      classOf[SuiteTransformCrashFrameworkSuite],
      """|==> failure munit.SuiteTransformCrashFrameworkSuite.munitSuiteTransform - an implementation is missing
         |""".stripMargin,
    )
