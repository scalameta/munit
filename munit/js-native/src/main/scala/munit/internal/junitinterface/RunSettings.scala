/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import sbt.testing.Status

import scala.util.Try

final class RunSettings(
    val color: Boolean,
    decodeScalaNames: Boolean,
    val verbose: Boolean,
    val logMode: RunSettings.LogMode,
    val logAssert: Boolean,
    val notLogExceptionClass: Boolean,
    val useSbtLoggers: Boolean,
    val trimStackTraces: Boolean,
    val tags: TagsFilter,
) extends Settings {
  def shouldLog(status: Status): Boolean = logMode.shouldLog(status)

  def shouldLogSuccess: Boolean = logMode.shouldLogSuccess

  def decodeName(name: String): String =
    if (decodeScalaNames) Try(scala.reflect.NameTransformer.decode(name))
      .getOrElse(name)
    else name
}

object RunSettings {
  sealed abstract class LogMode {
    final def shouldLogSuccess: Boolean = this == LogMode.Success

    def shouldLog(status: Status): Boolean
  }

  object LogMode {
    case object Failure extends LogMode {
      override def shouldLog(status: Status): Boolean =
        status == Status.Failure || status == Status.Error
    }

    case object Ignored extends LogMode {
      override def shouldLog(status: Status): Boolean =
        status == Status.Ignored || status == Status.Skipped ||
          status == Status.Failure || status == Status.Error
    }

    case object Success extends LogMode {
      override def shouldLog(status: Status): Boolean = true
    }

    def parse(mode: String): LogMode = mode match {
      case "failure" => Failure
      case "ignored" | "skipped" => Ignored
      case "success" => Success
      case _ => throw new IllegalArgumentException(
          s"Invalid --log mode '$mode'. Supported values: failure, ignored, skipped, success"
        )
    }
  }
}
