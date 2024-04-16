package munit.internal

import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.concurrent.Future
import scala.scalanative.reflect.Reflect
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.annotation.nowarn
import scala.concurrent.Await
import scala.concurrent.Awaitable
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext

object PlatformCompat {
  def awaitResult[T](awaitable: Awaitable[T]): T = {
    scalanative.runtime.loop()
    Await.result(awaitable, Duration.Inf)
  }

  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    task.execute(eventHandler, loggers)
    Future.successful(())
  }
  @nowarn("msg=used")
  def waitAtMost[T](
      startFuture: () => Future[T],
      duration: Duration,
      ec: ExecutionContext
  ): Future[T] = {
    startFuture()
  }
  def setTimeout(ms: Int)(body: => Unit): () => Unit = {
    Thread.sleep(ms)
    body

    () => ()
  }

  // Scala Native does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  @nowarn("msg=used")
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

  type InvocationTargetException = java.lang.reflect.InvocationTargetException
  type UndeclaredThrowableException =
    java.lang.reflect.UndeclaredThrowableException
}
