package munit

trait EventuallyTransforms {
  this: BaseFunSuite =>

  /** Evaluates the effectful body until it succeeds or max retries are exhausted. */
  def eventually[A](
      body: => A
  )(implicit options: EventuallyOptions, transform: EventuallyTransform[A]): A =
    options.eventually(body)

}
