package munit

import org.scalacheck.Prop.forAll

class ScalaCheckFrameworkSuite extends ScalaCheckSuite {

  // NOTE(gabro): this is needed for making the test output stable for the failed test below.
  // It also serves as a test for overriding these parameters.
  override def scalaCheckTestParameters =
    super.scalaCheckTestParameters.withInitialSeed(
      "CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB="
    )

  property("boolean check (true)") {
    forAll { (l1: List[Int], l2: List[Int]) =>
      l1.size + l2.size == (l1 ::: l2).size
    }
  }

  property("boolean check (false)") {
    forAll { (n: Int) => scala.math.sqrt(n * n) == n }
  }

  property("tagged".tag(new Tag("a"))) {
    forAll { (n: Int) => n + 0 == n }
  }

  property("assertions (true)") {
    forAll { (n: Int) =>
      assertEquals(n * 2, n + n)
      assertEquals(n * 0, 0)
    }
  }

  property("assertions (false)") {
    forAll { (n: Int) =>
      assertEquals(n * 1, n)
      assertEquals(n * n, n)
      assertEquals(n + 0, n)
    }
  }

}

object ScalaCheckFrameworkSuite
    extends FrameworkTest(
      classOf[ScalaCheckFrameworkSuite],
      """|==> success munit.ScalaCheckFrameworkSuite.boolean check (true)
         |==> failure munit.ScalaCheckFrameworkSuite.boolean check (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:20
         |19:
         |20:  property("boolean check (false)") {
         |21:    forAll { (n: Int) => scala.math.sqrt(n * n) == n }
         |
         |Failing seed: CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB=
         |
         |Falsified after 0 passed tests.
         |> ARG_0: -1
         |> ARG_0_ORIGINAL: 2147483647
         |==> success munit.ScalaCheckFrameworkSuite.tagged
         |==> success munit.ScalaCheckFrameworkSuite.assertions (true)
         |==> failure munit.ScalaCheckFrameworkSuite.assertions (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:38
         |37:      assertEquals(n * 1, n)
         |38:      assertEquals(n * n, n)
         |39:      assertEquals(n + 0, n)
         |values are not the same
         |=> Obtained
         |1
         |=> Diff (- obtained, + expected)
         |-1
         |+-1
         |
         |Failing seed: CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB=
         |
         |Falsified after 0 passed tests.
         |> ARG_0: -1
         |> ARG_0_ORIGINAL: 2147483647
         |""".stripMargin
    )
