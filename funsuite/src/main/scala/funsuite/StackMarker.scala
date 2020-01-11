package funsuite

import java.{util => ju}

/**
  * Wrapper-functions that can be used to mark parts of the callstack that are
  * meant to be filtered out later.
  */
class StackMarker
object StackMarker {
  // Ask Scalac/Scala.js nicely to try and avoid inlining these two marker methods,
  // to make sure they don't disappear from the stack traces
  @noinline
  def dropInside[T](t: => T): T = t
  @noinline
  def dropOutside[T](t: => T): T = t

  def trimStackTrace(ex: Throwable): ex.type = {
    val isVisited = ju.Collections.newSetFromMap(
      new ju.IdentityHashMap[Throwable, java.lang.Boolean]()
    )
    def loop(e: Throwable): Unit = {
      if (e != null && isVisited.add(e)) {
        val stack = e.getStackTrace()
        if (stack != null) {
          e.setStackTrace(filterCallStack(stack))
        }
        loop(e.getCause())
      }
    }
    loop(ex)
    ex
  }
  private val className = classOf[StackMarker].getCanonicalName() + "$"
  def filterCallStack(
      stack: Array[StackTraceElement]
  ): Array[StackTraceElement] = {
    val droppedInside = stack.lastIndexWhere(x =>
      x.getClassName == className &&
        x.getMethodName == "dropInside"
    )

    val droppedOutside = stack.indexWhere(x =>
      x.getClassName == className &&
        x.getMethodName == "dropOutside"
    )

    val stack1 = stack.slice(
      droppedInside match {
        case -1 => 0
        case n  => n + 3
      },
      droppedOutside match {
        case -1 => stack.length
        case n  => n
      }
    )

    val lastNonLMFIndex =
      stack1.lastIndexWhere(x => !x.getClassName.contains("$$Lambda$"))

    if (lastNonLMFIndex < 0) stack1
    else stack1.take(lastNonLMFIndex + 1)
  }
}
