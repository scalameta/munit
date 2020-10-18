package munit

import org.junit.ComparisonFailure

/**
 * The base exception for all comparison failures.
 *
 * This class exists so that it can extend `org.junit.ComparisonFailure`,
 * which is recognised by IntelliJ so that users can optionally compare the
 * obtained/expected values in a GUI diff explorer.
 *
 * @param message the exception message.
 * @param obtained the obtained value from this comparison.
 * @param expected the expected value from this comparison.
 * @param location the source location where this exception was thrown.
 */
class ComparisonFailException(
    val message: String,
    val obtained: Any,
    val expected: Any,
    val location: Location
) extends ComparisonFailure(message, s"$expected", s"$obtained")
    with FailExceptionLike[ComparisonFailException] {
  override def getMessage: String = message
  def withMessage(newMessage: String): ComparisonFailException =
    new ComparisonFailException(
      newMessage,
      obtained,
      expected,
      location
    )
}
