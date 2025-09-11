package munit.internal

import munit.MUnitRunner

import sbt.testing.EventHandler
import sbt.testing.Logger
import sbt.testing.Task
import sbt.testing.TaskDef

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Await
import scala.concurrent.Awaitable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
import scala.scalanative.reflect.Reflect
import scala.util.control.NonFatal

// Delay reachability of multithreading capability
// Would not force multithreading support unless explicitly configured by the user
// or if using threads in the tests
private object LazyMultithreadingSupport {
  val sh = Executors.newSingleThreadScheduledExecutor(new ThreadFactory {
    val counter = new AtomicInteger
    def threadNumber() = counter.incrementAndGet()
    def newThread(r: Runnable) =
      new Thread(r, s"munit-scheduler-${threadNumber()}") {
        setDaemon(true)
        setPriority(Thread.NORM_PRIORITY)
      }
  })
}
object PlatformCompat {
  import LazyMultithreadingSupport._

  val executionContext: ExecutionContext = ExecutionContext.global

  def awaitResult[T](awaitable: Awaitable[T]): T = {
    if (!isMultithreadingEnabled) Thread.`yield`() // invokes SN 0.4 scalanative.runtime.loop()
    Await.result(awaitable, Duration.Inf)
  }

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
    if (!isMultithreadingEnabled) startFuture()
    else if (!duration.isFinite) startFuture()
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

  def setTimeout(ms: Int)(body: => Unit): () => Unit =
    if (!isMultithreadingEnabled) {
      // Thread.sleep(ms)
      body
      () => ()
    } else {
      val scheduled = sh.schedule[Unit](() => body, ms, TimeUnit.MILLISECONDS)

      () => scheduled.cancel(false)
    }

  // Scala Native does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  final def isJVM: Boolean = false
  final def isJS: Boolean = false
  final def isNative: Boolean = true

  def newRunner(
      taskDef: TaskDef,
      classLoader: ClassLoader,
  ): Option[MUnitRunner] = Reflect
    .lookupInstantiatableClass(taskDef.fullyQualifiedName()).map(cls =>
      new MUnitRunner(
        cls.runtimeClass.asInstanceOf[Class[_ <: munit.Suite]],
        cls.newInstance().asInstanceOf[munit.Suite],
      )
    )
  private var myClassLoader: ClassLoader = _
  def setThisClassLoader(loader: ClassLoader): Unit = myClassLoader = loader
  def getThisClassLoader: ClassLoader = myClassLoader

  type InvocationTargetException = java.lang.reflect.InvocationTargetException
  type UndeclaredThrowableException =
    java.lang.reflect.UndeclaredThrowableException
}
