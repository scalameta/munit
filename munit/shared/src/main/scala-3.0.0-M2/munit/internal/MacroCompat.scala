package munit.internal

import munit.Clue
import munit.Location
import scala.quoted._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.macros.TypecheckException
import scala.reflect.macros.ParseException

object MacroCompat {

  trait LocationMacro {
    inline implicit def generate: Location = ${ locationImpl() }
    implicit def generate: Location = macro locationImplScala2
  }

  def locationImplScala2(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(c.mirror.staticClass(classOf[Location].getName()), path, line)
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
    implicit def generate[T](value: T): Clue[T] = macro clueImplScala2
  }

  def clueImpl[T: Type](value: Expr[T])(using Quotes): Expr[Clue[T]] = {
    import quotes.reflect._
    val source = Term.of(value).pos.sourceCode
    val valueType = Type.show[T]
    '{ new Clue(${Expr(source)}, $value, ${Expr(valueType)}) }
  }

  def clueImplScala2(c: Context)(value: c.Tree): c.Tree = {
    import c.universe._
    import compat._
    val text: String =
      if (value.pos != null && value.pos.isRange) {
        val chars = value.pos.source.content
        val start = value.pos.start
        val end = value.pos.end
        if (
          end > start &&
          start >= 0 && start < chars.length &&
          end >= 0 && end < chars.length
        ) {
          new String(chars, start, end - start)
        } else {
          ""
        }
      } else {
        ""
      }
    def simplifyType(tpe: Type): Type = tpe match {
      case TypeRef(ThisType(pre), sym, args) if pre == sym.owner =>
        simplifyType(TypeRef(NoPrefix, sym, args))
      case t =>
        // uncomment to debug:
        // Printers.log(t)(Location.empty)
        t.widen
    }
    val source = Literal(Constant(text))
    val valueType = Literal(Constant(simplifyType(value.tpe).toString()))
    New(
      TypeRef(NoPrefix, c.mirror.staticClass(classOf[Clue[_]].getName), List(value.tpe.widen)),
      source,
      value,
      valueType
    )
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
    def compileErrors(code: String): String = macro compileErrorsImplScala2
  }

  def compileErrorsImplScala2(c: Context)(code: c.Tree): c.Tree = {
    import c.universe._
    val toParse: String = code match {
      case Literal(Constant(literal: String)) => literal
      case _ =>
        c.abort(
          code.pos,
          "cannot compile dynamic expressions, only constant literals.\n" +
            "To fix this problem, pass in a string literal in double quotes \"...\""
        )
    }

    def formatError(message: String, pos: scala.reflect.api.Position): String =
      new StringBuilder()
        .append("error:")
        .append(if (message.contains('\n')) "\n" else " ")
        .append(message)
        .append("\n")
        .append(pos.lineContent)
        .append("\n")
        .append(" " * (pos.column - 1))
        .append("^")
        .toString()

    val message: String =
      try {
        c.typecheck(c.parse(s"{\n$toParse\n}"))
        ""
      } catch {
        case e: ParseException =>
          formatError(e.getMessage(), e.pos)
        case e: TypecheckException =>
          formatError(e.getMessage(), e.pos)
      }
    Literal(Constant(message))
  }

}
