package munit

import sbt.testing.Event

// Framework tests needs to be manually added to FrameworkSuite.tests
class FrameworkTest(
    val cls: Class[_ <: FunSuite],
    val expected: String,
    val tags: Set[Tag] = Set.empty,
    val format: FrameworkTestFormat = SbtFormat,
    val arguments: Array[String] = Array(),
    val onEvent: Event => String = _ => ""
)(implicit val location: Location)

sealed abstract class FrameworkTestFormat
case object SbtFormat extends FrameworkTestFormat
case object StdoutFormat extends FrameworkTestFormat
