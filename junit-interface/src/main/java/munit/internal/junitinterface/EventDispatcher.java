package munit.internal.junitinterface;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import sbt.testing.EventHandler;
import sbt.testing.Fingerprint;
import sbt.testing.Status;

import static munit.internal.junitinterface.Ansi.*;


final class EventDispatcher extends RunListener
{
  private final RichLogger logger;
  private final Set<Description> reported = Collections.newSetFromMap(new ConcurrentHashMap<Description, Boolean>());
  private final Set<String> reportedSuites = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  private final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<String, Long>();
  private final EventHandler handler;
  private final RunSettings settings;
  private final Fingerprint fingerprint;
  private final String taskInfo;
  private final RunStatistics runStatistics;

  private final static Description TEST_RUN = Description.createTestDescription("Test", "run");

  EventDispatcher(RichLogger logger, EventHandler handler, RunSettings settings, Fingerprint fingerprint,
                  Description taskDescription, RunStatistics runStatistics)
  {
    this.logger = logger;
    this.handler = handler;
    this.settings = settings;
    this.fingerprint = fingerprint;
    this.taskInfo = settings.buildPlainName(taskDescription);
    this.runStatistics = runStatistics;
  }

  private abstract class Event extends AbstractEvent {
    Event(String name, String message, Status status, Long duration, Throwable error) {
      super(name, message, status, fingerprint, duration, error);
    }
    String durationSuffix() { return " " + durationToString(); }
  }

  private abstract class ErrorEvent extends Event {
    ErrorEvent(Failure failure, Status status) {
      super(settings.buildErrorName(failure.getDescription(), status),
            settings.buildErrorMessage(failure.getException()),
            status,
            elapsedTime(failure.getDescription()),
            failure.getException());
    }
  }

  private abstract class InfoEvent extends Event {
    InfoEvent(Description desc, Status status) {
      super(settings.buildInfoName(desc, status), null, status, elapsedTime(desc), null);
    }
  }

  @Override
  public void testAssumptionFailure(final Failure failure)
  {
    postIfFirst(failure.getDescription(), new ErrorEvent(failure, Status.Skipped) {
      void logTo(RichLogger logger) {
        if (settings.verbose) {
          logger.info(failure.getDescription(), Ansi.c("==> i "  + failure.getDescription().getMethodName(), WARNMSG));
        }
      }
    });
  }

  @Override
  public void testFailure(final Failure failure)
  {
    if (failure.getDescription() != null && failure.getDescription().getClassName() != null) {
      try {
        trimStackTrace(
            failure.getException(),
            "java.lang.Thread",
            failure.getDescription().getClassName(),
            settings
        );
      } catch (Throwable t) {
        // Ignore error.
      }
    }
    postIfFirst(failure.getDescription(), new ErrorEvent(failure, Status.Failure) {
      void logTo(RichLogger logger) {
        logger.error( failure.getDescription(), settings.buildTestResult(Status.Failure) +ansiName+" "+ durationSuffix() + " " + ansiMsg, error);
      }
    });
  }


  @Override
  public void testFinished(Description desc)
  {
    postIfFirst(desc, new InfoEvent(desc, Status.Success) {
      void logTo(RichLogger logger) {
        logger.info(desc, settings.buildTestResult(Status.Success) + Ansi.c(desc.getMethodName(), SUCCESS1) + durationSuffix());
      }
    });
    logger.popCurrentTestClassName();
  }

  @Override
  public void testIgnored(Description desc)
  {
    postIfFirst(desc, new InfoEvent(desc, Status.Skipped) {
      void logTo(RichLogger logger) {
        logger.warn(desc, settings.buildTestResult(Status.Ignored) + ansiName+" ignored" + durationSuffix());
      }
    });
  }


  @Override
  public void testSuiteStarted(Description desc)
  {
    if (desc == null || desc.getClassName() == null || desc.getClassName().equals("null")) return;
    reportedSuites.add(desc.getClassName());
    logger.info(desc, c(desc.getClassName() + ":", SUCCESS1));
  }


  @Override
  public void testStarted(Description desc)
  {
    recordStartTime(desc);
    if (reportedSuites.add(desc.getClassName())) {
      testSuiteStarted(desc);
    }
    logger.pushCurrentTestClassName(desc.getClassName());
    if (settings.verbose) {
      logger.info(desc, settings.buildPlainName(desc) + " started");
    }
  }

  private void recordStartTime(Description description) {
    startTimes.putIfAbsent(settings.buildPlainName(description), System.currentTimeMillis());
  }

  private Long elapsedTime(Description description) {
    Long startTime = startTimes.get(settings.buildPlainName(description));
    if( startTime == null ) {
      return 0l;
    } else {
      return System.currentTimeMillis() - startTime;
    }
  }

  @Override
  public void testRunFinished(Result result)
  {
      if (settings.verbose) {
        logger.info(TEST_RUN, "Test run " +taskInfo+" finished: "+
          result.getFailureCount()+" failed" +
          ", " +
          result.getIgnoreCount()+" ignored" +
          ", "+result.getRunCount()+" total, "+(result.getRunTime()/1000.0)+"s") ;
      }
    runStatistics.addTime(result.getRunTime());
      logger.flush(TEST_RUN);
  }

  @Override
  public void testSuiteFinished(Description description) throws Exception {
    logger.flush(description);
  }

  @Override
  public void testRunStarted(Description desc)
  {
      if (settings.verbose) {
        logger.info(desc, taskInfo + " started");
      }
  }

  void testExecutionFailed(String testName, Throwable err)
  {
    post(new Event(Ansi.c(testName, Ansi.ERRMSG), settings.buildErrorMessage(err), Status.Error, 0L, err) {
      void logTo(RichLogger logger) {
        logger.error(TEST_RUN, ansiName+" failed: "+ansiMsg, error);
      }
    });
  }


  private void postIfFirst(Description desc, AbstractEvent e)
  {
    if(reported.add(desc)) {
      e.logTo(logger);
      runStatistics.captureStats(e);
      handler.handle(e);
    }
  }

  void post(AbstractEvent e)
  {
    e.logTo(logger);
    runStatistics.captureStats(e);
    handler.handle(e);
  }


  // Removes stack trace elements that reference the reflective invocation in TestLauncher.
  private static void trimStackTrace(Throwable ex, String fromClassName, String toClassName, Settings settings) {
    if (!settings.trimStackTraces()) return;
    Throwable cause = ex;
    while (cause != null) {
      StackTraceElement[] stackTrace = cause.getStackTrace();
      if (stackTrace != null && stackTrace.length > 0) {
        int end = stackTrace.length - 1;
        StackTraceElement last = stackTrace[end];
        if (last.getClassName() != null && last.getClassName().equals(fromClassName)) {
          for (int i = 0; end >= 0; end--) {
            StackTraceElement e = stackTrace[end];
            if (e.getClassName().equals(toClassName)) {
              break;
            }
          }
          StackTraceElement[] newStackTrace = Arrays.copyOfRange(stackTrace, 0, end + 1);
          cause.setStackTrace(newStackTrace);
        }
      }
      cause = cause.getCause();
    }
  }
}
