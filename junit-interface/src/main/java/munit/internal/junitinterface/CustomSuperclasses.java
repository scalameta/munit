package munit.internal.junitinterface;

import java.util.Set;
import sbt.testing.Fingerprint;
import sbt.testing.SubclassFingerprint;

public class CustomSuperclasses {
  final Set<String> superclasses;

  public CustomSuperclasses(Set<String> superclasses) {
    this.superclasses = superclasses;
  }

  public boolean matchesFingerprint(Fingerprint fingerprint) {
    if (fingerprint instanceof SubclassFingerprint) {
      SubclassFingerprint subclassFingerprint = (SubclassFingerprint) fingerprint;
      return superclasses.contains(subclassFingerprint.superclassName());
    }
    return false;
  }
}
