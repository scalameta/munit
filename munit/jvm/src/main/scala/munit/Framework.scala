package munit

import sbt.testing.Fingerprint

import munit.internal.junitinterface.CustomFingerprint
import munit.internal.junitinterface.CustomRunners
import sbt.testing.SubclassFingerprint

class Framework extends munit.internal.junitinterface.JUnitFramework {
  val munitFingerprint: CustomFingerprint = CustomFingerprint.of(
    "munit.Suite",
    "munit.MUnitRunner"
  )
  override val name = "munit"
  override val fingerprints: Array[Fingerprint] = Array(
    munitFingerprint,
    new SubclassFingerprint {
      def isModule(): Boolean = true
      def superclassName(): String = "munit.Suite"
      def requireNoArgConstructor(): Boolean = true
    }
  )
  override val customRunners: CustomRunners =
    CustomRunners.of(munitFingerprint)
}
