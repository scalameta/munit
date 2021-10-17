package munit.internal.junitinterface;

import sbt.testing.SubclassFingerprint;

public class JUnit3Fingerprint implements SubclassFingerprint {
  @Override
  public String superclassName() {
    return "junit.framework.TestCase";
  }

  @Override
  public boolean isModule() {
    return false;
  }

  @Override
  public boolean requireNoArgConstructor() {
    return false;
  }
}
