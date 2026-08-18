package munit

import munit.internal.{PlatformCompat, console}

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

final class EventuallyOptions private (
    val maxRetries: Int,
    val sleep: FiniteDuration,
    private val filters: Seq[Throwable => Boolean] =
      Seq(EventuallyOptions.isAssertion),
    // add fields above with defaults, to `privateCopy`, then a `withX` method
) {

  /**
   * Whether `eventually` retries after the given exception.
   *
   * A `Future` boxes an `AssertionError` in an `ExecutionException`, so the
   * filter is given the root cause and matches alike on either path.
   */
  private def isRetriable(throwable: Throwable): Boolean = {
    val ex = Exceptions.rootCause(throwable)
    filters.exists(_(ex))
  }

  def withMaxRetries(newValue: Int): EventuallyOptions =
    privateCopy(maxRetries = EventuallyOptions.requirePositive(newValue))
  def withSleep(newValue: FiniteDuration): EventuallyOptions =
    privateCopy(sleep = EventuallyOptions.requirePositive(newValue))
  def withFilters(newValue: Throwable => Boolean*): EventuallyOptions = {
    require(newValue.nonEmpty, "filters must be non-empty")
    privateCopy(filters = newValue)
  }
  def addFilters(newValue: Throwable => Boolean*): EventuallyOptions =
    privateCopy(filters = this.filters ++ newValue)

  private[this] def privateCopy(
      maxRetries: Int = this.maxRetries,
      sleep: FiniteDuration = this.sleep,
      filters: Seq[Throwable => Boolean] = this.filters,
  ): EventuallyOptions = new EventuallyOptions(maxRetries, sleep, filters)

  /**
   * Evaluates the effectful body until it succeeds or max retries are exhausted.
   */
  def eventually[A](body: => A)(implicit transform: EventuallyTransform[A]): A =
    transform(body, this)

  private[munit] def retryAsync[A](
      body: => Future[A]
  )(implicit ctx: SuiteContext): Future[A] = {
    implicit val ec: ExecutionContext = ctx.ec
    def attempt(remaining: Int): Future[A] = Future.unit
      .flatMap(_ => console.StackTraces.dropOutside(body)).transformWith {
        case Failure(ex) if remaining > 0 && isRetriable(ex) =>
          val promise = Promise[Unit]()
          PlatformCompat.setTimeout(sleep.toMillis.toInt) {
            promise.trySuccess(())
            ()
          }
          promise.future.flatMap(_ => attempt(remaining - 1))
        case x => Future.fromTry(x)
      }

    attempt(maxRetries)
  }

  private[munit] def retry[A](body: => A): A = {
    // a loop, not recursion: retries must not deepen the reported stack trace
    var remaining = maxRetries
    while (remaining > 0)
      try return console.StackTraces.dropOutside(body)
      catch {
        case control.NonFatal(ex) if isRetriable(ex) =>
          PlatformCompat.sleep(sleep)
          remaining -= 1
      }
    console.StackTraces.dropOutside(body)
  }

}

object EventuallyOptions {

  def apply(maxRetries: Int, sleep: FiniteDuration) =
    new EventuallyOptions(requirePositive(maxRetries), requirePositive(sleep))

  private val isAssertion =
    (ex: Throwable) => ex.isInstanceOf[FailExceptionLike[_]]

  /** Retries nothing, so `eventually` evaluates its body exactly once. */
  val disabled: EventuallyOptions = new EventuallyOptions(0, Duration.Zero)

  private def requirePositive(maxRetries: Int): Int = {
    require(maxRetries > 0, s"maxRetries must be positive: $maxRetries")
    maxRetries
  }

  private def requirePositive(sleep: FiniteDuration): FiniteDuration = {
    require(sleep > Duration.Zero, s"sleep must be positive: $sleep")
    sleep
  }

}
