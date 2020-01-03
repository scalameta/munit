package funsuite.internal

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import fansi.ErrorMode.Throw

class OutputCapture(
    val originalOut: PrintStream,
    val originalScalaOut: PrintStream
) {
  val buffer = new ByteArrayOutputStream
  val prBuffer = new PrintStream(buffer, true)
  def stop(): Unit = {
    System.out.flush()
    System.setOut(originalOut)
    trySetConsoleOut()
  }
  def replay(): Unit = {
    System.out.write(buffer.toByteArray())
    System.out.flush()
  }
  private def trySetConsoleOut(): Unit = {
    try {
      val setOutDirect =
        Console.getClass().getMethod("setOutDirect", classOf[PrintStream])
      setOutDirect.invoke(null, originalScalaOut)
    } catch {
      case ex: Throwable =>
        () // ignore
    }
  }
}
object OutputCapture {
  def start(): OutputCapture = {
    val capture = new OutputCapture(System.out, Console.out)
    System.out.flush()
    System.setOut(capture.prBuffer)
    capture
  }
}
