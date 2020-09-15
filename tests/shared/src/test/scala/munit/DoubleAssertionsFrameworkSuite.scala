package munit

class DoubleAssertionsFrameworkSuite extends BaseSuite {
  test("Assert Equals NaN Fails".fail) {
    assertEqualsDouble(1.234, Double.NaN, 0.0)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEqualsDouble(Double.NaN, 1.234, 0.0)
  }

  test("Assert NaN Equals NaN") {
    assertEqualsDouble(Double.NaN, Double.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEqualsDouble(Double.PositiveInfinity, Double.NegativeInfinity, 0.0)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEqualsDouble(Double.PositiveInfinity, 1.23, 0.0)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEqualsDouble(Double.PositiveInfinity, Double.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEqualsDouble(Double.NegativeInfinity, Double.NegativeInfinity, 0.0)
  }

  test("All Infinities") {
    assertEqualsDouble(
      Double.PositiveInfinity,
      Double.NegativeInfinity,
      Double.PositiveInfinity
    )
  }

  // And now, the same with floats...
  test("Assert Equals NaN Fails".fail) {
    assertEqualsFloat(1.234f, Float.NaN, 0.0f)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEqualsFloat(Float.NaN, 1.234f, 0.0f)
  }

  test("Assert NaN Equals NaN") {
    assertEqualsFloat(Float.NaN, Float.NaN, 0.0f)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEqualsFloat(Float.PositiveInfinity, Float.NegativeInfinity, 0.0f)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEqualsFloat(Float.PositiveInfinity, 1.23f, 0.0f)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEqualsFloat(Float.PositiveInfinity, Float.PositiveInfinity, 0.0f)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEqualsFloat(Float.NegativeInfinity, Float.NegativeInfinity, 0.0f)
  }

  test("All Infinities") {
    assertEqualsFloat(
      Float.PositiveInfinity,
      Float.NegativeInfinity,
      Float.PositiveInfinity
    )
  }
}

object DoubleAssertionsFrameworkSuite
    extends FrameworkTest(
      classOf[DoubleAssertionsFrameworkSuite],
      """|==> success munit.DoubleAssertionsFrameworkSuite.Assert Equals NaN Fails
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert NaN Equals Fails
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert NaN Equals NaN
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Not Equals Neg Infinity
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Not Equals
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Equals Infinity
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Neg Infinity Equals Infinity
         |==> success munit.DoubleAssertionsFrameworkSuite.All Infinities
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Equals NaN Fails-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert NaN Equals Fails-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert NaN Equals NaN-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Not Equals Neg Infinity-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Not Equals-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Pos Infinity Equals Infinity-1
         |==> success munit.DoubleAssertionsFrameworkSuite.Assert Neg Infinity Equals Infinity-1
         |==> success munit.DoubleAssertionsFrameworkSuite.All Infinities-1
         |""".stripMargin
    )
