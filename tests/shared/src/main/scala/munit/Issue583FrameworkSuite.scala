package munit

class Issue583FrameworkSuite extends FunSuite {
  test("simple test") {
    ()
  }
  test("infinite loop test") {
    def loop(x: Int): Int = loop(x) + loop(x)

    loop(0)
  }
  test("another test") {
    ()
  }
}

object Issue583FrameworkSuite
    extends FrameworkTest(
      classOf[Issue583FrameworkSuite],
      """|==> success munit.Issue583FrameworkSuite.simple test
         |==> failure munit.Issue583FrameworkSuite.infinite loop test - null
         |==> success munit.Issue583FrameworkSuite.another test
         |""".stripMargin,
      tags = Set(OnlyJVM)
    )
