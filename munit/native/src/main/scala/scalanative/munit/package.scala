package scala.scalanative.runtime

package object munit {
  /** Drains the execution context by executing all pending tasks. */
  def drainExecutionContext(): Unit = loop()
}
