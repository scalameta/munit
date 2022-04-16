package munit

import munit.internal.MacroCompat

class Clue[+T](
    val source: String,
    val value: T,
    val valueType: String
) extends Serializable {
  override def toString(): String = s"Clue($source, $value)"
}
object Clue extends MacroCompat.ClueMacro {
  @deprecated("use fromValue instead", "1.0.0")
  def empty[T](value: T): Clue[T] = fromValue(value)
  def fromValue[T](value: T): Clue[T] = new Clue("", value, "")
}
