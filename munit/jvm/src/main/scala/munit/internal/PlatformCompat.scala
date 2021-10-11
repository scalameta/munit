package munit.internal

import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.concurrent.duration.Duration
import scala.concurrent.BlockContext
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
  private[this] val _blockContext = new ThreadLocal[BlockContext]()
  def waitAtMost[T](
      future: Future[T],
      duration: Duration,
      ec: ExecutionContext
  ): Future[T] = {
    val onComplete = Promise[T]()
    var onCancel: () => Unit = () => ()
    future.onComplete { result =>
      onComplete.tryComplete(result)
    }(ec)
    val timeout = sh.schedule[Unit](
      () =>
        onComplete.tryFailure(
          new TimeoutException(s"test timed out after $duration")
        ),
      duration.toMillis,
      TimeUnit.MILLISECONDS
    )
    onCancel = () => timeout.cancel(false)
    onComplete.future
  }

  def isIgnoreSuite(cls: Class[_]): Boolean =
    cls.getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
  def getThisClassLoader: ClassLoader = this.getClass().getClassLoader()
}
