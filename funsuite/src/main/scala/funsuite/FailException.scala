package funsuite

import scala.util.control.NoStackTrace

class FailException(message: String, location: Location)
    extends Exception(message)
