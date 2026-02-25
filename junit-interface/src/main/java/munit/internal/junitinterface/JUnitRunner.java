package munit.internal.junitinterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    RunSettings.LogMode logMode = null;
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
      if (s.isEmpty()) continue;
      if (s.charAt(0) != '-' && s.charAt(0) != '+') {
        globPatterns.add(s);
        continue;
      }

      int sep = s.indexOf('=');
      if (sep < 0) {
        switch (s) {
          case "-v":
          case "--verbose":
            verbose = true;
            break;
          case "-n":
            nocolor = true;
            break;
          case "-s":
            decodeScalaNames = true;
            break;
          case "-a":
            logAssert = true;
            break;
          case "-c":
            logExceptionClass = false;
            break;
          case "+l":
            useSbtLoggers = true;
            break;
          case "+b":
            useBufferedLoggers = true;
            break;
          case "-b":
            useBufferedLoggers = false;
            break;
          case "-l":
            useSbtLoggers = false;
            break;
          case "-F":
            trimStackTraces = false;
            break;
          case "+F":
            trimStackTraces = true;
            break;
          default:
        }
        continue;
      }

      String value = s.substring(sep + 1);
      if (s.startsWith("-D")) {
        sysprops.put(s.substring(2, sep), value);
        continue;
      }

      switch (s.substring(0, sep)) {
        case "--log":
          logMode = RunSettings.LogMode.parse(value);
          break;
        case "--summary":
          summary = RunSettings.Summary.values()[Integer.parseInt(value)];
          break;
        case "--logger":
          switch (value) {
            case "sbt":
              useSbtLoggers = true;
              break;
            case "buffered":
              useBufferedLoggers = true;
              break;
            default:
          }
          break;
        case "--tests":
          testFilter = value;
          break;
        case "--ignore-runners":
          ignoreRunners = value;
          break;
        case "--run-listener":
          runListener = value;
          break;
        case "--include-categories":
          includeCategories.addAll(Arrays.asList(value.split(",")));
          break;
        case "--exclude-categories":
          excludeCategories.addAll(Arrays.asList(value.split(",")));
          break;
        case "--include-tags":
          includeTags.addAll(Arrays.asList(value.split(",")));
          break;
        case "--exclude-tags":
          excludeTags.addAll(Arrays.asList(value.split(",")));
          break;
        default:
      }
    }

    for (String s : args) {
      switch (s) {
        case "+n":
          nocolor = false;
          break;
        case "+s":
          decodeScalaNames = false;
          break;
        case "+a":
          logAssert = false;
          break;
        case "+c":
          logExceptionClass = true;
          break;
        case "+l":
          useSbtLoggers = true;
          break;
        default:
      }
    }
    if (logMode == null)
      logMode = verbose ? RunSettings.LogMode.TRACE : RunSettings.LogMode.INFO;
    this.settings =
        new RunSettings(
            !nocolor,
            decodeScalaNames,
            logMode,
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
