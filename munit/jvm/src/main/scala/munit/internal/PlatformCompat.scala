package munit.internal

import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger

import scala.concurrent.duration.Duration
import java.util.concurrent.{
  Executors,
  ThreadFactory,
  TimeUnit,
  TimeoutException
}
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext
import java.util.concurrent.atomic.AtomicInteger

object PlatformCompat {
  private val sh = Executors.newSingleThreadScheduledExecutor(
    new ThreadFactory {
      val counter = new AtomicInteger
      def threadNumber() = counter.incrementAndGet()
      def newThread(r: Runnable) =
        new Thread(r, s"munit-scheduler-${threadNumber()}") {
          setDaemon(true)
          setPriority(Thread.NORM_PRIORITY)
        }
    }
  )
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

  def setTimeout(ms: Int)(body: => Unit): () => Unit = {
    val scheduled = sh.schedule[Unit](
      () => body,
      ms,
      TimeUnit.MILLISECONDS
    )

    () => scheduled.cancel(false)
  }

  def isIgnoreSuite(cls: Class[_]): Boolean =
    cls.getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  def isJVM: Boolean = true
  def isJS: Boolean = false
  def isNative: Boolean = false
  def getThisClassLoader: ClassLoader = this.getClass().getClassLoader()

  type InvocationTargetException = java.lang.reflect.InvocationTargetException
  type UndeclaredThrowableException =
    java.lang.reflect.UndeclaredThrowableException
}
