package funsuite

import org.junit.runner.RunWith
import scala.util.Properties

class BasicSuite extends FunSuite {
  override def isCI = true
  override def isFlakyFailureOk = true
  def assertNoDiff()(implicit loc: Location): Unit = {
    assert(false)
  }
  def check(a: String, b: String)(implicit loc: Location): Unit = {
    assertNoDiff(a, b, "check failed")
  }
  test("fail".flaky) {
    ???
  }
  test("ignore".ignore) {
    ???
  }
  test("assume") {
    assume(false, "just because")
    ???
  }

  test("paths") {
    assume(Properties.isLinux, "this test runs only on Linux")
    // Linux-specific assertions
  }
  test("not-implemented") {
    List(0, 1, 2).foreach { i =>
      assertEqual(i, 0)
    }
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
