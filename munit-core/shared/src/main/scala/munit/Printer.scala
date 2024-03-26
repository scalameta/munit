package munit

/**
 * Implement this trait to customize the default printer
 */
trait Printer {

  /**
   * Pretty-print a single value during pretty printing.
   *
   * Returns true if this value has been printed, false if FunSuite should fallback to the default pretty-printer.
   */
  def print(value: Any, out: StringBuilder, indent: Int): Boolean
  def height: Int = Printer.defaultHeight
  def isMultiline(string: String): Boolean =
    string.contains('\n')

  /**
   * Combine two printers into a single printer.
   *
   * Order is important : this printer will be tried first, then the other printer.
   * The new Printer's height will be the max of the two printers' heights.
   *
   * Example use case :  define some default printers for some types for all tests,
   * and override it for some tests only.
   *
   * {{{
   *
   * case class Person(name: String, age: Int, mail: String)
   *
   * trait MySuites extends FunSuite {
   *   override val printer = Printer.apply {
   *     case Person(name, age, mail) => s"$name:$age:$mail"
   *     case m: SomeOtherCaseClass => m.someCustomToString
   *   }
   * }
   *
   * trait CompareMailsOnly extends MySuites {
   *   val mailOnlyPrinter = Printer.apply {
   *     case Person(_, _, mail) => mail
   *   }
   *   override val printer =  mailOnlyPrinterPrinter orElse super.printer
   * }
   *
   * }}}
   */
  def orElse(other: Printer): Printer = {
    val h = this.height
    val p: (Any, StringBuilder, Int) => Boolean = this.print
    new Printer {
      def print(value: Any, out: StringBuilder, indent: Int): Boolean =
        p.apply(value, out, indent) || other.print(
          value,
          out,
          indent
        )
      override def height: Int = h.max(other.height)
    }
  }

}

object Printer {

  val defaultHeight = 100

  def apply(
      height: Int
  )(partialPrint: PartialFunction[Any, String]): Printer = {
    val h = height
    new Printer {
      def print(value: Any, out: StringBuilder, indent: Int): Boolean = {
        partialPrint.lift.apply(value) match {
          case Some(string) =>
            out.append(string)
            true
          case None => false
        }
      }

      override def height: Int = h
    }
  }

  /**
   * Utiliy constructor defining a printer for some types.
   *
   * Example use case is overriding the string repr for types which default pretty-printers
   * do not output helpful diffs.
   *
   * {{{
   * type ByteArray = Array[Byte]
   * val listPrinter = Printer.apply {
   *   case ll: ByteArray => ll.map(String.format("%02x", b)).mkString(" ")
   * }
   * val bytes = Array[Byte](1, 5, 8, 24)
   * Printers.print(bytes, listPrinter) // "01 05 08 18"
   * }}}
   */
  def apply(partialPrint: PartialFunction[Any, String]): Printer =
    apply(defaultHeight)(partialPrint)
}

/** Default printer that does not customize the pretty-printer */
object EmptyPrinter extends Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean = false
}
