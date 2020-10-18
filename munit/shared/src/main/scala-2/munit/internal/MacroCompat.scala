package munit.internal

import munit.Clue
import munit.Compare
import munit.Location
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
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
    implicit def generate: Location = macro MacroCompatScala2.locationImpl
  }

  @deprecated("Use MacroCompatScala2.locationImpl instead", "2020-01-06")
  def locationImpl(c: Context): c.Tree = MacroCompatScala2.locationImpl(c)

  trait ClueMacro {
    implicit def generate[T](value: T): Clue[T] = macro MacroCompatScala2.clueImpl
  }

  @deprecated("Use MacroCompatScala2.clueImpl instead", "2020-01-06")
  def clueImpl(c: Context)(value: c.Tree): c.Tree = MacroCompatScala2.clueImpl(c)(value)

  trait CompileErrorMacro {
    def compileErrors(code: String): String = macro MacroCompatScala2.compileErrorsImpl
  }

  @deprecated("Use MacroCompatScala2.compileErrorsImpl instead", "2020-01-06")
  def compileErrorsImpl(c: Context)(value: c.Tree): c.Tree = MacroCompatScala2.compileErrorsImpl(c)(value)

}
