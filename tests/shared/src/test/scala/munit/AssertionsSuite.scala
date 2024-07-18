package munit

import munit.internal.console.Printers

class AssertionsSuite extends BaseSuite {
  def check(
      name: TestOptions,
      cond: => Boolean,
      expected: String
  )(implicit loc: Location): Unit =
    test(name) {
      val (_, clues) = munitCaptureClues(cond)
      assertNoDiff(Printers.print(clues), expected)
    }

  val a = 42
  val b = 43L
  val c: List[Int] = List(41)
  check(
    "basic",
    clue(a) > clue(b),
    """|Clues {
       |  a: Int = 42
       |  b: Long = 43
       |}
       |""".stripMargin
  )

  check(
    "expr",
    clue(a) > clue(c.head),
    """|Clues {
       |  a: Int = 42
       |  c.head: Int = 41
       |}
       |""".stripMargin
  )

  check(
    "subexpr",
    clue(a) > clue(c).head,
    """|Clues {
       |  a: Int = 42
       |  c: List[Int] = List(
       |    41
       |  )
       |}
       |""".stripMargin
  )

  test("subtype".tag(NoDotty)) {
    assertEquals(Option(1), Some(1))
    assertEquals(Some(1), Option(1))
    assertEquals(Option(1), Option(1))
  }

  test("false-negative") {
    assume(BuildInfo.scalaVersion != "3.3.3")
    assertNoDiff(
      compileErrors("assertEquals(List(1), Vector(1))"),
      if (isDotty)
        """|error:
           |Can't compare these two types:
           |  First type:  List[Int]
           |  Second type: Vector[Int]
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[List[Int], Vector[Int]]
           |  Alternative 2: upcast either type into `Any` or a shared supertype.
           |I found:
           |
           |    munit.Compare.compareSubtypeWithSupertype[List[Int], Vector[Int]](
           |      /* missing */summon[List[Int] <:< Vector[Int]])
           |
           |But no implicit values were found that match type List[Int] <:< Vector[Int].
           |assertEquals(List(1), Vector(1))
           |           ^
           |""".stripMargin
      else
        """|error:
           |Can't compare these two types:
           |  First type:  List[Int]
           |  Second type: scala.collection.immutable.Vector[Int]
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[List[Int], scala.collection.immutable.Vector[Int]]
           |  Alternative 2: upcast either type into `Any` or a shared supertype
           |assertEquals(List(1), Vector(1))
           |            ^
           |""".stripMargin
    )
  }

  test("unrelated") {
    assume(BuildInfo.scalaVersion != "3.3.3")
    assertNoDiff(
      compileErrors("""
class A {
  override def equals(x: Any): Boolean = true
}
class B {
  override def equals(x: Any): Boolean = true
}
assertEquals(new A, new B)
      """),
      if (isDotty)
        """|error:
           |Can't compare these two types:
           |  First type:  A
           |  Second type: B
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[A, B]
           |  Alternative 2: upcast either type into `Any` or a shared supertype.
           |I found:
           |
           |    munit.Compare.compareSubtypeWithSupertype[A, B](/* missing */summon[A <:< B])
           |
           |But no implicit values were found that match type A <:< B.
           |assertEquals(new A, new B)
           |           ^
           |""".stripMargin
      else
        """|error:
           |Can't compare these two types:
           |  First type:  A
           |  Second type: B
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[A, B]
           |  Alternative 2: upcast either type into `Any` or a shared supertype
           |assertEquals(new A, new B)
           |            ^
           |""".stripMargin
    )
  }

  test("char-int-nok") {
    assume(BuildInfo.scalaVersion != "3.3.3")
    assertNoDiff(
      compileErrors("assertEquals('a', 'a'.toInt)"),
      if (isDotty)
        """|error:
           |Can't compare these two types:
           |  First type:  Char
           |  Second type: Int
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[Char, Int]
           |  Alternative 2: upcast either type into `Any` or a shared supertype.
           |I found:
           |
           |    munit.Compare.compareSubtypeWithSupertype[Char, Int](
           |      /* missing */summon[Char <:< Int])
           |
           |But no implicit values were found that match type Char <:< Int.
           |assertEquals('a', 'a'.toInt)
           |           ^
           |""".stripMargin
      else
        """|error:
           |Can't compare these two types:
           |  First type:  Char
           |  Second type: Int
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[Char, Int]
           |  Alternative 2: upcast either type into `Any` or a shared supertype
           |assertEquals('a', 'a'.toInt)
           |            ^
           |""".stripMargin
    )
  }

  test("array-sameElements") {
    val e = intercept[ComparisonFailException] {
      assertEquals(Array(1, 2), Array(1, 2))
    }
    assert(
      clue(e).getMessage.contains(
        "arrays have the same elements but different reference equality. Convert the arrays to a non-Array collection if you intend to assert the two arrays have the same elements. For example, `assertEquals(a.toSeq, b.toSeq)"
      )
    )
  }

  test("some-none-nokj") {
    assume(BuildInfo.scalaVersion != "3.3.3")
    assertNoDiff(
      compileErrors("assertEquals(None, Some(1))"),
      if (isDotty)
        """|error:
           |Can't compare these two types:
           |  First type:  None.type
           |  Second type: Some[Int]
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[None.type, Some[Int]]
           |  Alternative 2: upcast either type into `Any` or a shared supertype.
           |I found:
           |
           |    munit.Compare.compareSubtypeWithSupertype[None.type, Some[Int]](
           |      /* missing */summon[None.type <:< Some[Int]])
           |
           |But no implicit values were found that match type None.type <:< Some[Int].
           |assertEquals(None, Some(1))
           |           ^
           |""".stripMargin
      else
        """|error:
           |Can't compare these two types:
           |  First type:  None.type
           |  Second type: Some[Int]
           |Possible ways to fix this error:
           |  Alternative 1: provide an implicit instance for Compare[None.type, Some[Int]]
           |  Alternative 2: upcast either type into `Any` or a shared supertype
           |assertEquals(None, Some(1))
           |            ^
           |""".stripMargin
    )
  }

}
