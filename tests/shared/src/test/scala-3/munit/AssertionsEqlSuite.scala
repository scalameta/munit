package munit

import scala.language.strictEquality

class AssertionsEqlSuite extends BaseSuite {
  test("nested-generic") {
    assertNoDiff(
      compileErrors("""assertNotEquals(List(1), List("1"))"""),
    """|error:
       |Values of types List[Int] and List[String] cannot be compared with == or !=.
       |I found:
       |
       |    Eql.eqlSeq[Int, String](/* missing */summon[Eql[Int, String]])
       |
       |But no implicit values were found that match type Eql[Int, String].
       |assertNotEquals(List(1), List("1"))
       |                                  ^
       |""".stripMargin
    )
  }

  test("subtype") {
    assertNoDiff(
      compileErrors("""assertNotEquals(Some(1), Option(1))"""),
      """|error: Values of types Some[Int] and Option[Int] cannot be compared with == or !=
         |assertNotEquals(Some(1), Option(1))
         |                                  ^
         |""".stripMargin
    )
  }
  test("supertype") {
    assertNoDiff(
      compileErrors("""assertNotEquals(Option(1), Some(1))"""),
      """|error: Values of types Option[Int] and Some[Int] cannot be compared with == or !=
         |assertNotEquals(Option(1), Some(1))
         |                                  ^
         |""".stripMargin
    )
  }
  test("unrelated") {
    class A
    class B
    assertNoDiff(
      compileErrors("""assertNotEquals(new A, new B)"""),
      """|error: Values of types A and B cannot be compared with == or !=
         |assertNotEquals(new A, new B)
         |                            ^
         |""".stripMargin
    )
  }
}
