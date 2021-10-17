package munit.internal.junitinterface;

public class RunWithFingerprint extends AbstractAnnotatedFingerprint {
  @Override
  public String annotationName() {
    return "org.junit.runner.RunWith";
  }

  @Override
  public boolean isModule() {
    return false;
  }
}
