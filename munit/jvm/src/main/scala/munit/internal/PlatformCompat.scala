package munit.internal

import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.concurrent.duration.Duration
import java.util.concurrent.Executors
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object PlatformCompat {
  private val sh = Executors.newSingleThreadScheduledExecutor()
  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    task.execute(eventHandler, loggers)
    Future.successful(())
  }
  @deprecated("use the overload with an explicit ExecutionContext", "1.0.0")
  def waitAtMost[T](
      future: Future[T],
      duration: Duration
  ): Future[T] = {
    waitAtMost(() => future, duration, ExecutionContext.global)
  }
  def waitAtMost[T](
      startFuture: () => Future[T],
      duration: Duration,
      ec: ExecutionContext
  ): Future[T] = {
    val onComplete = Promise[T]()
    val timeout = sh.schedule[Unit](
      () =>
        onComplete.tryFailure(
          new TimeoutException(s"test timed out after $duration")
        ),
      duration.toMillis,
      TimeUnit.MILLISECONDS
    )
    ec.execute(new Runnable {
      def run(): Unit = {
        startFuture().onComplete { result =>
          onComplete.tryComplete(result)
          timeout.cancel(false)
        }(ec)
      }
    })
    onComplete.future
  }

  def isIgnoreSuite(cls: Class[_]): Boolean =
    cls.getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
  def getThisClassLoader: ClassLoader = this.getClass().getClassLoader()
}
