package munit

class TypeCheckSuite extends FunSuite {

  def check(
      options: TestOptions,
      obtained: String,
      compat: Map[String, String]
  )(implicit loc: Location): Unit = {
    test(options) {
      val split = BuildInfo.scalaVersion.split("\\.")
      val binaryVersion = split.take(2).mkString(".")
      val majorVersion = split.head match {
        case "0" => "3"
        case n   => n
      }
      val expected = compat
        .get(BuildInfo.scalaVersion)
        .orElse(compat.get(binaryVersion))
        .orElse(compat.get(majorVersion))
        .getOrElse {
          compat(BuildInfo.scalaVersion)
        }
      assertNoDiff(obtained, expected)(loc)
    }
  }

  val msg = "Hello"
  check(
    "not a member",
    compileErrors("msg.foobar"),
    Map(
      "2" ->
        """|error: value foobar is not a member of String
           |msg.foobar
           |    ^
           |""".stripMargin,
      "3" ->
        """|error:
           |value foobar is not a member of String, but could be made available as an extension method.
           |
           |The following import might fix the problem:
           |
           |  import munit.Clue.generate
           |
           |msg.foobar
           |   ^
           |""".stripMargin
    )
  )

  check(
    "parse error",
    compileErrors("val x: = 2"),
    Map(
      "2" -> """|error: identifier expected but '=' found.
                |val x: = 2
                |       ^
                |""".stripMargin,
      "3" ->
        // NOTE(olafur): I'm not sure what's going on with the second errors but
        // that's what Dotty reports.
        """|error: an identifier expected, but eof found
           |val x: = 2
           |      ^
           |error: Declaration of value x not allowed here: only classes can have declared but undefined members
           |package munit
           |   ^
           |""".stripMargin
    )
  )

  check(
    "type mismatch",
    compileErrors("val n: Int = msg"),
    Map(
      "2" ->
        """|error:
           |type mismatch;
           | found   : String
           | required: Int
           |val n: Int = msg
           |             ^
           |""".stripMargin,
      "3" ->
        """|error:
           |Found:    (TypeCheckSuite.this.msg : String)
           |Required: Int
           |
           |The following import might make progress towards fixing the problem:
           |
           |  import munit.Clue.generate
           |
           |val n: Int = msg
           |            ^
           |""".stripMargin
    )
  )
}
