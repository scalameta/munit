// Mostly copied from
// http://stackoverflow.com/questions/1230706/running-a-subset-of-junit-test-methods/1236782#1236782
package munit.internal.junitinterface;

import java.util.HashSet;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public final class TestFilter extends Filter {
  private static final String DELIMITER = "\\,";

  private final HashSet<String> ignored = new HashSet<String>();
  private final String[] testPatterns;
  private final EventDispatcher ed;

  public TestFilter(String testFilter, EventDispatcher ed) {
    this.ed = ed;
    this.testPatterns = testFilter.split(DELIMITER);
  }

  @Override
  public String describe() {
    return "Filters out all tests not explicitly named in the '-tests=' option.";
  }

  @Override
  public boolean shouldRun(Description d) {
    String displayName = d.getDisplayName();

    // We get asked both if we should run the class/suite, as well as the individual tests
    // So let the suite always run, so we can evaluate the individual test cases
    if (displayName.indexOf('(') == -1) return true;
    String testName = displayName.substring(0, displayName.indexOf('('));

    // JUnit calls this multiple times per test and we don't want to print a new "test ignored"
    // message each time
    if (ignored.contains(testName)) return false;

    for (String p : testPatterns) if (Pattern.matches(p, testName)) return true;

    ignored.add(testName);
    ed.testIgnored(d);
    return false;
  }
}
