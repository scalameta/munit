package munit.sbtmunit

object MUnitTestReport {
  case class Summary(
      repository: String,
      ref: String,
      sha: String,
      timestamp: String,
      scalaVersion: String,
      projectName: String,
      javaVersion: String,
      os: String,
      groups: Array[Group]
  )
  case class Group(
      name: String,
      result: String,
      events: Array[TestEvent]
  )
  case class TestEvent(
      status: String,
      name: String,
      // NOTE(olafur): this field should be typed as Double to match JSON types
      // but then all numbers get formatted as `2.0` with a redundant `.0`
      // suffix.
      duration: Long,
      exception: TestException
  )
  case class TestException(
      className: String,
      message: String,
      stack: Array[String],
      cause: TestException
  )
}
