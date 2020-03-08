package munit

trait FunFixtures { self: FunSuite =>

  class FunFixture[T](
      val setup: TestOptions => T,
      val teardown: T => Unit
  ) {
    def test(options: TestOptions)(
        body: T => Any
    )(implicit loc: Location): Unit = {
      self.test(options) {
        val argument = setup(options)
        try body(argument)
        finally teardown(argument)
      }(loc)
    }
  }
  object FunFixture {
    def map2[A, B](a: FunFixture[A], b: FunFixture[B]): FunFixture[(A, B)] =
      new FunFixture[(A, B)](
        setup = { options =>
          (a.setup(options), b.setup(options))
        },
        teardown = {
          case (argumentA, argumentB) =>
            try a.teardown(argumentA)
            finally b.teardown(argumentB)
        }
      )
    def map3[A, B, C](
        a: FunFixture[A],
        b: FunFixture[B],
        c: FunFixture[C]
    ): FunFixture[(A, B, C)] =
      new FunFixture[(A, B, C)](
        setup = { options =>
          (a.setup(options), b.setup(options), c.setup(options))
        },
        teardown = {
          case (argumentA, argumentB, argumentC) =>
            try a.teardown(argumentA)
            finally {
              try b.teardown(argumentB)
              finally c.teardown(argumentC)
            }
        }
      )
  }

}
