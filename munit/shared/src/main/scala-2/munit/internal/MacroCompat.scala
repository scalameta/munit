package munit.internal

import munit.{Clue, Location}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object MacroCompat {

  trait LocationMacro {
    implicit def generate: Location = macro MacroCompatScala2.locationImpl
  }

  @deprecated("Use MacroCompatScala2.locationImpl instead", "2020-01-06")
  def locationImpl(c: blackbox.Context): c.Tree =
    MacroCompatScala2.locationImpl(c)

  trait ClueMacro {
    implicit def generate[T](value: T): Clue[T] =
      macro MacroCompatScala2.clueImpl
  }

  @deprecated("Use MacroCompatScala2.clueImpl instead", "2020-01-06")
  def clueImpl(c: blackbox.Context)(value: c.Tree): c.Tree =
    MacroCompatScala2.clueImpl(c)(value)

  trait CompileErrorMacro {
    def compileErrors(code: String): String =
      macro MacroCompatScala2.compileErrorsImpl

    def assertCompile(code: String)(implicit
        loc: Location
    ): Unit =
      macro MacroCompatScala2.assertCompileWithDefaultClueImpl

    def assertCompile(code: String, clue: => Any)(implicit
        loc: Location
    ): Unit =
      macro MacroCompatScala2.assertCompileImpl
  }

  @deprecated("Use MacroCompatScala2.compileErrorsImpl instead", "2020-01-06")
  def compileErrorsImpl(c: blackbox.Context)(value: c.Tree): c.Tree =
    MacroCompatScala2.compileErrorsImpl(c)(c.Expr[String](value)).tree

}
