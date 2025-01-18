package munit

class TestTransformCrashFrameworkSuite extends munit.FunSuite {
  override val munitTestTransforms: List[TestTransform] =
    List(new TestTransform("boom", test => ???))

  test("hello") {}
}
object TestTransformCrashFrameworkSuite
    extends FrameworkTest(
      classOf[TestTransformCrashFrameworkSuite],
      """|==> failure munit.TestTransformCrashFrameworkSuite.hello - an implementation is missing
         |""".stripMargin,
    )
