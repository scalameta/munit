/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import sbt.testing._
import munit.internal.PlatformCompat

final class JUnitRunner(
    val args: Array[String],
    val remoteArgs: Array[String],
    runSettings: RunSettings,
    classLoader: ClassLoader,
    customRunners: CustomRunners
) extends Runner {
  PlatformCompat.setThisClassLoader(classLoader)

  def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map(new JUnitTask(_, runSettings, classLoader))

  def done(): String = ""

  def serializeTask(task: Task, serializer: TaskDef => String): String =
    serializer(task.taskDef)

  def deserializeTask(task: String, deserializer: String => TaskDef): Task =
    new JUnitTask(deserializer(task), runSettings, classLoader)

  def receiveMessage(msg: String): Option[String] = None
}
