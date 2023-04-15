package munit

import munit.internal.junitinterface.CustomFingerprint
import munit.internal.junitinterface.CustomRunners
import munit.internal.junitinterface.JUnitFramework

class Framework extends JUnitFramework {
  override def name(): String = "munit"
  val munitFingerprint = new CustomFingerprint("munit.Suite", _isModule = false)
  val customRunners = new CustomRunners(
    List(
      munitFingerprint,
      new CustomFingerprint("munit.Suite", _isModule = true)
    )
  )
}
