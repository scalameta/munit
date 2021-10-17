package munit.internal.junitinterface;

import sbt.testing.TaskDef;

public class TaskDefFilter {

  public TaskDefFilter(ClassLoader testClassLoader) {}

  public boolean shouldRun(TaskDef taskDef) {
    return true;
  }
}
