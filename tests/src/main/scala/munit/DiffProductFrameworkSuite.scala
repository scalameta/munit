package munit

import scala.util.Properties

class DiffProductFrameworkSuite extends FunSuite {

  case class User(name: String, age: Int, friends: List[Int])
  test("pass") {
    val john = User("John", 42, 1.to(2).toList)
    val john2 = User("John", 43, 2.to(2).toList)
    assertEqual(john2, john)
  }

}
