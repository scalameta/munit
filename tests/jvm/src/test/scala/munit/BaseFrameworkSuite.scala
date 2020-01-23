package munit

import java.io.ByteArrayOutputStream
import sbt.testing.TaskDef
import sbt.testing.EventHandler
import sbt.testing.Event
import java.io.PrintStream
import com.geirsson.junit.Ansi
import java.nio.charset.StandardCharsets
import sbt.testing.Logger
import scala.util.control.NonFatal
import java.util.regex.Pattern

abstract class BaseFrameworkSuite extends FunSuite {
  val systemOut = System.out
  override def munitIgnore: Boolean = !BuildInfo.scalaVersion.startsWith("2.13")
  def exceptionMessage(ex: Throwable): String = {
    if (ex.getMessage() == null) "null"
    else {
      ex.getMessage()
        .replaceAllLiterally(
          BuildInfo.sourceDirectory.toString(),
          ""
        )
        .replace('\\', '/')
    }
  }
  override def afterEach(context: AfterEach): Unit = {
    System.setOut(systemOut)
  }

  def check(t: FrameworkTest): Unit = {
    test(t.cls.getSimpleName()) {
      val baos = new ByteArrayOutputStream()
      val out = new PrintStream(baos)
      System.setOut(out)
      val logger = new Logger {
        def ansiCodesSupported(): Boolean = false
        def error(x: String): Unit = out.println(x)
        def warn(x: String): Unit = out.println(x)
        def info(x: String): Unit = out.println(x)
        def debug(x: String): Unit = () // ignore debugging output
        def trace(x: Throwable): Unit = out.println(x)
      }
      val framework = new Framework
      val runner = framework.runner(
        t.arguments ++ Array("+l"), // use sbt loggers
        Array(),
        this.getClass().getClassLoader()
      )
      val tasks = runner.tasks(
        Array(
          new TaskDef(
            t.cls.getName(),
            framework.munitFingerprint,
            false,
            Array()
          )
        )
      )
      val events = new StringBuilder()
      val eventHandler = new EventHandler {
        def handle(event: Event): Unit = {
          try {
            val status = event.status().toString().toLowerCase()
            val name = event.fullyQualifiedName()
            events
              .append("==> ")
              .append(status)
              .append(" ")
              .append(name)
            if (event.throwable().isDefined()) {
              events
                .append(" - ")
                .append(exceptionMessage(event.throwable().get()))
            }
            events.append("\n")
          } catch {
            case NonFatal(e) =>
              e.printStackTrace()
              events.append(s"unexpected error: $e")
          }
        }
      }
      val elapsedTimePattern =
        Pattern.compile(" \\d+\\.\\d+s$", Pattern.MULTILINE)
      tasks.foreach(_.execute(eventHandler, Array(logger)))
      val stdout = Ansi
        .filterAnsi(baos.toString(StandardCharsets.UTF_8.name()))
      val obtained = Ansi.filterAnsi(
        t.format match {
          case SbtFormat =>
            events.toString().replaceAllLiterally("\"\"\"", "'''")
          case StdoutFormat =>
            elapsedTimePattern.matcher(stdout).replaceAll(" <elapsed time>")
        }
      )
      assertNoDiff(
        obtained,
        t.expected,
        stdout
      )(t.location)
    }(t.location)
  }
}
