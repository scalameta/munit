package munit.internal

import fansi.Category
import fansi.Bold
import fansi.Attr
import fansi.EscapeAttr
import fansi.ResetAttr

object Colors {

  /**
    * [[Attr]]s to turn text bold/bright or disable it
    */
  object Bold extends Category(offset = 0, width = 2)("Bold") {
    val Faint = makeAttr("\u001b[2m", 2)("Faint")
    val On = makeAttr(Console.BOLD, 1)("On")
    val Off = makeNoneAttr(0)("Off")
    val all: Vector[Attr] = Vector(On, Off, Faint)
  }

  // NOTE(olafur): this code is copy-pasted from utest in order to recreate the
  // `Bold.Faint` attribute which is missing in the original fansi library.
  sealed abstract class Category(val offset: Int, val width: Int)(
      implicit catName: sourcecode.Name
  ) {
    def mask = ((1 << width) - 1) << offset
    val all: Vector[Attr]

    def lookupEscape(applyState: Long) = {
      val escapeOpt = lookupAttr(applyState).escapeOpt
      if (escapeOpt.isDefined) escapeOpt.get
      else ""
    }
    def lookupAttr(applyState: Long) =
      lookupAttrTable((applyState >> offset).toInt)

    // Allows fast lookup of categories based on the desired applyState
    protected[this] def lookupTableWidth = 1 << width

    protected[this] lazy val lookupAttrTable = {
      val arr = new Array[Attr](lookupTableWidth)
      for (attr <- all) {
        arr((attr.applyMask >> offset).toInt) = attr
      }
      arr
    }

    def makeAttr(s: String, applyValue: Long)(
        implicit name: sourcecode.Name
    ) = {
      EscapeAttr(s, mask, applyValue << offset)(
        catName.value + "." + name.value
      )
    }

    def makeNoneAttr(applyValue: Long)(implicit name: sourcecode.Name) = {
      ResetAttr(mask, applyValue << offset)(
        catName.value + "." + name.value
      )
    }
  }
}
