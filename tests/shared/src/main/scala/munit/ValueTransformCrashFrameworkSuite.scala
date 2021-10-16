package munit

class ValueTransformCrashFrameworkSuite extends munit.FunSuite {
  override val munitValueTransforms: List[ValueTransform] = List(
    new ValueTransform("boom", { case "test-body" => ??? })
  )

  test("hello") {
    "test-body"
  }
}
object ValueTransformCrashFrameworkSuite
    extends FrameworkTest(
      classOf[ValueTransformCrashFrameworkSuite],
      """|==> failure munit.ValueTransformCrashFrameworkSuite.hello - an implementation is missing
         |""".stripMargin
    )
