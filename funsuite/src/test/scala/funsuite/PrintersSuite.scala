package funsuite

import funsuite.internal.Printers

class PrintersSuite extends FunSuite {
  def check(name: String, original: Any, expected: String): Unit = {
    test(name) {
      val obtained = Printers.print(original)
      assertNoDiff(obtained, expected)
    }
  }

  check(
    "basic",
    "a",
    "\"a\""
  )

  check(
    "multiline",
    "a\n",
    "\"\"\"a\n\"\"\""
  )
  check(
    "single-quote",
    '\'',
    "'\\''"
  )
  check(
    "newline",
    '\n',
    "'\\n'"
  )
  check(
    "map",
    Map(1 -> 2, 3 -> 4, 5 -> Map(6 -> 7)),
    """|Map(
       |  1 -> 2,
       |  3 -> 4,
       |  5 -> Map(
       |    6 -> 7
       |  )
       |)
       |""".stripMargin
  )

  check(
    "list",
    List(1, 2, 3, List(4, 5, List(6, 7))),
    """|List(
       |  1,
       |  2,
       |  3,
       |  List(
       |    4,
       |    5,
       |    List(
       |      6,
       |      7
       |    )
       |  )
       |)
       |""".stripMargin
  )

  check(
    "array",
    Array(1, 2, 3, Array(4, 5, Array(6, 7))),
    """|Array(
       |  1,
       |  2,
       |  3,
       |  Array(
       |    4,
       |    5,
       |    Array(
       |      6,
       |      7
       |    )
       |  )
       |)
       |""".stripMargin
  )

  case class User(name: String, age: Int, friends: List[User] = Nil)
  check(
    "user1",
    User("John", 42, Nil),
    """|User(
       |  name = "John",
       |  age = 42,
       |  friends = List()
       |)
       |""".stripMargin
  )

  check(
    "user2",
    User("John", 42, List(User("Susan", 43))),
    """|User(
       |  name = "John",
       |  age = 42,
       |  friends = List(
       |    User(
       |      name = "Susan",
       |      age = 43,
       |      friends = List()
       |    )
       |  )
       |)
       |""".stripMargin
  )
}
