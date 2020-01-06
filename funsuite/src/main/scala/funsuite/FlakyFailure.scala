package funsuite

import scala.util.control.NoStackTrace

class FlakyFailure(error: Throwable)
    extends Exception("ignoring flaky test failure", error)
    with NoStackTrace
