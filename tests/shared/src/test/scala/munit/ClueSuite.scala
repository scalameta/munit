package munit

class ClueSuite extends BaseSuite {
  def check[T](options: TestOptions, clue: Clue[T], expected: String): Unit = {
    test(options) {
      assertEquals(clue.source, expected)
      println(clue.valueType)
    }
  }

  val a: List[Int] = List(1)

  check("identifier", a, "a")
  check("select", a.head, "a.head")
  check("comment", a /*comment*/ .head, "a /*comment*/ .head")

  // Disabled on Dotty because the starting position doesn't include opening "("
  check("lambda".tag(NoDotty), { (y: String) =>
    y.head
  }, "(y: String) =>\n    y.head")

  checkPrint(
    "string-message",
    clues("message"),
    """|Clues {
       |  "message": "message"
       |}
       |""".stripMargin
  )

  val x = 42
  checkPrint(
    "clue",
    clues(x),
    """|Clues {
       |  x: 42
       |}
       |""".stripMargin
  )

  val y = 32
  checkPrint(
    "clues",
    clues(x, y),
    """|Clues {
       |  x: 42
       |  y: 32
       |}
       |""".stripMargin
  )

  val z: List[Int] = List(1)
  checkPrint(
    "list",
    clues(z),
    """|Clues {
       |  z: List(
       |    1
       |  )
       |}
       |""".stripMargin
  )

  case class User(name: String, age: Int)
  val user: User = User("Tanya", 34)
  checkPrint(
    "product".tag(Only213),
    clues(user),
    """|Clues {
       |  user: User(
       |    name = "Tanya",
       |    age = 34
       |  )
       |}
       |""".stripMargin
  )

  def checkPrint(
      options: TestOptions,
      clues: Clues,
      expected: String
  )(implicit loc: Location): Unit = {
    test(options) {
      val obtained = munitPrint(clues)
      assertNoDiff(obtained, expected)
    }
  }

}
