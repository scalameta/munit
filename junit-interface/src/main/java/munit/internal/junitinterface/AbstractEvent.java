package munit.internal.junitinterface;

import static munit.internal.junitinterface.Ansi.*;

import sbt.testing.*;

abstract class AbstractEvent implements Event {
  protected final String ansiName;
  protected final String ansiMsg;
  protected final Status status;
  protected final Throwable error;
  private final Fingerprint fingerprint;
  private final Long duration;

  AbstractEvent(
      String ansiName,
      String ansiMsg,
      Status status,
      Fingerprint fingerprint,
      Long duration,
      Throwable error) {
    this.fingerprint = fingerprint;
    this.ansiName = ansiName;
    this.ansiMsg = ansiMsg;
    this.status = status;
    this.duration = duration;
    this.error = error;
  }

  abstract void logTo(RichLogger logger);

  @Override
  public String fullyQualifiedName() {
    return filterAnsi(ansiName);
  }

  @Override
  public Fingerprint fingerprint() {
    return fingerprint;
  }

  @Override
  public Selector selector() {
    return new TestSelector(fullyQualifiedName());
  }

  @Override
  public Status status() {
    return status;
  }

  @Override
  public OptionalThrowable throwable() {
    if (error == null) {
      return new OptionalThrowable();
    } else {
      return new OptionalThrowable(error);
    }
  }

  @Override
  public long duration() {
    return duration;
  }

  String durationToString() {
    return c(duration / 1000.0 + "s", FAINT);
  }
}
