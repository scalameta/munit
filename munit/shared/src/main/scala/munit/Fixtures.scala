package munit

/**
  * Experimental API for declaring fixtures without mutable state.
  */
trait Fixtures { self: FunSuite =>

  trait Fixture[T] {
    class BeforeEachFixture(val options: TestOptions)
    class AfterEachFixture(
        val argument: T,
        val testValue: Option[TestValue],
        val options: TestOptions
    ) {
      def withArgument(newArgument: T): AfterEachFixture =
        new AfterEachFixture(newArgument, testValue, options)
    }
    def beforeEach(context: BeforeEachFixture): T
    def afterEach(context: AfterEachFixture): Unit
    final def test(options: TestOptions)(
        body: T => TestValue
    )(implicit loc: Location): Unit = {
      self.test(options) {
        val env = beforeEach(new BeforeEachFixture(options))
        var testValue = Option.empty[TestValue]
        try {
          val value = body(env)
          testValue = Some(value)
          value
        } finally {
          afterEach(new AfterEachFixture(env, testValue, options))
        }
      }(loc)
    }
  }
  object Fixture {
    def map2[A, B](a: Fixture[A], b: Fixture[B]): Fixture[(A, B)] =
      new Fixture[(A, B)] {
        // NOTE(olafur) It would be nice to avoid the asInstanceOf casts but I
        // was not able to implement it without moving `AfterEachFixture` out of
        // the `Fixture[T]` trait making us write `AfterEachFixture[T]` instead
        // of `AfterEachFixture` (without type parameter) when implementing the
        // `afterEach()` method.
        def beforeEach(context: BeforeEachFixture): (A, B) = {
          (
            a.beforeEach(context.asInstanceOf[a.BeforeEachFixture]),
            b.beforeEach(context.asInstanceOf[b.BeforeEachFixture])
          )
        }
        def afterEach(context: AfterEachFixture): Unit = {
          val (argumentA, argumentB) = context.argument
          a.afterEach(
            context.asInstanceOf[a.AfterEachFixture].withArgument(argumentA)
          )
          b.afterEach(
            context.asInstanceOf[b.AfterEachFixture].withArgument(argumentB)
          )
        }
      }
  }

}
