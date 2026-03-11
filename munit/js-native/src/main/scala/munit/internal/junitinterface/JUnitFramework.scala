/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import sbt.testing._

abstract class JUnitFramework extends Framework {

  override def name(): String = "Scala.js JUnit test framework"

  def customRunners: CustomRunners

  def fingerprints(): Array[Fingerprint] = customRunners.runners.toArray

  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
  ): Runner = new JUnitRunner(
    args,
    remoteArgs,
    parseRunSettings(args),
    testClassLoader,
    customRunners,
  )

  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit,
  ): Runner = new JUnitRunner(
    args,
    remoteArgs,
    parseRunSettings(args),
    testClassLoader,
    customRunners,
  )

  private def parseRunSettings(args: Array[String]): RunSettings = {
    val defaults = Settings.defaults()
    var verbose = false
    var logMode: RunSettings.LogMode = null
    var noColor = false
    var decodeScalaNames = false
    var logAssert = false
    var notLogExceptionClass = false
    var useSbtLoggers = false
    var useBufferedLogger = false
    var trimStackTraces = defaults.trimStackTraces
    var includeTags = Set.empty[String]
    var excludeTags = Set.empty[String]
    for (str <- args) {
      def unsupported: Nothing =
        throw new UnsupportedOperationException(s"Invalid munit parameter: $str")
      if (!str.startsWith("-") && !str.startsWith("+"))
        throw new UnsupportedOperationException(str)
      val sep = str.indexOf('=')
      def value = str.substring(sep + 1)
      if (sep < 0) str match {
        case "-v" => verbose = true
        case "-n" => noColor = true
        case "-s" => decodeScalaNames = true
        case "-a" => logAssert = true
        case "-c" => notLogExceptionClass = true
        case _ =>
      }
      else str.substring(0, sep) match {
        case "--log" => logMode = RunSettings.LogMode.parse(value)
        case "--exclude-tags" => excludeTags += value
        case "--include-tags" => includeTags += value
        case "--logger" => value.toLowerCase match {
            case "sbt" => useSbtLoggers = true
            case "buffered" => useBufferedLogger = true
            case _ => unsupported
          }
        case _ => unsupported
      }
    }
    for (s <- args) s match {
      case "+v" => verbose = false
      case "+n" => noColor = false
      case "+s" => decodeScalaNames = false
      case "+a" => logAssert = false
      case "+c" => notLogExceptionClass = false
      case "+l" => useSbtLoggers = true
      case "-l" => useSbtLoggers = false
      case "+F" => trimStackTraces = true
      case "-F" => trimStackTraces = false
      case _ =>
    }
    if (logMode eq null) logMode =
      if (verbose) RunSettings.LogMode.Trace else RunSettings.LogMode.Info
    new RunSettings(
      color = !noColor,
      decodeScalaNames = decodeScalaNames,
      logMode = logMode,
      logAssert = logAssert,
      notLogExceptionClass = notLogExceptionClass,
      useSbtLoggers = useSbtLoggers,
      useBufferedLogger = useBufferedLogger,
      trimStackTraces = trimStackTraces,
      tags = new TagsFilter(includeTags, excludeTags),
    )
  }
}
