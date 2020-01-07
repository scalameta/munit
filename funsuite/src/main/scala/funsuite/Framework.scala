package funsuite

import sbt.testing.Fingerprint

import sbt.testing.Runner
import sbt.testing.SubclassFingerprint
import java.{util => ju}
import scala.util.control.NonFatal
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
