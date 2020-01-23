package munit

class TagsFrameworkSuite extends munit.FunSuite {
  val include = new Tag("include")
  val exclude = new Tag("exclude")
  test("a".tag(include)) {}
  test("b".tag(exclude)) {}
  test("c".tag(include).tag(exclude)) {}
  test("d") {}
}
object TagsIncludeFramweworkSuite
    extends FrameworkTest(
      classOf[TagsFrameworkSuite],
      """|==> success munit.TagsFrameworkSuite.a
         |==> success munit.TagsFrameworkSuite.c
         |""".stripMargin,
      arguments = Array("--include-tags=include")
    )

object TagsIncludeExcludeFramweworkSuite
    extends FrameworkTest(
      classOf[TagsFrameworkSuite],
      """|==> success munit.TagsFrameworkSuite.a
         |""".stripMargin,
      arguments = Array("--include-tags=include", "--exclude-tags=exclude")
    )

object TagsExcludeFramweworkSuite
    extends FrameworkTest(
      classOf[TagsFrameworkSuite],
      """|==> success munit.TagsFrameworkSuite.a
         |==> success munit.TagsFrameworkSuite.d
         |""".stripMargin,
      arguments = Array("--exclude-tags=exclude")
    )
