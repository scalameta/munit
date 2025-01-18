package munit.internal

import munit.Clue
import munit.Location

import scala.reflect.macros.ParseException
import scala.reflect.macros.TypecheckException
import scala.reflect.macros.blackbox.Context

object MacroCompatScala2 {

  def locationImpl(c: Context): c.Tree = {
    import c.universe._
    val workingDirectory: String = sys.props("user.dir") +
      java.io.File.separator
    val line = Literal(Constant(c.enclosingPosition.line))
    val path0 = c.enclosingPosition.source.path
    val relativePath =
      if (path0.startsWith(workingDirectory)) path0.drop(workingDirectory.length)
      else path0
    val path = Literal(Constant(relativePath))
    New(c.mirror.staticClass(classOf[Location].getName()), path, line)
  }

  def clueImpl(c: Context)(value: c.Tree): c.Tree = {
    import c.universe._
    val text: String =
      if (value.pos != null && value.pos.isRange) {
        val chars = value.pos.source.content
        val start = value.pos.start
        val end = value.pos.end
        if (
          end > start && start >= 0 && start < chars.length && end >= 0 &&
          end < chars.length
        ) new String(chars, start, end - start)
        else ""
      } else ""
    def simplifyType(tpe: Type): Type = tpe match {
      case TypeRef(ThisType(pre), sym, args) if pre == sym.owner =>
        simplifyType(c.internal.typeRef(NoPrefix, sym, args))
      case t =>
        // uncomment to debug:
        // Printers.log(t)(Location.empty)
        t.widen
    }
    val source = Literal(Constant(text))
    val valueType = Literal(Constant(simplifyType(value.tpe).toString()))
    New(
      c.internal.typeRef(
        NoPrefix,
        c.mirror.staticClass(classOf[Clue[_]].getName()),
        List(value.tpe.widen),
      ),
      source,
      value,
      valueType,
    )
  }

  def compileErrorsImpl(c: Context)(code: c.Tree): c.Tree = {
    import c.universe._
    val toParse: String = code match {
      case Literal(Constant(literal: String)) => literal
      case _ => c.abort(
          code.pos,
          "cannot compile dynamic expressions, only constant literals.\n" +
            "To fix this problem, pass in a string literal in double quotes \"...\"",
        )
    }

    def formatError(message: String, pos: scala.reflect.api.Position): String =
      new StringBuilder().append("error:")
        .append(if (message.contains('\n')) "\n" else " ").append(message)
        .append("\n").append(pos.lineContent).append("\n")
        .append(" " * (pos.column - 1)).append("^").toString()

    val message: String =
      try {
        c.typecheck(c.parse(s"{\n$toParse\n}"))
        ""
      } catch {
        case e: ParseException => formatError(e.getMessage(), e.pos)
        case e: TypecheckException => formatError(e.getMessage(), e.pos)
      }
    Literal(Constant(message))
  }
}
