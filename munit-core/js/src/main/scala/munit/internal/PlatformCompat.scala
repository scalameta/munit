package munit.internal

import scala.scalajs.reflect.Reflect
import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.Awaitable
import scala.concurrent.ExecutionContext
import scala.scalajs.js.timers
import java.util.concurrent.TimeoutException

object PlatformCompat {
  def awaitResult[T](awaitable: Awaitable[T]): T = {
    Await.result(awaitable, Duration.Inf)
  }

  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    val p = Promise[Unit]()
    task.execute(eventHandler, loggers, _ => p.success(()))
    p.future
  }

  def waitAtMost[T](
      startFuture: () => Future[T],
      duration: Duration,
      ec: ExecutionContext
  ): Future[T] = {
    val onComplete = Promise[T]()
    val timeoutHandle = timers.setTimeout(duration.toMillis) {
      onComplete.tryFailure(
        new TimeoutException(s"test timed out after $duration")
      )
    }
    ec.execute(new Runnable {
      def run(): Unit = {
        startFuture().onComplete { result =>
          onComplete.tryComplete(result)
          timers.clearTimeout(timeoutHandle)
        }(ec)
      }
    })
    onComplete.future
  }

  def setTimeout(ms: Int)(body: => Unit): () => Unit = {
    val timeoutHandle = timers.setTimeout(ms)(body)

    () => timers.clearTimeout(timeoutHandle)
  }

  // Scala.js does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

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

  type InvocationTargetException = munit.internal.InvocationTargetException
  type UndeclaredThrowableException =
    munit.internal.UndeclaredThrowableException
}
