package munit

class DoubleAssertionsFrameworkSuite extends BaseSuite {
  test("Assert Equals NaN Fails".fail) {
    assertEquals(1.234, Double.NaN, 0.0)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEquals(Double.NaN, 1.234, 0.0)
  }

  test("Assert NaN Equals NaN") {
    assertEquals(Double.NaN, Double.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEquals(Double.PositiveInfinity, Double.NegativeInfinity, 0.0)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEquals(Double.PositiveInfinity, 1.23, 0.0)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEquals(Double.PositiveInfinity, Double.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEquals(Double.NegativeInfinity, Double.NegativeInfinity, 0.0)
  }

  test("All Infinities".fail) {
    assertEquals(
      Double.PositiveInfinity,
      Double.NegativeInfinity,
      Double.PositiveInfinity
    )
  }

  // And now, the same with floats...
  test("Assert Equals NaN Fails".fail) {
    assertEquals(1.234f, Float.NaN, 0.0)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEquals(Float.NaN, 1.234f, 0.0)
  }

  test("Assert NaN Equals NaN") {
    assertEquals(Float.NaN, Float.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEquals(Float.PositiveInfinity, Float.NegativeInfinity, 0.0)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEquals(Float.PositiveInfinity, 1.23f, 0.0)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEquals(Float.PositiveInfinity, Float.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEquals(Float.NegativeInfinity, Float.NegativeInfinity, 0.0)
  }

  test("All Infinities".fail) {
    assertEquals(
      Float.PositiveInfinity,
      Float.NegativeInfinity,
      Float.PositiveInfinity
    )
  }
}
