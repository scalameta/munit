package munit.internal.junitinterface;

import sbt.testing.SubclassFingerprint;

public class CustomFingerprint implements SubclassFingerprint {
  final String suite;
  final String runner;
  final boolean module;

  public CustomFingerprint(String suite, String runner, boolean module) {
    this.suite = suite;
    this.runner = runner;
    this.module = module;
  }

  public static CustomFingerprint of(String suite, String runner) {
    return new CustomFingerprint(suite, runner, false);
  }

  public static CustomFingerprint ofModule(String suite, String runner) {
    return new CustomFingerprint(suite, runner, true);
  }

  @Override
  public boolean isModule() {
    return module;
  }

  @Override
  public String superclassName() {
    return suite;
  }

  @Override
  public boolean requireNoArgConstructor() {
    return true;
  }

  @Override
  public String toString() {
    return "CustomFingerprint{" + "suite='" + suite + '\'' + '}';
  }
}
