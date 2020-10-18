package munit.internal

import munit.Clue
import munit.Compare
import munit.Location
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.macros.TypecheckException
import scala.reflect.macros.ParseException
import scala.collection.mutable

object MacroCompat {

  trait CompareMacro {

    /**
     * Implements equality according to `==` for any two types (including
     * unrelated types) as long as the compiler option
     * "-Xmacro-settings:munit.strictEquality" is not enabled.
     */
    implicit def defaultCompareImplicit[A, B]: Compare[A, B] =
      macro compareImpl[A, B]
  }

  private val strictEqualityFlag = "munit.strictEquality"
  private var strictEqualityCache: Option[Boolean] = None
  private var strictEqualityError: Option[Context => Unit] = None
  private def isStrictEqualityEnabled(c: Context): Boolean = {
    strictEqualityCache match {
      case Some(cachedValue) =>
        strictEqualityError.foreach(fn => fn(c))
        cachedValue
      case _ =>
        val result = c.settings.contains(strictEqualityFlag)
        c.settings.foreach { setting =>
          if (setting.startsWith("munit.") && setting != strictEqualityFlag) {
            strictEqualityError = Some(context =>
              context.error(
                context.enclosingPosition,
                s"unknown flag '-Xmacro-setting:$setting'. Did you mean -Xmacro-setting:$strictEqualityFlag?"
              )
            )
          }
        }
        strictEqualityCache = Some(result)
        isStrictEqualityEnabled(c)
    }
  }

  def compareImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](
      c: Context
  ): c.Expr[Compare[A, B]] = {
    import c.universe._
    if (!isStrictEqualityEnabled(c)) {
      reify(Compare.defaultCompare[A, B])
    } else {
      val A = weakTypeOf[A]
      val B = weakTypeOf[B]
      val solutions = mutable.ListBuffer.empty[String]
      val lineContent = c.enclosingPosition.lineContent
      solutions += s"provide an implicit instance of type Equality[$A, $B]"

      if (A =:= B) ()
      else if (lineContent.contains("assertEquals"))
        solutions += "use assertEquals[Any, Any](...) if you think it's OK to compare these types at runtime"
      else if (lineContent.contains("assertNotEquals"))
        solutions += "use assertNotEquals[Any, Any](...) if you think it's OK to compare these types at runtime"

      solutions += s"""disable strict equality by removing the compiler option "-Xmacro-settings:${strictEqualityFlag}""""

      val allSolutions = solutions.zipWithIndex
        .map { case (msg, i) => s"  Alternative ${i + 1}: $msg" }
        .mkString("\n")

      c.error(
        c.enclosingPosition,
        s"""Can't compare these two types when using strict equality.
           |  First type:  $A
           |  Second type: $B
           |Possible ways to fix this problem:
           |$allSolutions
           |""".stripMargin
      )
      reify[Compare[A, B]](???)
    }
  }

  trait LocationMacro {
    implicit def generate: Location = macro locationImpl
  }

  def locationImpl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }

  trait ClueMacro {
    implicit def generate[T](value: T): Clue[T] = macro clueImpl
  }

  def clueImpl(c: Context)(value: c.Tree): c.Tree = {
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
      TypeRef(NoPrefix, typeOf[Clue[_]].typeSymbol, List(value.tpe.widen)),
      source,
      value,
      valueType
    )
  }

  trait CompileErrorMacro {
    def compileErrors(code: String): String = macro compileErrorsImpl
  }

  def compileErrorsImpl(c: Context)(code: c.Tree): c.Tree = {
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
