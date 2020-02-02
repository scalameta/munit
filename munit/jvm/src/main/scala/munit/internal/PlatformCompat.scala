package munit.internal

import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

object PlatformCompat {
  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    task.execute(eventHandler, loggers)
    Future.successful(())
  }
  def waitAtMost[T](future: Future[T], duration: Duration): Future[T] = {
    Future.fromTry(Try(Await.result(future, duration)))
  }

  def isIgnoreSuite(cls: Class[_]): Boolean =
    cls.getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
  def getThisClassLoader: ClassLoader = this.getClass().getClassLoader()
}
