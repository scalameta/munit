package munit

trait ComparisonFailExceptionHandler {
  def handle(
      message: String,
      obtained: String,
      expected: String,
      location: Location,
  ): Nothing
}
