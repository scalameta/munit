package munit.internal.junitinterface;

public interface Settings {
  public boolean trimStackTraces();

  public static Settings defaults() {
    return new Settings() {
      @Override
      public boolean trimStackTraces() {
        return true;
      }
    };
  }
}
