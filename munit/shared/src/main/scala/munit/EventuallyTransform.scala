package munit

import scala.concurrent.Future

/**
 * Picks how `eventually` retries a body of type `A`.
 *
 * Dispatch is by type rather than by overload: a `Future` body must retry
 * without blocking, anything else retries in place.
 */
trait EventuallyTransform[A] {
  def apply(body: => A, options: EventuallyOptions): A
}

object EventuallyTransform extends EventuallyTransformLowPriority {

  implicit def munitFutureTransform[A](implicit
      ctx: SuiteContext
  ): EventuallyTransform[Future[A]] = new EventuallyTransform[Future[A]] {
    def apply(body: => Future[A], options: EventuallyOptions): Future[A] =
      options.retryAsync(body)
  }

}

trait EventuallyTransformLowPriority {

  implicit def munitValueTransform[A]: EventuallyTransform[A] =
    new EventuallyTransform[A] {
      def apply(body: => A, options: EventuallyOptions): A = options.retry(body)
    }

}
