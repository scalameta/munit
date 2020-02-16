package munit.internal.junitinterface;

import sbt.testing.AnnotatedFingerprint;

public abstract class AbstractAnnotatedFingerprint implements AnnotatedFingerprint {
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AnnotatedFingerprint)) return false;
    AnnotatedFingerprint f = (AnnotatedFingerprint) obj;
    return annotationName().equals(f.annotationName()) && isModule() == f.isModule();
  }
}
