/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import sbt.testing._

final class JUnitEvent(
    taskDef: TaskDef,
    _fullyQualifiedName: String,
    _status: Status,
    _selector: Selector,
    _throwable: OptionalThrowable = new OptionalThrowable,
    _duration: Long = -1L
) extends Event {
  override def status(): Status = _status
  override def selector(): Selector = _selector
  override def throwable(): OptionalThrowable = _throwable
  override def duration(): Long = _duration
  override def fullyQualifiedName(): String = _fullyQualifiedName
  override def fingerprint(): Fingerprint = taskDef.fingerprint()
}
