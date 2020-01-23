package munit

// Framework tests needs to be manually added to FrameworkSuite.tests
class FrameworkTest(
    val cls: Class[_ <: FunSuite],
    val expected: String,
    val format: FrameworkTestFormat = SbtFormat,
    val arguments: Array[String] = Array()
)(implicit val location: Location)

sealed abstract class FrameworkTestFormat
case object SbtFormat extends FrameworkTestFormat
case object StdoutFormat extends FrameworkTestFormat
