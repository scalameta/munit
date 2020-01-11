package funsuite

import scala.util.control.NoStackTrace

/**
  * Values that have special treatment when evaluating values produced by tests.
  */
object TestValues {

  /** The test failed with the given exception but was ignored but its marked as flaky */
  class FlakyFailure(error: Throwable)
      extends Exception("ignoring flaky test failure", error)
      with NoStackTrace

  /** The test case was ignored. */
  val Ignore = funsuite.Ignore
}
