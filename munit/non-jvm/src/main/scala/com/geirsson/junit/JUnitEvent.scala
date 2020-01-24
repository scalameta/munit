/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import sbt.testing._

final class JUnitEvent(
    taskDef: TaskDef,
    val fullyQualifiedName: String,
    val status: Status,
    val selector: Selector,
    val throwable: OptionalThrowable = new OptionalThrowable,
    val duration: Long = -1L
) extends Event {
  def fingerprint: Fingerprint = taskDef.fingerprint
}
