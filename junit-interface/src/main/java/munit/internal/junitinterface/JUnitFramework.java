package munit.internal.junitinterface;

import sbt.testing.Fingerprint;
import sbt.testing.Framework;

public class JUnitFramework implements Framework {
  private static Fingerprint[] FINGERPRINTS =
      new Fingerprint[] {new RunWithFingerprint(), new JUnitFingerprint(), new JUnit3Fingerprint()};

  @Override
  public String name() {
    return "JUnit";
  }

  @Override
  public sbt.testing.Fingerprint[] fingerprints() {
    CustomRunners customRunners = customRunners();
    if (customRunners.isEmpty()) return FINGERPRINTS;
    Fingerprint[] result = new Fingerprint[FINGERPRINTS.length + customRunners.runners.size()];
    System.arraycopy(FINGERPRINTS, 0, result, 0, FINGERPRINTS.length);
    CustomFingerprint[] customFingerprints =
        customRunners.runners.toArray(new CustomFingerprint[0]);
    System.arraycopy(customFingerprints, 0, result, FINGERPRINTS.length, customFingerprints.length);
    return result;
  }

  public CustomRunners customRunners() {
    return CustomRunners.of();
  }

  @Override
  public sbt.testing.Runner runner(
      String[] args, String[] remoteArgs, ClassLoader testClassLoader) {
    return new JUnitRunner(args, remoteArgs, testClassLoader, customRunners());
  }
}
