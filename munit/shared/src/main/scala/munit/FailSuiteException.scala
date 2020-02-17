package munit

class FailSuiteException(
    override val message: String,
    override val location: Location
) extends FailException(message, location)
