package object munit {
  val Ignore = new Tag("Ignore")
  val Only = new Tag("Only")
  val Flaky = new Tag("Flaky")
  val Fail = new Tag("Fail")
  val Slow = new Tag("Slow")

  @deprecated("use BeforeEach instead", "1.0.0")
  type GenericBeforeEach[T] = BeforeEach

  @deprecated("use AfterEach instead", "1.0.0")
  type GenericAfterEach[T] = AfterEach

  @deprecated("use Test instead", "1.0.0")
  type GenericTest[T] = Test
}
