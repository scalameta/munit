package funsuite

import scala.collection.mutable
import scala.util.{Failure, Success}
import funsuite.internal.StackMarker
import funsuite.internal.Colors
import fansi.Bold
import fansi.Str
import fansi.Attrs

object Formatter extends Formatter

/**
  * Default implementation of [[Formatter]], also used by the default SBT test
  * framework. Allows some degree of customization of the formatted test results.
  */
trait Formatter {

  def formatColor: Boolean = true
  def formatTruncateHeight: Int = 15
  def formatWrapWidth: Int =
    Int.MaxValue >> 1 // halving here to avoid overflows later

  def formatValue(x: Any): Str = testValueColor("" + x)

  def toggledColor(t: fansi.Attrs): Attrs =
    if (formatColor) t else fansi.Attrs.Empty
  def testValueColor: Attrs = toggledColor(fansi.Color.Blue)
  def exceptionClassColor: Attrs =
    toggledColor(fansi.Underlined.On ++ fansi.Color.LightRed)
  def exceptionMsgColor: Attrs = toggledColor(fansi.Color.LightRed)
  def exceptionPrefixColor: Attrs = toggledColor(fansi.Color.Red)
  def exceptionMethodColor: Attrs = toggledColor(fansi.Color.LightRed)
  def exceptionPunctuationColor: Attrs = toggledColor(fansi.Color.Red)
  def exceptionLineNumberColor: Attrs = toggledColor(fansi.Color.LightRed)

  def formatResultColor(success: Boolean): Attrs = toggledColor(
    if (success) fansi.Color.Green
    else fansi.Color.Red
  )

  def formatMillisColor: Attrs = toggledColor(Colors.Bold.Faint)

  def exceptionStackFrameHighlighter(s: StackTraceElement): Boolean = true

  def formatException(x: Throwable, leftIndent: String): Str = {
    val output = mutable.Buffer.empty[Str]
    var current = x
    while (current != null) {
      val exCls = exceptionClassColor(current.getClass.getName)
      output.append(
        joinLineStr(
          lineWrapInput(
            current.getMessage match {
              case null => exCls
              case nonNull =>
                Str.join(exCls, ": ", exceptionMsgColor(nonNull))
            },
            leftIndent
          ),
          leftIndent
        )
      )

      val stack = current.getStackTrace

      StackMarker
        .filterCallStack(stack)
        .foreach { e =>
          // Scala.js for some reason likes putting in full-paths into the
          // filename slot, rather than just the last segment of the file-path
          // like Scala-JVM does. This results in that portion of the
          // stacktrace being terribly long, wrapping around and generally
          // being impossible to read. We thus manually drop the earlier
          // portion of the file path and keep only the last segment

          val filenameFrag: Str = e.getFileName match {
            case null => exceptionLineNumberColor("Unknown")
            case fileName =>
              val shortenedFilename = fileName.lastIndexOf('/') match {
                case -1 => fileName
                case n  => fileName.drop(n + 1)
              }
              Str.join(
                exceptionLineNumberColor(shortenedFilename),
                ":",
                exceptionLineNumberColor(e.getLineNumber.toString)
              )
          }

          val frameIndent = leftIndent + "  "
          val wrapper =
            if (exceptionStackFrameHighlighter(e)) fansi.Attrs.Empty
            else Colors.Bold.Faint

          output.append(
            "\n",
            frameIndent,
            joinLineStr(
              lineWrapInput(
                wrapper(
                  Str.join(
                    exceptionPrefixColor(e.getClassName + "."),
                    exceptionMethodColor(e.getMethodName),
                    exceptionPunctuationColor("("),
                    filenameFrag,
                    exceptionPunctuationColor(")")
                  )
                ),
                frameIndent
              ),
              frameIndent
            )
          )
        }
      current = current.getCause
      if (current != null) output.append("\n", leftIndent)
    }

    Str.join(output.toSeq: _*)
  }

  def lineWrapInput(input: Str, leftIndent: String): Seq[Str] = {
    val output = mutable.Buffer.empty[Str]
    val plainText = input.plainText
    var index = 0
    while (index < plainText.length) {
      val nextWholeLine = index + (formatWrapWidth - leftIndent.length)
      val (nextIndex, skipOne) = plainText.indexOf('\n', index + 1) match {
        case -1 =>
          if (nextWholeLine < plainText.length) (nextWholeLine, false)
          else (plainText.length, false)
        case n =>
          if (nextWholeLine < n) (nextWholeLine, false)
          else (n, true)
      }

      output.append(input.substring(index, nextIndex))
      if (skipOne) index = nextIndex + 1
      else index = nextIndex
    }
    output.toSeq
  }

  def joinLineStr(lines: Seq[Str], leftIndent: String): Str = {
    Str.join(
      lines.flatMap(Seq[Str]("\n", leftIndent, _)).drop(2): _*
    )
  }

  def formatIcon(success: Boolean): Str = {
    formatResultColor(success)(if (success) "+" else "X")
  }

}
