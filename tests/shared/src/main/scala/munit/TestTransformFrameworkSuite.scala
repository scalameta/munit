package munit

class TestTransformFrameworkSuite extends munit.FunSuite {
  override val munitTestTransforms: List[TestTransform] = List(
    new TestTransform(
      "ok",
      test =>
        if (test.name == "hello") test.withName(test.name + "-ok") else test,
    ),
    munitAppendToFailureMessage(t =>
      if (t.name.startsWith("suffix")) Some("==> extra info") else None
    ),
  )

  test("hello") {}

  test("suffix-success") {}
  test("suffix-fail")(fail("boom"))
  test("suffix-assertEquals")(assertEquals(0, 1))
}
object TestTransformFrameworkSuite
    extends FrameworkTest(
      classOf[TestTransformFrameworkSuite],
      """|==> success munit.TestTransformFrameworkSuite.hello-ok
         |==> success munit.TestTransformFrameworkSuite.suffix-success
         |==> failure munit.TestTransformFrameworkSuite.suffix-fail - tests/shared/src/main/scala/munit/TestTransformFrameworkSuite.scala:18 boom
         |17:  test("suffix-success") {}
         |18:  test("suffix-fail")(fail("boom"))
         |19:  test("suffix-assertEquals")(assertEquals(0, 1))
         |==> extra info
         |==> failure munit.TestTransformFrameworkSuite.suffix-assertEquals - tests/shared/src/main/scala/munit/TestTransformFrameworkSuite.scala:19
         |18:  test("suffix-fail")(fail("boom"))
         |19:  test("suffix-assertEquals")(assertEquals(0, 1))
         |20:}
         |values are not the same
         |=> Obtained
         |0
         |=> Diff (- obtained, + expected)
         |-0
         |+1
         |==> extra info
         |""".stripMargin,
    )
