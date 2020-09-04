package munit

class DoubleAssertionsSuite extends BaseSuite {
  test("Assert Equals NaN Fails") {
    try {
      assertEquals(1.234, Double.NaN, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert NaN Equals Fails") {
    try {
      assertEquals(Double.NaN, 1.234, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert NaN Equals NaN") {
    assertEquals(Double.NaN, Double.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity") {
    try {
      assertEquals(Double.PositiveInfinity, Double.NegativeInfinity, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert Pos Infinity Not Equals") {
    try {
      assertEquals(Double.PositiveInfinity, 1.23, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEquals(Double.PositiveInfinity, Double.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEquals(Double.NegativeInfinity, Double.NegativeInfinity, 0.0)
  }

  test("All Infinities") {
    try {
      assertEquals(
        Double.PositiveInfinity,
        Double.NegativeInfinity,
        Double.PositiveInfinity
      )
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  // And now, the same with floats...
  test("Assert Equals NaN Fails") {
    try {
      assertEquals(1.234f, Float.NaN, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert NaN Equals Fails") {
    try {
      assertEquals(Float.NaN, 1.234f, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert NaN Equals NaN") {
    assertEquals(Float.NaN, Float.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity") {
    try {
      assertEquals(Float.PositiveInfinity, Float.NegativeInfinity, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert Pos Infinity Not Equals") {
    try {
      assertEquals(Float.PositiveInfinity, 1.23f, 0.0)
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEquals(Float.PositiveInfinity, Float.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEquals(Float.NegativeInfinity, Float.NegativeInfinity, 0.0)
  }

  test("All Infinities") {
    try {
      assertEquals(
        Float.PositiveInfinity,
        Float.NegativeInfinity,
        Float.PositiveInfinity
      )
      fail("unexpected")
    } catch {
      case _: FailException =>
    }
  }
}
