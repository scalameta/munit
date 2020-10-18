package org.junit

class ComparisonFailure(message: String, fExpected: String, fActual: String)
    extends AssertionError(message) {

  override def getMessage(): String = message

  def getActual(): String = fActual

  def getExpected(): String = fExpected
}
