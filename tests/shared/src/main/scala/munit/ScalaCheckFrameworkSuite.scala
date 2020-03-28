package munit

import org.scalacheck.Prop.forAll
import org.scalacheck.rng.Seed

class ScalaCheckFrameworkSuite extends ScalaCheckSuite {

  // NOTE(gabro): this is needed for making the test output stable for the failed test below.
  // It also serves as a test for overriding these parameters.
  override def scalaCheckTestParameters: org.scalacheck.Test.Parameters =
    super.scalaCheckTestParameters.withInitialSeed(Seed(123L))

  property("boolean check (true)") {
    forAll { (l1: List[Int], l2: List[Int]) =>
      l1.size + l2.size == (l1 ::: l2).size
    }
  }

  property("boolean check (false)") {
    forAll { (n: Int) =>
      scala.math.sqrt(n * n) == n
    }
  }

  property("tagged".tag(new Tag("a"))) {
    forAll { (n: Int) =>
      n + 0 == n
    }
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
      s"""|==> success munit.ScalaCheckFrameworkSuite.boolean check (true)
          |==> failure munit.ScalaCheckFrameworkSuite.boolean check (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:19
          |18:
          |19:  property("boolean check (false)") {
          |20:    forAll { (n: Int) =>
          |
          |Falsified after 0 passed tests.
          |> ARG_0: -1
          |> ARG_0_ORIGINAL: 2147483647
          |==> success munit.ScalaCheckFrameworkSuite.tagged
          |==> success munit.ScalaCheckFrameworkSuite.assertions (true)
          |==> failure munit.ScalaCheckFrameworkSuite.assertions (false) - /scala/munit/ScalaCheckFrameworkSuite.scala:41
          |40:      assertEquals(n * 1, n)
          |41:      assertEquals(n * n, n)
          |42:      assertEquals(n + 0, n)
          |values are not the same
          |=> Obtained
          |1
          |=> Diff (- obtained, + expected)
          |-1
          |+-1
          |
          |Falsified after 0 passed tests.
          |> ARG_0: -1
          |> ARG_0_ORIGINAL: 2147483647
          |""".stripMargin
    )
