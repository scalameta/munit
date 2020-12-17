package munit.internal

import munit.Clue
import munit.Location
import scala.quoted._

object MacroCompat {

  trait LocationMacro {
    inline implicit def generate: Location = ${ locationImpl() }
  }

  def locationImpl()(using Quotes): Expr[Location] = {
    import quotes.reflect._
    val pos = Position.ofMacroExpansion
    val path = pos.sourceFile.jpath.toString
    val startLine = pos.startLine + 1
    '{ new Location(${Expr(path)}, ${Expr(startLine)}) }
  }

  trait ClueMacro {
    inline implicit def generate[T](value: T): Clue[T] = ${ clueImpl('value) }
  }

  def clueImpl[T: Type](value: Expr[T])(using Quotes): Expr[Clue[T]] = {
    import quotes.reflect._
    val source = Term.of(value).pos.sourceCode.getOrElse("")
    val valueType = TypeTree.of[T].show(using Printer.TreeShortCode)
    '{ new Clue(${Expr(source)}, $value, ${Expr(valueType)}) }
  }

  trait CompileErrorMacro {
    inline def compileErrors(inline code: String): String = {
      val errors = scala.compiletime.testing.typeCheckErrors(code)
      errors.map { error =>
        val indent = " " * (error.column - 1)
        val trimMessage = error.message.linesIterator.map { line =>
          if (line.matches(" +")) ""
          else line
        }.mkString("\n")
        val separator = if (error.message.contains('\n')) "\n" else " "
        s"error:${separator}${trimMessage}\n${error.lineContent}\n${indent}^"
      }.mkString("\n")
    }
  }

}
