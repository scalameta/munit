import munit.internal.junitinterface.{PendingCommentTag, PendingTag}

package object munit {
  case class PendingComment(override val value: String)
      extends Tag(value)
      with PendingCommentTag

  val Ignore = new Tag("Ignore")
  val Only = new Tag("Only")
  val Flaky = new Tag("Flaky")
  val Fail = new Tag("Fail")
  val Pending: Tag with PendingTag = new Tag("Pending") with PendingTag
  val Slow = new Tag("Slow")
}
