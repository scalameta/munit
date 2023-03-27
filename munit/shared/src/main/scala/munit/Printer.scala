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
  def height: Int = 100
  def isMultiline(string: String): Boolean =
    string.contains('\n')

  /**
   * Combine two printers into a single printer.
   *
   * Order is important : this printer will be tried first, then the other printer.
   * The new Printer's height will be the max of the two printers' heights.
   * 
   * Comibining two printers can be useful if you want to customize Printer for
   * some types somewhere, but want to add more specialized printers for some tests 
   * 
   * {{{
   * trait MySuites extends FunSuite { 
   *   override val printer = Printer { 
   *     case long => s"${l}L"
   *   }
   * }
   * 
   * trait SomeOtherSuites extends MySuites {
   *   override val printer = Printer {
   *     case m: SomeCaseClass => m.someCustomToString
   *   } orElse super.printer
   * }
   * 
   * }}}
   */
  def orElse(other: Printer): Printer =
    new Printer {
      def print(value: Any, out: StringBuilder, indent: Int): Boolean =
        this.print(value, out, indent) || other.print(
          value,
          out,
          indent
        )
      override def height: Int = this.height.max(other.height)
    }

  val a = Byte
}

object Printer {

  def apply(h: Int)(partialPrint: PartialFunction[Any, String]): Printer =
    new Printer {
      def print(value: Any, out: StringBuilder, indent: Int): Boolean =
        value match {
          case simpleValue =>
            partialPrint.lift.apply(simpleValue).fold(false) { string =>
              out.append(string)
              true
            }
        }

      override def height: Int = h
    }

  /**
   * Utiliy constructor defining a printer for some types.
   *
   * This might be useful for some types which default pretty-printers
   * do not output helpful diffs.
   *
   * {{{
   * type ByteArray = Array[Byte]
   * val listPrinter = Printer {
   *   case ll: ByteArray => ll.map(String.format("%02x", b)).mkString(" ")
   * }
   * val bytes = Array[Byte](1, 5, 8, 24)
   * Printers.print(bytes, listPrinter) // "01 05 08 18"
   * }}}
   */
  def apply(partialPrint: PartialFunction[Any, String]): Printer =
    apply(100)(partialPrint)
}

/** Default printer that does not customize the pretty-printer */
object EmptyPrinter extends Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean = false
}
