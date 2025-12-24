package munit.internal

import sbt.testing.{EventHandler, Logger, Task}

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{
  Executors, ThreadFactory, TimeUnit, TimeoutException,
}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

object PlatformCompat {

  val executionContext: ExecutionContext = ExecutionContext.global

  private val sh = Executors
    .newSingleThreadScheduledExecutor(new ThreadFactory {
      val counter = new AtomicInteger
      def threadNumber() = counter.incrementAndGet()
      def newThread(r: Runnable) =
        new Thread(r, s"munit-scheduler-${threadNumber()}") {
          setDaemon(true)
          setPriority(Thread.NORM_PRIORITY)
        }
    })

  def awaitResult[T](awaitable: Awaitable[T]): T = Await
    .result(awaitable, Duration.Inf)

  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger],
  ): Future[Unit] = {
    task.execute(eventHandler, loggers)
    Future.successful(())
  }

  def waitAtMost[T](
      startFuture: () => Future[T],
      duration: Duration,
      ec: ExecutionContext,
  ): Future[T] =
    if (!duration.isFinite) startFuture()
    else {
      val onComplete = Promise[T]()
      val timeout = sh.schedule[Unit](
        () =>
          onComplete
            .tryFailure(new TimeoutException(s"test timed out after $duration")),
        duration.toMillis,
        TimeUnit.MILLISECONDS,
      )
      def completeWith(result: util.Try[T]): Unit = {
        onComplete.tryComplete(result)
        timeout.cancel(false)
      }
      ec.execute(() =>
        try startFuture().onComplete(completeWith)(ec)
        catch { case NonFatal(ex) => completeWith(util.Failure(ex)) }
      )
      onComplete.future
    }

  def setTimeout(ms: Int)(body: => Unit): () => Unit = {
    val scheduled = sh.schedule[Unit](() => body, ms, TimeUnit.MILLISECONDS)

    () => scheduled.cancel(false)
  }

  def isIgnoreSuite(cls: Class[_]): Boolean = cls
    .getAnnotationsByType(classOf[munit.IgnoreSuite]).nonEmpty
  final val isJVM = true
  final val isJS = false
  final val isNative = false
  def getThisClassLoader: ClassLoader = this.getClass().getClassLoader()

  type InvocationTargetException = java.lang.reflect.InvocationTargetException
  type UndeclaredThrowableException =
    java.lang.reflect.UndeclaredThrowableException
}
