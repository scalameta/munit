package munit

import java.io.ByteArrayOutputStream
import sbt.testing.TaskDef
import sbt.testing.EventHandler
import sbt.testing.Event
import java.io.PrintStream
import com.geirsson.junit.Ansi
import java.nio.charset.StandardCharsets
import sbt.testing.Logger
import sbt.testing.Status
import scala.util.Properties

class FrameworkSuite extends BaseFrameworkSuite {
  check(
    classOf[CiOnlyFrameworkSuite],
    """|==> failure munit.CiOnlyFrameworkSuite.only - /scala/munit/CiOnlyFrameworkSuite.scala:5 'Only' tag is not allowed when `isCI=true`
       |4:   override def isCI: Boolean = true
       |5:   test("only".only) {
       |6:     println("pass")
       |""".stripMargin
  )

  check(
    classOf[DiffProductFrameworkSuite],
    """|==> failure munit.DiffProductFrameworkSuite.pass - /scala/munit/DiffProductFrameworkSuite.scala:11 values are not the same
       |=> Obtained
       |User(
       |  name = "John",
       |  age = 43,
       |  friends = List(
       |    2
       |  )
       |)
       |=> Diff (- obtained, + expected)
       |   name = "John",
       |-  age = 43,
       |+  age = 42,
       |   friends = List(
       |+    1,
       |     2
       |10:     val john2 = User("John", 43, 2.to(2).toList)
       |11:     assertEqual(john2, john)
       |12:   }
       |""".stripMargin
  )

  check(
    classOf[FailFrameworkSuite],
    """|==> failure munit.FailFrameworkSuite.pass - /scala/munit/FailFrameworkSuite.scala:4 expected failure but test passed
       |3: class FailFrameworkSuite extends FunSuite {
       |4:   test("pass".fail) {
       |5:     // println("pass")
       |==> success munit.FailFrameworkSuite.fail
       |""".stripMargin
  )

  check(
    classOf[DuplicateNameFrameworkSuite],
    """|==> success munit.DuplicateNameFrameworkSuite.basic
       |==> success munit.DuplicateNameFrameworkSuite.basic-1
       |""".stripMargin
  )
}
