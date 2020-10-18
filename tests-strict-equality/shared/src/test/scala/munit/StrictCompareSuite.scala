package munit

class StrictCompareSuite extends BaseSuite {
  implicit def equalTypesCompare[A, B](implicit ev: A =:= B): Compare[A, B] =
    Compare.defaultCompare[A, B]

  test("basic") {
    assertEquals(Option(1), Option(1))
    assertNotEquals(Option(2), Option(1))
  }

}
