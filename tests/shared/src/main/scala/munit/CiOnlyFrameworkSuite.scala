package munit

class CiOnlyFrameworkSuite extends FunSuite {
  override def isCI: Boolean = true
  test("only".only)(println("pass"))
  test("boom")(???)
}

object CiOnlyFrameworkSuite
    extends FrameworkTest(
      classOf[CiOnlyFrameworkSuite],
      """|==> failure munit.CiOnlyFrameworkSuite.only - tests/shared/src/main/scala/munit/CiOnlyFrameworkSuite.scala:5 'Only' tag is not allowed when `isCI=true`
         |4:  override def isCI: Boolean = true
         |5:  test("only".only)(println("pass"))
         |6:  test("boom")(???)
         |""".stripMargin,
    )
