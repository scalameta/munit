package munit.internal.difflib

import munit.Location

trait ComparisonFailExceptionHandler {
  def handle(
      message: String,
      obtained: String,
      expected: String,
      location: Location
  ): Nothing
}
