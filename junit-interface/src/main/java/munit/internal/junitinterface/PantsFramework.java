package munit.internal.junitinterface;

import sbt.testing.Fingerprint;

public class PantsFramework extends JUnitFramework {

  private static final CustomFingerprint scalatestFingerprint =
      CustomFingerprint.of("org.scalatest.Suite", "org.scalatest.junit.JUnitRunner");

  // In ScalaTest 3.1+, `JUnitRunner` moved to a different package.
  private static final CustomFingerprint scalatestPlusJunitFingerprint =
      CustomFingerprint.of("org.scalatest.Suite", "org.scalatestplus.junit.JUnitRunner");

  private static final Fingerprint[] FINGERPRINTS =
      new Fingerprint[] {
        new RunWithFingerprint(),
        new JUnitFingerprint(),
        new JUnit3Fingerprint(),
        scalatestFingerprint,
        scalatestPlusJunitFingerprint
      };

  @Override
  public Fingerprint[] fingerprints() {
    return FINGERPRINTS;
  }

  @Override
  public CustomRunners customRunners() {
    return CustomRunners.of(scalatestFingerprint, scalatestPlusJunitFingerprint);
  }

  @Override
  public sbt.testing.Runner runner(
      String[] args, String[] remoteArgs, ClassLoader testClassLoader) {
    String[] newArgs = new String[args.length + 1];
    // NOTE(olafur): by default, stderr is not printed when running tests. Users can still enable
    // stderr by passing in the "--stderr" flag.
    newArgs[0] = "--no-stderr";
    System.arraycopy(args, 0, newArgs, 1, args.length);
    return super.runner(newArgs, remoteArgs, testClassLoader);
  }
}
