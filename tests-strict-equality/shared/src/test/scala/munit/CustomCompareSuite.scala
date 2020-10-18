package munit

class CustomCompareSuite extends BaseSuite {
  import CustomCompare.fromCustomEquality
  test("ok") {
    assertEquals(Some(1), Option(1))
  }

  test("boom") {
    interceptMessage[RuntimeException]("boom")(
      assertEquals(Some(42), Option(42))
    )
  }
}
