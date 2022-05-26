package munit.internal

import munit.{Clue, Location}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.macros.{blackbox, ParseException, TypecheckException}

object MacroCompatScala2 {

  def locationImpl(c: blackbox.Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(c.mirror.staticClass(classOf[Location].getName), path, line)
  }

  def clueImpl(c: blackbox.Context)(value: c.Tree): c.Tree = {
    import c.universe._
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

    @tailrec
    def simplifyType(tpe: Type): Type = tpe match {
      case TypeRef(ThisType(pre), sym, args) if pre == sym.owner =>
        simplifyType(c.internal.typeRef(NoPrefix, sym, args))
      case t =>
        // uncomment to debug:
        // Printers.log(t)(Location.empty)
        t.widen
    }
    val source = Literal(Constant(text))
    val valueType = Literal(Constant(simplifyType(value.tpe).toString))
    New(
      c.internal.typeRef(
        NoPrefix,
        c.mirror.staticClass(classOf[Clue[_]].getName),
        List(value.tpe.widen)
      ),
      source,
      value,
      valueType
    )
  }

  def assertCompileWithDefaultClueImpl(
      c: blackbox.Context
  )(code: c.Expr[String])(loc: c.Expr[Location]): c.Expr[Unit] =
    assertCompileImpl(c)(
      code = code,
      clue = c.universe.reify("code does not compile")
    )(loc)

  def assertCompileImpl(
      c: blackbox.Context
  )(code: c.Expr[String], clue: c.Expr[Any])(
      loc: c.Expr[Location]
  ): c.Expr[Unit] = {
    import c.universe._
    compileErrorsImpl(c)(code).tree match {
      case Literal(Constant(errors: String)) if errors.nonEmpty =>
        c.Expr[Unit](
          q"""
              munit.internal.console.StackTraces.dropInside {
                munit.Assertions.fail(
                  munit.Assertions.munitPrint($clue) + "\n\n" + $errors
                )($loc)
              }
           """
        )
      case _ => reify(())
    }
  }

  def compileErrorsImpl(
      c: blackbox.Context
  )(code: c.Expr[String]): c.Expr[String] = {
    import c.universe._

    val toParse: String = code.tree match {
      case Literal(Constant(literal: String)) => literal
      case tree =>
        c.abort(
          tree.pos,
          "cannot compile dynamic expressions, only constant literals.\n" +
            "To fix this problem, pass in a string literal in double quotes \"...\""
        )
    }

    def formatError(
        message: String,
        pos: scala.reflect.api.Position
    ): String =
      new mutable.StringBuilder()
        .append("error:")
        .append(if (message.contains('\n')) "\n" else " ")
        .append(message)
        .append("\n")
        .append(pos.source.lineToString(pos.source.offsetToLine(pos.point)))
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
          formatError(e.getMessage, e.pos)
        case e: TypecheckException =>
          formatError(e.getMessage, e.pos)
      }

    c.Expr(Literal(Constant(message)))
  }
}
