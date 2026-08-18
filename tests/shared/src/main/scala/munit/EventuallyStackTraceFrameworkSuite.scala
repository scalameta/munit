package munit

import scala.concurrent.duration._

class EventuallyStackTraceFrameworkSuite extends FunSuite {
  private implicit val options: EventuallyOptions = EventuallyOptions(4, 1.milli)
  test("fail")(eventually(assertEquals(1, 2)))
}

// Retrying must stay out of the reported stack trace: these are the frames a
// plain failing assertion produces, with no trace of the retry loop.
object EventuallyStackTraceFrameworkSuite
    extends FrameworkTest(
      classOf[EventuallyStackTraceFrameworkSuite],
      """|at munit.FunSuite:assertEquals
         |  at munit.EventuallyStackTraceFrameworkSuite:$anonfun$new$2
         |  at scala.runtime.java8.JFunction0$mcV$sp:apply
         |==> failure munit.EventuallyStackTraceFrameworkSuite.fail - tests/shared/src/main/scala/munit/EventuallyStackTraceFrameworkSuite.scala:7
         |6:  private implicit val options: EventuallyOptions = EventuallyOptions(4, 1.milli)
         |7:  test("fail")(eventually(assertEquals(1, 2)))
         |8:}
         |values are not the same
         |=> Obtained
         |1
         |=> Diff (- expected, + obtained)
         |-2
         |+1
         |""".stripMargin,
      tags = Set(OnlyJVM),
      onEvent = { event =>
        if (event.throwable().isDefined()) {
          val s = event.throwable().get().getStackTrace()
          s.map(e => s"  at ${e.getClassName()}:${e.getMethodName()}")
            .mkString("", "\n", "\n")
        } else ""
      },
    )
