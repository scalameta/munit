package munit.internal.junitinterface;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

class JUnitRunnerWrapper extends Runner implements Filterable {
    private final Runner delegate;

    JUnitRunnerWrapper(Runner delegate) {
      this.delegate = delegate;
    }

    @Override
    public Description getDescription() {
      return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
      delegate.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
      filter.apply(delegate);
    }
  }
