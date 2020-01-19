package munit.internal

import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.scalanative.testinterface.PreloadedClassLoader

object PlatformCompat {
  // Scala Native does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def newRunner(
      taskDef: TaskDef,
      classLoader: ClassLoader
  ): Option[MUnitRunner] = {
    scala.util.Try {
      val suite = classLoader
        .asInstanceOf[PreloadedClassLoader]
        .loadPreloaded(taskDef.fullyQualifiedName)
        .asInstanceOf[munit.Suite]
      new MUnitRunner(suite.getClass, () => suite)
    }.toOption
  }
}
