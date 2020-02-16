package munit.internal.junitinterface;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

class EmptyRunner extends Runner {

  private final Description desc;

  EmptyRunner(Description desc) { this.desc = desc; } 

  @Override
  public Description getDescription() { return desc; }

  @Override
  public void run(RunNotifier notifier) {}
}
