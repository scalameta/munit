package munit

import munit.internal.console.Printers

class Clues(val values: List[Clue[_]]) {
  override def toString(): String = Printers.print(this)
}
