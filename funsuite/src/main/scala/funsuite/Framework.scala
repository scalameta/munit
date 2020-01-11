package funsuite

import sbt.testing.Fingerprint

import com.geirsson.junit.CustomFingerprint
import com.geirsson.junit.CustomRunners

class Framework extends com.geirsson.junit.JUnitFramework {
  val funsuiteFingerprint = CustomFingerprint.of(
    "funsuite.Suite",
    "funsuite.FunSuiteRunner"
  )
  override val fingerprints: Array[Fingerprint] = Array(
    funsuiteFingerprint
  )
  override val customRunners: CustomRunners =
    CustomRunners.of(funsuiteFingerprint)
}
