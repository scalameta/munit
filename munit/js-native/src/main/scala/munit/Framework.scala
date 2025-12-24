package munit

import munit.internal.junitinterface.{
  CustomFingerprint, CustomRunners, JUnitFramework,
}

class Framework extends JUnitFramework {
  override def name(): String = "munit"
  val munitFingerprint = new CustomFingerprint("munit.Suite", _isModule = false)
  val customRunners = new CustomRunners(
    List(munitFingerprint, new CustomFingerprint("munit.Suite", _isModule = true))
  )
}
