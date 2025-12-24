package munit.internal

import munit.{Clue, Location}

import scala.language.experimental.macros

object MacroCompat {

  trait LocationMacro {
    implicit def generate: Location = macro MacroCompatScala2.locationImpl
  }

  trait ClueMacro {
    implicit def generate[T](value: T): Clue[T] =
      macro MacroCompatScala2.clueImpl
  }

  trait CompileErrorMacro {
    def compileErrors(code: String): String =
      macro MacroCompatScala2.compileErrorsImpl
  }

}
