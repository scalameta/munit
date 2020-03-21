package munit

import scala.util.control.NoStackTrace

class ScalaCheckFailException(message: String)
    extends Exception(message)
    with NoStackTrace
