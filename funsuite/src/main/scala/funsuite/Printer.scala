package funsuite

trait Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean
}

object EmptyPrinter extends Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean = false
}
