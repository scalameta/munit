package munit

class DuplicateNameFrameworkSuite extends FunSuite {
  def check(name: String)(body: () => Any): Unit =
    test(name) {
      body()
    }
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
         |==> failure munit.DuplicateNameFrameworkSuite.a-1 - /scala/munit/DuplicateNameFrameworkSuite.scala:9 boom
         |8:   check("a")(() => ())
         |9:   check("a")(() => fail("boom"))
         |10:  check("a")(() => fail("boom"))
         |==> failure munit.DuplicateNameFrameworkSuite.a-2 - /scala/munit/DuplicateNameFrameworkSuite.scala:10 boom
         |9:   check("a")(() => fail("boom"))
         |10:  check("a")(() => fail("boom"))
         |11:  check("a")(() => ())
         |==> success munit.DuplicateNameFrameworkSuite.a-3
         |==> success munit.DuplicateNameFrameworkSuite.POST -> /[type]/[id]
         |==> success munit.DuplicateNameFrameworkSuite.POST -> /[type]/[id]-1
         |""".stripMargin
    )
