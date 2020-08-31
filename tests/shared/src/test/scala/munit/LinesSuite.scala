package munit

class LinesSuite extends FunSuite {

  check(
    "basic",
    "hello",
    Location.generate,
    // comment
    """|LinesSuite.scala:8 hello
       |7:    "hello",
       |8:    Location.generate,
       |9:    // comment
       |""".stripMargin
  )

  check(
    "multiline",
    "hello\nworld!",
    Location.generate,
    // comment
    """|LinesSuite.scala:20
       |19:    "hello\nworld!",
       |20:    Location.generate,
       |21:    // comment
       |hello
       |world!
       |""".stripMargin
  )

  // This method is written at the bottom of the file to keep stable line
  // numbers in the test cases above.
  def check(
      options: TestOptions,
      message: String,
      location: Location,
      expected: String
  )(implicit loc: Location): Unit = {
    test(options) {
      val obtained = munitLines
        .formatLine(location, message)
        .replace(location.path, location.filename)
      assertNoDiff(obtained, expected)
    }
  }
}
