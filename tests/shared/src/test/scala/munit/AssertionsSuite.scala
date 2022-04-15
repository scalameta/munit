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
    class A {
      override def equals(x: Any): Boolean = true
    }
    class B {
      override def equals(x: Any): Boolean = true
    }
    // This compiles by default in Scala 3 because we haven't enabled strict
    // equality for this file. See AssertionsEqlSuite for Scala 3 tests where
    // strict equality is enabled and the following assertion fails to compile.
    assertEquals(new A, new B)
  }
  test("array-sameElements".fail) {
    val e = intercept[ComparisonFailException] {
      assertEquals(Array(1, 2), Array(1, 2))
    }
    assert(
      clue(e).getMessage.contains(
        "arrays have the same elements but different reference equality. Convert the arrays to a non-Array collection if you intend to assert the two arrays have the same elements. For example, `assertEquals(a.toSeq, b.toSeq)"
      )
    )
  }
}
