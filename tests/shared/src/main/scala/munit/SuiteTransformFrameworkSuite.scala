package munit

class SuiteTransformFrameworkSuite extends munit.FunSuite {
  override val munitSuiteTransforms: List[SuiteTransform] = List(
    new SuiteTransform(
      "hello",
      tests => tests.filter(_.name.startsWith("hello"))
    )
  )

  test("hello") {}
  test("hello-yes") {}
  test("goodbye") {}
  test("goodbye-yes") {}
}
object SuiteTransformFrameworkSuite
    extends FrameworkTest(
      classOf[SuiteTransformFrameworkSuite],
      """|==> success munit.SuiteTransformFrameworkSuite.hello
         |==> success munit.SuiteTransformFrameworkSuite.hello-yes
         |""".stripMargin
    )
