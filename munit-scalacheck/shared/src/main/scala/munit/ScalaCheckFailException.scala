package munit

import scala.util.control.NoStackTrace

@deprecated(
  "This class is not used anywhere and will be removed in a future release",
  "0.8.0"
)
class ScalaCheckFailException(message: String)
    extends Exception(message)
    with NoStackTrace
