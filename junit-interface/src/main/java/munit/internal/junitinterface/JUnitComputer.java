package munit.internal.junitinterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class JUnitComputer extends Computer {
  final Map<Class<?>, Class<?>> suiteRunners;
  final Settings settings;

  public JUnitComputer(
      ClassLoader testClassLoader, CustomRunners customRunners, Settings settings) {
    this.settings = settings;
    suiteRunners = new HashMap<>();
    customRunners
        .all()
        .forEach(
            (suite, runner) -> {
              try {
                suiteRunners.put(
                    testClassLoader.loadClass(suite), testClassLoader.loadClass(runner));
              } catch (ClassNotFoundException e) {
                // ignore, since we'll fail to find at least one of the 2 `JUnitRunners`
                // (it moved from `org.scalatest.junit` to `org.scalatestplus.junit` in 3.1)
              }
            });
  }

  public Optional<Class<?>> customRunner(Class<?> clazz) {
    for (Map.Entry<Class<?>, Class<?>> entry : suiteRunners.entrySet()) {
      if (entry.getKey().isAssignableFrom(clazz)) {
        return Optional.of(entry.getValue());
      }
    }
    return Optional.empty();
  }

  private class MySuite extends Suite implements Filterable {
    public MySuite(RunnerBuilder runnerBuilder, Class<?>[] classes) throws InitializationError {
      super(runnerBuilder, classes);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
      for (Runner r : super.getChildren()) {
        filter.apply(r);
      }
    }
  }

  @Override
  public Runner getSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
    RunnerBuilder runnerBuilder =
        new RunnerBuilder() {
          @Override
          public Runner runnerForClass(Class<?> testClass) throws Throwable {
            return getRunner(builder, testClass);
          }
        };
    return new MySuite(runnerBuilder, classes);
  }

  @Override
  protected Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
    Optional<Class<?>> runnerClass = customRunner(testClass);
    if (runnerClass.isPresent()) {
      Runner runner = (Runner) runnerClass.get().getConstructor(Class.class).newInstance(testClass);
      if (runner instanceof Configurable) {
        Configurable configurable = (Configurable) runner;
        configurable.configure(this.settings);
      }
      return new JUnitRunnerWrapper(runner);
    } else {
      return super.getRunner(builder, testClass);
    }
  }
}
