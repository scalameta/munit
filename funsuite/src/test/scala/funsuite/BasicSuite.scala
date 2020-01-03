package funsuite

import org.junit.runner.RunWith

class BasicSuite extends FunSuite {
  def assertNoDiff()(implicit loc: Location): Unit = {
    assert(false)
  }
  def check(a: String, b: String)(implicit loc: Location): Unit = {
    assertNoDiff(a, b)
  }
  test("fail".fail) {}
  test("assume") {
    assume(false, "just because")
    ???
  }
  test("not-implemented") {
    assertEqual(1, 2)
    assertNoDiff()
  }
  case class User(name: String, age: Int, zips: List[Int])
  test("pass") {
    val john = User("John", 42, 1.to(10).toList)
    val john2 = User("John", 43, 2.to(10).toList)
    assertEqual(42, 53)
    assertEqual(john2, john)
  }
  test("assertSame") {
    check(
      """|
         |val x = 42
         |val y = 42
         |""".stripMargin,
      """|
         |val x = 43
         |val y = 42
         |""".stripMargin
    )
  }
}
