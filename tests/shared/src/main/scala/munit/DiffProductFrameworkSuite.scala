package munit

class DiffProductFrameworkSuite extends FunSuite {

  case class User(name: String, age: Int, friends: List[Int])
  test("pass") {
    val john = User("John", 42, 1.to(2).toList)
    val john2 = User("John", 43, 2.to(2).toList)
    assertEquals(john2, john)
  }

}

object DiffProductFrameworkSuite
    extends FrameworkTest(
      classOf[DiffProductFrameworkSuite],
      """|==> failure munit.DiffProductFrameworkSuite.pass - tests/shared/src/main/scala/munit/DiffProductFrameworkSuite.scala:9
         |8:     val john2 = User("John", 43, 2.to(2).toList)
         |9:     assertEquals(john2, john)
         |10:  }
         |values are not the same
         |=> Obtained
         |User(
         |  name = "John",
         |  age = 43,
         |  friends = List(
         |    2
         |  )
         |)
         |=> Diff (- expected, + obtained)
         |   name = "John",
         |-  age = 42,
         |+  age = 43,
         |   friends = List(
         |-    1,
         |     2
         |""".stripMargin,
    )
