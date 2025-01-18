package munit

import munit.internal.MacroCompat

class Clue[+T](val source: String, val value: T, val valueType: String)
    extends Serializable {
  override def toString(): String = s"Clue($source, $value)"
}
object Clue extends MacroCompat.ClueMacro {
  def fromValue[T](value: T): Clue[T] = new Clue("", value, "")
}
