package munit

class SubtypeCompareErrorSuite extends BaseSuite {
  implicit def equalTypesCompare[A, B](implicit ev: A <:< B): Compare[A, B] =
    Compare.defaultCompare[A, B]
  implicit def supertypeCompare[A, B](implicit ev: A <:< B): Compare[B, A] =
    Compare.defaultCompare[B, A]

  test("subtype") {
    assertEquals(Some(1), Option(1))
    assertEquals(Option(1), Some(1))
  }

  test("equals-diverging".tag(Only213)) {
    assertNoDiff(
      compileErrors("assertEquals(Option(1), Option(1))"),
      """|error:
         |ambiguous implicit values:
         | both method equalTypesCompare in class SubtypeCompareErrorSuite of type [A, B](implicit ev: A <:< B): munit.Compare[A,B]
         | and method supertypeCompare in class SubtypeCompareErrorSuite of type [A, B](implicit ev: A <:< B): munit.Compare[B,A]
         | match expected type munit.Compare[Option[Int],Option[Int]]
         |assertEquals(Option(1), Option(1))
         |            ^
         |""".stripMargin
    )
  }
}
