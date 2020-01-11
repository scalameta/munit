package munit

import sbt.testing.Fingerprint

import com.geirsson.junit.CustomFingerprint
import com.geirsson.junit.CustomRunners

class Framework extends com.geirsson.junit.JUnitFramework {
  val munitFingerprint = CustomFingerprint.of(
    "munit.Suite",
    "munit.FunSuiteRunner"
  )
  override val fingerprints: Array[Fingerprint] = Array(
    munitFingerprint
  )
  override val customRunners: CustomRunners =
    CustomRunners.of(munitFingerprint)
}
