package munit.internal.junitinterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sbt.testing.Fingerprint;
import sbt.testing.SubclassFingerprint;

public class CustomRunners {
  final List<CustomFingerprint> runners;
  final Set<String> superclasses;

  public CustomRunners(List<CustomFingerprint> runners) {
    this.runners = runners;
    this.superclasses = new HashSet<>();
    runners.forEach(runner -> this.superclasses.add(runner.suite));
  }

  public boolean isEmpty() {
    return runners.isEmpty();
  }

  public Map<String, String> all() {
    Map<String, String> result = new HashMap<>();
    runners.forEach(runner -> result.put(runner.suite, runner.runner));
    return result;
  }

  public boolean matchesFingerprint(Fingerprint fingerprint) {
    if (fingerprint instanceof SubclassFingerprint) {
      SubclassFingerprint subclassFingerprint = (SubclassFingerprint) fingerprint;
      return superclasses.contains(subclassFingerprint.superclassName());
    }
    return false;
  }

  public static CustomRunners of(CustomFingerprint... runners) {
    return new CustomRunners(Arrays.asList(runners));
  }
}
