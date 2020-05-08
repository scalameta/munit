package munit

import scala.collection.mutable
import org.scalacheck.Prop.forAll

// Regression test for https://github.com/scalameta/munit/issues/118
final class ScalaCheckSeedSuite extends ScalaCheckSuite {

  private val ints = mutable.Set.empty[Int]

  property("generating int") {
    forAll { (i: Int) =>
      ints.add(i)
      true
    }
  }

  test("generated int are not all the same") {
    assert(ints.size > 1)
  }
}
