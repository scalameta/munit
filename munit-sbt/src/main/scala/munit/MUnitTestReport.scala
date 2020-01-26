package munit

object MUnitTestReport {
  case class TestReport(
      repository: String,
      runId: String,
      timestamp: String,
      scalaVersion: String,
      projectName: String,
      javaVersion: String,
      osName: String,
      groups: Array[TestGroup]
  )
  case class TestGroup(
      name: String,
      result: String,
      events: Array[TestGroupEvent]
  )
  case class TestGroupEvent(
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
