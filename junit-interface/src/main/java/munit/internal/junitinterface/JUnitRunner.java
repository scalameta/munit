package munit.internal.junitinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.runner.notification.RunListener;
import sbt.testing.Runner;
import sbt.testing.Selector;
import sbt.testing.Task;
import sbt.testing.TaskDef;
import sbt.testing.TestSelector;

final class JUnitRunner implements Runner {
  private final String[] args;
  private final String[] remoteArgs;
  private final RunSettings settings;

  private volatile boolean used = false;

  final ClassLoader testClassLoader;
  final RunListener runListener;
  final RunStatistics runStatistics;
  final CustomRunners customRunners;

  JUnitRunner(
      String[] args,
      String[] remoteArgs,
      ClassLoader testClassLoader,
      CustomRunners customRunners) {

    this.args = args;
    this.remoteArgs = remoteArgs;
    this.testClassLoader = testClassLoader;
    this.customRunners = customRunners;
    Settings defaults = Settings.defaults();

    boolean nocolor = false,
        decodeScalaNames = false,
        logAssert = true,
        logExceptionClass = true,
        useSbtLoggers = false,
        useBufferedLoggers = true;
    boolean verbose = false;
    boolean trimStackTraces = defaults.trimStackTraces();
    RunSettings.Summary summary = RunSettings.Summary.SBT;
    HashMap<String, String> sysprops = new HashMap<String, String>();
    ArrayList<String> globPatterns = new ArrayList<String>();
    Set<String> includeCategories = new HashSet<String>();
    Set<String> excludeCategories = new HashSet<String>();
    Set<String> includeTags = new HashSet<String>();
    Set<String> excludeTags = new HashSet<String>();

    String testFilter = "";
    String ignoreRunners = "org.junit.runners.Suite";
    String runListener = null;
    for (String s : args) {
      if ("-v".equals(s) || "--verbose".equals(s)) verbose = true;
      else if (s.startsWith("--summary="))
        summary = RunSettings.Summary.values()[Integer.parseInt(s.substring(10))];
      else if ("-n".equals(s)) nocolor = true;
      else if ("-s".equals(s)) decodeScalaNames = true;
      else if ("-a".equals(s)) logAssert = true;
      else if ("-c".equals(s)) logExceptionClass = false;
      else if ("+l".equals(s)) useSbtLoggers = true;
      else if ("+b".equals(s)) useBufferedLoggers = true;
      else if ("-b".equals(s)) useBufferedLoggers = false;
      else if ("--logger=sbt".equals(s)) useSbtLoggers = true;
      else if ("--logger=buffered".equals(s)) useBufferedLoggers = true;
      else if ("-l".equals(s)) useSbtLoggers = false;
      else if ("-F".equals(s)) trimStackTraces = false;
      else if ("+F".equals(s)) trimStackTraces = true;
      else if (s.startsWith("--tests=")) testFilter = s.substring(8);
      else if (s.startsWith("--ignore-runners=")) ignoreRunners = s.substring(17);
      else if (s.startsWith("--run-listener=")) runListener = s.substring(15);
      else if (s.startsWith("--include-categories="))
        includeCategories.addAll(Arrays.asList(s.substring(21).split(",")));
      else if (s.startsWith("--exclude-categories="))
        excludeCategories.addAll(Arrays.asList(s.substring(21).split(",")));
      else if (s.startsWith("--include-tags="))
        includeTags.addAll(Arrays.asList(s.substring("--include-tags=".length()).split(",")));
      else if (s.startsWith("--exclude-tags="))
        excludeTags.addAll(Arrays.asList(s.substring("--exclude-tags=".length()).split(",")));
      else if (s.startsWith("-D") && s.contains("=")) {
        int sep = s.indexOf('=');
        sysprops.put(s.substring(2, sep), s.substring(sep + 1));
      } else if (!s.startsWith("-") && !s.startsWith("+")) globPatterns.add(s);
    }
    for (String s : args) {
      if ("+n".equals(s)) nocolor = false;
      else if ("+s".equals(s)) decodeScalaNames = false;
      else if ("+a".equals(s)) logAssert = false;
      else if ("+c".equals(s)) logExceptionClass = true;
      else if ("+l".equals(s)) useSbtLoggers = true;
    }
    this.settings =
        new RunSettings(
            !nocolor,
            decodeScalaNames,
            verbose,
            useSbtLoggers,
            useBufferedLoggers,
            trimStackTraces,
            summary,
            logAssert,
            ignoreRunners,
            logExceptionClass,
            sysprops,
            globPatterns,
            includeCategories,
            excludeCategories,
            includeTags,
            excludeTags,
            testFilter);
    this.runListener = createRunListener(runListener);
    this.runStatistics = new RunStatistics(settings);
  }

  @Override
  public Task[] tasks(TaskDef[] taskDefs) {
    used = true;
    JUnitComputer computer = new JUnitComputer(testClassLoader, customRunners, settings);
    int length = taskDefs.length;
    List<Task> tasks = new ArrayList<>(taskDefs.length);
    for (int i = 0; i < length; i++) {
      TaskDef taskDef = taskDefs[i];
      if (shouldRun(computer, taskDef)) {
        RunSettings alteredSettings = alterRunSettings(this.settings, taskDef.selectors());
        tasks.add(new JUnitTask(this, alteredSettings, taskDef, computer));
      }
    }
    return tasks.toArray(new Task[0]);
  }

  private boolean shouldRun(JUnitComputer computer, TaskDef taskDef) {
    try {
      Class<?> cls = testClassLoader.loadClass(taskDef.fullyQualifiedName());
      return !computer.customRunner(cls).isPresent()
          || customRunners.matchesFingerprint(taskDef.fingerprint());
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private RunListener createRunListener(String runListenerClassName) {
    if (runListenerClassName != null) {
      try {
        return (RunListener) testClassLoader.loadClass(runListenerClassName).newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else return null;
  }

  /**
   * Alter default RunSettings depending on the passed selectors. If selectors contains only
   * elements of type TestSelector, then default settings are altered to include only test names
   * from these selectors. This allows to run particular test cases within given test class.
   * testFilter is treated as a regular expression, hence joining is done via '|'.
   */
  private RunSettings alterRunSettings(RunSettings defaultSettings, Selector[] selectors) {
    List<TestSelector> testSelectors =
        Arrays.stream(selectors)
            .flatMap(
                selector -> {
                  return selector instanceof TestSelector
                      ? Stream.of((TestSelector) selector)
                      : Stream.empty();
                })
            .collect(Collectors.toList());
    if (testSelectors.size() == selectors.length) {
      String testFilter =
          testSelectors.stream().map(TestSelector::testName).collect(Collectors.joining("|"));
      // if already provided testFilter is not empty add to it | (regex or operator)
      String currentFilter =
          defaultSettings.testFilter.length() > 0 ? defaultSettings.testFilter + "|" : "";
      String newFilter = currentFilter + testFilter;
      return defaultSettings.withTestFilter(newFilter);
    }

    return defaultSettings;
  }

  @Override
  public String done() {
    // Can't simply return the summary due to https://github.com/sbt/sbt/issues/3510
    if (!used) return "";
    String stats = runStatistics.createSummary();
    if (stats.isEmpty()) return stats;
    System.out.println(stats);
    return " ";
  }

  @Override
  public String[] remoteArgs() {
    return remoteArgs;
  }

  @Override
  public String[] args() {
    return args;
  }
}
