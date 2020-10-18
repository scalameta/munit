package munit

import scala.language.strictEquality

class StrictCompareErrorSuite extends BaseSuite {

  test("nested-generic") {
    assertNoDiff(
      compileErrors("""assertNotEquals(List(1), List("1"))"""),
      """|error:
         |no implicit argument of type munit.Compare[List[Int], List[String]] was found for parameter ev of method assertNotEquals in trait Assertions.
         |I found:
         |
         |    munit.Compare.defaultCompareImplicit[List[Int], List[String]](
         |      Eql.eqlSeq[Int, String](/* missing */summon[Eql[Int, String]])
         |    )
         |
         |But no implicit values were found that match type Eql[Int, String].
         |
         |The following import might make progress towards fixing the problem:
         |
         |  import munit.CustomCompare.fromCustomEquality
         |
         |assertNotEquals(List(1), List("1"))
         |                                  ^
         |""".stripMargin
    )
  }

  test("subtype") {
    assertNoDiff(
      compileErrors("""assertNotEquals(Some(1), Option(1))"""),
      """|error:
         |no implicit argument of type munit.Compare[Some[Int], Option[Int]] was found for parameter ev of method assertNotEquals in trait Assertions.
         |I found:
         |
         |    munit.Compare.defaultCompareImplicit[Some[Int], Option[Int]](
         |      /* missing */summon[Eql[Some[Int], Option[Int]]]
         |    )
         |
         |But no implicit values were found that match type Eql[Some[Int], Option[Int]].
         |
         |The following import might fix the problem:
         |
         |  import munit.CustomCompare.fromCustomEquality
         |
         |assertNotEquals(Some(1), Option(1))
         |                                  ^
         |""".stripMargin
    )
  }

  test("supertype") {
    assertNoDiff(
      compileErrors("""assertNotEquals(Option(1), Some(1))"""),
      """|error:
         |no implicit argument of type munit.Compare[Option[Int], Some[Int]] was found for parameter ev of method assertNotEquals in trait Assertions.
         |I found:
         |
         |    munit.Compare.defaultCompareImplicit[Option[Int], Some[Int]](
         |      /* missing */summon[Eql[Option[Int], Some[Int]]]
         |    )
         |
         |But no implicit values were found that match type Eql[Option[Int], Some[Int]].
         |
         |The following import might make progress towards fixing the problem:
         |
         |  import munit.CustomCompare.fromCustomEquality
         |
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
      """|error:
         |no implicit argument of type munit.Compare[A, B] was found for parameter ev of method assertNotEquals in trait Assertions.
         |I found:
         |
         |    munit.Compare.defaultCompareImplicit[A, B](/* missing */summon[Eql[A, B]])
         |
         |But no implicit values were found that match type Eql[A, B].
         |
         |The following import might make progress towards fixing the problem:
         |
         |  import munit.CustomCompare.fromCustomEquality
         |
         |assertNotEquals(new A, new B)
         |                            ^
         |""".stripMargin
    )
  }
}
