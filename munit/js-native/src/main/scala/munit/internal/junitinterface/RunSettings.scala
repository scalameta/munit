/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import scala.util.Try

final class RunSettings(
    val color: Boolean,
    decodeScalaNames: Boolean,
    val logMode: RunSettings.LogMode,
    val logAssert: Boolean,
    val notLogExceptionClass: Boolean,
    val useSbtLoggers: Boolean,
    val useBufferedLogger: Boolean,
    val trimStackTraces: Boolean,
    val tags: TagsFilter,
) extends Settings {
  def shouldLog(value: RunSettings.LogMode): Boolean = logMode.severity >=
    value.severity

  def shouldLogWarn: Boolean = shouldLog(RunSettings.LogMode.Warn)
  def shouldLogInfo: Boolean = shouldLog(RunSettings.LogMode.Info)
  def shouldLogDebug: Boolean = shouldLog(RunSettings.LogMode.Debug)
  def shouldLogTrace: Boolean = shouldLog(RunSettings.LogMode.Trace)

  def decodeName(name: String): String =
    if (decodeScalaNames) Try(scala.reflect.NameTransformer.decode(name))
      .getOrElse(name)
    else name
}

object RunSettings {
  sealed abstract class LogMode(val severity: Int)

  object LogMode {
    case object Error extends LogMode(1)

    case object Warn extends LogMode(2)

    case object Info extends LogMode(3)

    case object Debug extends LogMode(4)

    case object Trace extends LogMode(5)

    def parse(mode: String): LogMode = mode.toLowerCase match {
      case "error" | "failure" => Error
      case "warn" | "ignored" | "skipped" => Warn
      case "info" => Info
      case "debug" | "success" => Debug
      case "trace" => Trace
      case _ => throw new IllegalArgumentException(
          s"Invalid --log mode '$mode'. Supported values: error, warn, info, debug, trace"
        )
    }
  }
}
