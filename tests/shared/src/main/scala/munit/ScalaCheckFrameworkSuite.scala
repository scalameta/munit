package munit

import org.scalacheck.Prop.forAll

class ScalaCheckFrameworkSuite extends ScalaCheckSuite {

  // NOTE(gabro): this is needed for making the test output stable for the failed test below.
  // It also serves as a test for overriding this parameter
  override val scalaCheckInitialSeed =
    "CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB="

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

  override def munitFlakyOK: Boolean = true
  test("flaky test".flaky) {
    forAll { (i: Int) =>
      assertEquals(1, 0)
    }
  }

}

object ScalaCheckFrameworkSuite
    extends FrameworkTest(
      classOf[ScalaCheckFrameworkSuite],
      """|==> success munit.ScalaCheckFrameworkSuite.boolean check (true)
         |==> failure munit.ScalaCheckFrameworkSuite.boolean check (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:18
         |17:
         |18:  property("boolean check (false)") {
         |19:    forAll { (n: Int) => scala.math.sqrt(n * n) == n }
         |
         |Failing seed: CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB=
         |You can reproduce this failure by adding the following override to your suite:
         |
         |  override def scalaCheckInitialSeed = "CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB="
         |
         |Falsified after 0 passed tests.
         |> ARG_0: -1
         |> ARG_0_ORIGINAL: 2147483647
         |==> success munit.ScalaCheckFrameworkSuite.tagged
         |==> success munit.ScalaCheckFrameworkSuite.assertions (true)
         |==> failure munit.ScalaCheckFrameworkSuite.assertions (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:36
         |35:      assertEquals(n * 1, n)
         |36:      assertEquals(n * n, n)
         |37:      assertEquals(n + 0, n)
         |values are not the same
         |=> Obtained
         |1
         |=> Diff (- obtained, + expected)
         |-1
         |+-1
         |
         |Failing seed: CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB=
         |You can reproduce this failure by adding the following override to your suite:
         |
         |  override def scalaCheckInitialSeed = "CTH6hXj8ViScMmsO78-k4_RytXHPK_wSJYNH2h4dCpB="
         |
         |Falsified after 0 passed tests.
         |> ARG_0: -1
         |> ARG_0_ORIGINAL: 2147483647
         |==> skipped munit.ScalaCheckFrameworkSuite.flaky test - ignoring flaky test failure
         |""".stripMargin
    )
