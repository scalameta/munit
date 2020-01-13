package munit

class FailFrameworkSuite extends FunSuite {
  test("pass".fail) {
    // println("pass")
  }
  test("fail".fail) {
    ???
  }
}
