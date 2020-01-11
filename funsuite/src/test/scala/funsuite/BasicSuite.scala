package funsuite

@Ignore
class BasicSuite extends FunSuite {
  override def isCI = true
  override def isFlakyFailureOk = true
  def assertNoDiff()(implicit loc: Location): Unit = {
    assert(false)
  }
  def check(a: String, b: String)(implicit loc: Location): Unit = {
    assertNoDiff(a, b, "check failed")
  }

  test("fail") {
    Thread.sleep(1000)
  }
  test("ignore".ignore) {
    ???
  }
  test("assume") {
    assume(false, "just because")
    ???
  }

  test("not-implemented") {
    List(List(0, 1, 2)).iterator.flatten.foreach { i =>
      assertEqual(i, 0)
    }
    assertNoDiff()
  }
  case class User(name: String, age: Int, friends: List[Int])
  test("pass") {
    val john = User("John", 42, 1.to(4).toList)
    val john2 = User("John", 43, 2.to(5).toList)
    assertEqual(john2, john)
  }

  test("only") {
    assertNoDiff(
      """|val x = 42
         |val y = 42
         |""".stripMargin,
      """|
         |val x = 43
         |val y = 42
         |""".stripMargin
    )
  }
}
