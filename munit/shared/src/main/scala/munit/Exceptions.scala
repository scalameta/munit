package munit

import java.util.concurrent.ExecutionException
import scala.annotation.tailrec

object Exceptions {

  // NOTE(olafur): these exceptions appear when we await on futures. We unwrap
  // these exception in order to provide more helpful error messages.
  @tailrec
  def rootCause(x: Throwable): Throwable = x match {
    case _: ExceptionInInitializerError | _: ExecutionException
        if x.getCause != null =>
      rootCause(x.getCause)
    case _ => x
  }
}
