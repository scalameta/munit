package munit

class TestTransformFrameworkSuite extends munit.FunSuite {
  override val munitTestTransforms: List[TestTransform] = List(
    new TestTransform("ok", test => if (test.name == "hello") test.withName(test.name + "-ok") else test),
    munitAppendToFailureMessage(t => if (t.name.startsWith("suffix")) Some("==> extra info") else None)
  )

  test("hello") {}

  test("suffix-success") {}
  test("suffix-fail") {
    fail("boom")
  }
  test("suffix-assertEquals") {
    assertEquals(0, 1)
  }
}
object TestTransformFrameworkSuite
    extends FrameworkTest(
      classOf[TestTransformFrameworkSuite],
      """|==> success munit.TestTransformFrameworkSuite.hello-ok
         |==> success munit.TestTransformFrameworkSuite.suffix-success
         |==> failure munit.TestTransformFrameworkSuite.suffix-fail - /scala/munit/TestTransformFrameworkSuite.scala:13 boom
         |12:  test("suffix-fail") {
         |13:    fail("boom")
         |14:  }
         |==> extra info
         |==> failure munit.TestTransformFrameworkSuite.suffix-assertEquals - /scala/munit/TestTransformFrameworkSuite.scala:16
         |15:  test("suffix-assertEquals") {
         |16:    assertEquals(0, 1)
         |17:  }
         |values are not the same
         |=> Obtained
         |0
         |=> Diff (- obtained, + expected)
         |-0
         |+1
         |==> extra info
         |""".stripMargin
    )
