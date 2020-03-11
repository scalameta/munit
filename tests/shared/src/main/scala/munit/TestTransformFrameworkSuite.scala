package munit

class TestTransformFrameworkSuite extends munit.FunSuite {
  override val munitTestTransforms: List[TestTransform] = List(
    new TestTransform("ok", test => test.withName(test.name + "-ok"))
  )

  test("hello") {}
}
object TestTransformFrameworkSuite
    extends FrameworkTest(
      classOf[TestTransformFrameworkSuite],
      """|==> success munit.TestTransformFrameworkSuite.hello-ok
         |""".stripMargin
    )
