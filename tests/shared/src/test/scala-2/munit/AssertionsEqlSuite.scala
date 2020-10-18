package munit

class AssertionsEqlSuite extends BaseSuite {
  test("basic".fail) {
    assertEquals(List("1"), List(1))
  }
}
