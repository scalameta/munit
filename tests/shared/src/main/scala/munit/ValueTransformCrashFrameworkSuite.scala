package munit

class ValueTransformCrashFrameworkSuite extends munit.FunSuite {
  override val munitValueTransforms: List[ValueTransform] = List(
    new ValueTransform("boom", { case test => ??? })
  )

  test("hello") {}
}
object ValueTransformCrashFrameworkSuite
    extends FrameworkTest(
      classOf[ValueTransformCrashFrameworkSuite],
      """|==> failure munit.ValueTransformCrashFrameworkSuite.hello - an implementation is missing
         |""".stripMargin
    )
