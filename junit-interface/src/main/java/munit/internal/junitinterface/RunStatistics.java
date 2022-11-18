package munit.internal.junitinterface;

import java.util.ArrayList;
import java.util.List;
import sbt.testing.Status;

class RunStatistics {
  private final RunSettings settings;

  private int failedCount, ignoredCount, otherCount;
  private final ArrayList<String> failedNames = new ArrayList<>();
  private final ArrayList<String> otherNames = new ArrayList<>();
  private volatile long accumulatedTime;

  RunStatistics(RunSettings settings) {
    this.settings = settings;
  }

  void addTime(long t) {
    accumulatedTime += t;
  }

  synchronized void captureStats(AbstractEvent e) {
    Status s = e.status();
    if (s == Status.Error || s == Status.Failure) {
      failedCount++;
      failedNames.add(e.fullyQualifiedName());
    } else {
      if (s == Status.Ignored) ignoredCount++;
      else otherCount++;
      otherNames.add(e.fullyQualifiedName());
    }
  }

  private String summaryLine() {
    return (failedCount == 0 ? "All tests passed: " : "Some tests failed: ")
        + failedCount
        + " failed, "
        + ignoredCount
        + " ignored, "
        + (failedCount + ignoredCount + otherCount)
        + " total, "
        + (accumulatedTime / 1000.0)
        + "s";
  }

  private static String mkString(List<String> l) {
    StringBuilder b = new StringBuilder();
    for (String s : l) {
      if (b.length() != 0) b.append(", ");
      b.append(s);
    }
    return b.toString();
  }

  synchronized String createSummary() {
    switch (settings.summary) {
      case LIST_FAILED:
        return failedNames.isEmpty()
            ? summaryLine()
            : (summaryLine() + "\n- Failed tests: " + mkString(failedNames));
      case ONE_LINE:
        return summaryLine();
      default:
        return "";
    }
  }
}
