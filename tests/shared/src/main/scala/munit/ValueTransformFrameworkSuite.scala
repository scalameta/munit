package munit

import scala.concurrent.Future

class ValueTransformFrameworkSuite extends munit.FunSuite {
  override val munitValueTransforms: List[ValueTransform] = List(
    new ValueTransform("number", { case 42 => Future.failed(new Exception("boom")) })
  )

  test("explode")(42)
  test("ok")(41)
}
object ValueTransformFrameworkSuite
    extends FrameworkTest(
      classOf[ValueTransformFrameworkSuite],
      """|==> failure munit.ValueTransformFrameworkSuite.explode - boom
         |==> success munit.ValueTransformFrameworkSuite.ok
         |""".stripMargin,
    )
