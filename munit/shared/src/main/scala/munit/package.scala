package object munit {
  case class PendingComment(override val value: String) extends Tag(value)

  val Ignore = new Tag("Ignore")
  val Only = new Tag("Only")
  val Flaky = new Tag("Flaky")
  val Fail = new Tag("Fail")
  val Pending = new Tag("Pending")
  val Slow = new Tag("Slow")
}
