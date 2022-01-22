package munit.internal.junitinterface

import sbt.testing.SubclassFingerprint

class CustomFingerprint(
    val suite: String,
    _isModule: Boolean
) extends SubclassFingerprint {
  override def isModule(): Boolean = _isModule
  override def superclassName(): String = suite
  override def requireNoArgConstructor(): Boolean = true
}
