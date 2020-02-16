package munit.internal.junitinterface;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

/**
 * A filtered request which ignores NoTestsRemainExceptions.
 */
final class SilentFilterRequest extends Request {
  private final Request request;
  private final Filter filter;

  public SilentFilterRequest(Request request, Filter filter) {
    this.request = request;
    this.filter = filter;
  }

  @Override 
  public Runner getRunner() {
    Runner runner = request.getRunner();
    try {
      filter.apply(runner);
      return runner;
    } catch (NoTestsRemainException e) {
      return new EmptyRunner(runner.getDescription());
    }
  }
}
