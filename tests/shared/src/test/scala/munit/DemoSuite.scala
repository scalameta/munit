package munit

abstract class DemoSuite extends FunSuite {
  // override def munitFlakyOK: Boolean = true
  def someCondition(n: Int): Boolean = n != 2
  test("source-locations") {
    assert(someCondition(1))
    assert(someCondition(2))
    assert(someCondition(3))
  }
  test("diffs") {
    case class User(name: String, age: Int)
    val john = User("John", age = 41)
    val susan = User("Susan", age = 42)
    assertEquals(john, susan)
  }

  test("multiline") {
    val obtained = "val x = 41\nval y = 43\nval z = 43"
    val expected = "val x = 41\nval y = 42\nval z = 43"
    assertNoDiff(obtained, expected)
  }

  test("clue") {
    val a = 42
    val b = 42

    assert(clue(a) < clue(b))
  }
  test("stack-traces".flaky)(
    List(List(1, 2, 3).iterator).iterator.flatten.foreach(i => require(i < 2, i))
  )

  test("flaky".flaky)(???)

}
