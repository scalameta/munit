package munit
package scalacheck

import org.scalacheck.Prop.forAll
import org.scalacheck.rng.Seed

class ScalaCheckFrameworkSuite extends ScalaCheckSuite {

  override def scalaCheckTestParameters =
    super.scalaCheckTestParameters.withInitialSeed(Seed(123L))

  property("list concatenation") {
    forAll { (l1: List[Int], l2: List[Int]) =>
      l1.size + l2.size == (l1 ::: l2).size
    }
  }

  property("squared") {
    forAll { (n: Int) =>
      scala.math.sqrt(n * n) == n
    }
  }
}

object ScalaCheckFrameworkSuite
    extends FrameworkTest(
      classOf[ScalaCheckFrameworkSuite],
      s"""|==> success munit.scalacheck.ScalaCheckFrameworkSuite.list concatenation
          |==> failure munit.scalacheck.ScalaCheckFrameworkSuite.squared -${' '}
          |Falsified after 0 passed tests.
          |> ARG_0: -1
          |> ARG_0_ORIGINAL: 2147483647
          |""".stripMargin
    )