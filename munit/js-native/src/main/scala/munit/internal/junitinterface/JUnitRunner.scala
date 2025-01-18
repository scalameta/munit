/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import munit.internal.PlatformCompat

import sbt.testing._

final class JUnitRunner(
    val args: Array[String],
    _remoteArgs: Array[String],
    runSettings: RunSettings,
    classLoader: ClassLoader,
    customRunners: CustomRunners,
) extends Runner {
  PlatformCompat.setThisClassLoader(classLoader)

  override def remoteArgs(): Array[String] = _remoteArgs

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = taskDefs
    .map(new JUnitTask(_, runSettings, classLoader))

  override def done(): String = ""

  override def serializeTask(
      task: Task,
      serializer: TaskDef => String,
  ): String = serializer(task.taskDef())

  override def deserializeTask(
      task: String,
      deserializer: String => TaskDef,
  ): Task = new JUnitTask(deserializer(task), runSettings, classLoader)

  override def receiveMessage(msg: String): Option[String] = None
}
