package munit.internal

import scala.scalajs.reflect.Reflect
import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object PlatformCompat {
  // Scala.js does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

  def await[T](f: Future[T], timeout: Duration): T = {
    f.value match {
      case Some(value) =>
        value.get
      case None =>
        throw new NoSuchElementException(
          s"Future $f is not completed and `scala.concurrent.Await` is not supported in JavaScript."
        )
    }
  }

  def newRunner(
      taskDef: TaskDef,
      classLoader: ClassLoader
  ): Option[MUnitRunner] = {
    Reflect
      .lookupInstantiatableClass(taskDef.fullyQualifiedName())
      .map(cls =>
        new MUnitRunner(
          cls.runtimeClass.asInstanceOf[Class[_ <: munit.Suite]],
          () => cls.newInstance().asInstanceOf[munit.Suite]
        )
      )
  }
  private var myClassLoader: ClassLoader = _
  def setThisClassLoader(loader: ClassLoader): Unit = myClassLoader = loader
  def getThisClassLoader: ClassLoader = myClassLoader
}
