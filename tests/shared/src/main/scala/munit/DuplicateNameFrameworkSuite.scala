package munit

class DuplicateNameFrameworkSuite extends FunSuite {
  def check(name: String)(body: () => Any): Unit = test(name)(body())
  check("a")(() => ())
  check("a")(() => fail("boom"))
  check("a")(() => fail("boom"))
  check("a")(() => ())

  test("POST -> /[type]/[id]") {}
  test("POST -> /[type]/[id]") {}
}

object DuplicateNameFrameworkSuite
    extends FrameworkTest(
      classOf[DuplicateNameFrameworkSuite],
      """|==> success munit.DuplicateNameFrameworkSuite.a
         |==> failure munit.DuplicateNameFrameworkSuite.a-1 - tests/shared/src/main/scala/munit/DuplicateNameFrameworkSuite.scala:6 boom
         |5:  check("a")(() => ())
         |6:  check("a")(() => fail("boom"))
         |7:  check("a")(() => fail("boom"))
         |==> failure munit.DuplicateNameFrameworkSuite.a-2 - tests/shared/src/main/scala/munit/DuplicateNameFrameworkSuite.scala:7 boom
         |6:  check("a")(() => fail("boom"))
         |7:  check("a")(() => fail("boom"))
         |8:  check("a")(() => ())
         |==> success munit.DuplicateNameFrameworkSuite.a-3
         |==> success munit.DuplicateNameFrameworkSuite.POST -> /[type]/[id]
         |==> success munit.DuplicateNameFrameworkSuite.POST -> /[type]/[id]-1
         |""".stripMargin,
    )
