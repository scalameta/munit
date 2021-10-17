package munit.internal.junitinterface;

public class JUnitFingerprint extends AbstractAnnotatedFingerprint {
  @Override
  public String annotationName() {
    return "org.junit.Test";
  }

  @Override
  public boolean isModule() {
    return false;
  }
}
