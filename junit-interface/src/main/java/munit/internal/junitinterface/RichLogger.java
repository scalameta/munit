package munit.internal.junitinterface;

import static munit.internal.junitinterface.Ansi.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import sbt.testing.Logger;

final class RichLogger {
  private final Logger[] loggers;
  private final RunSettings settings;
  private final JUnitRunner runner;
  private final String testClassName;
  private final Map<String, Boolean> highlightedCache = new HashMap<>();
  private final ConcurrentLinkedDeque<String> buffer = new ConcurrentLinkedDeque<>();

  RichLogger(Logger[] loggers, RunSettings settings, String testClassName, JUnitRunner runner) {
    this.loggers = loggers;
    this.settings = settings;
    this.runner = runner;
    this.testClassName = testClassName;
  }

  void error(String s) {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.error(s);
        else l.error(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(s);
    } else {
      System.out.println(s);
    }
  }

  void error(String s, Throwable t) {
    error(s);
    if (t != null && (settings.logAssert || !(t instanceof AssertionError))) logStackTrace(t);
  }

  void info(String s) {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.info(s);
        else l.info(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(s);
    } else {
      System.out.println(s);
    }
  }

  void warn(String s) {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.warn(s);
        else l.warn(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(s);
    } else {
      System.out.println(s);
    }
  }

  void flush() {
    if (!buffer.isEmpty()) {
      System.out.println(String.join("\n", buffer));
      buffer.clear();
    }
  }

  private void bufferMessage(String message) {
    buffer.addLast(message);
  }

  private void logStackTrace(Throwable t) {
    StackTraceElement[] trace = t.getStackTrace();
    String testFileName = settings.color ? findTestFileName(trace, testClassName) : null;
    logStackTracePart(trace, trace.length - 1, 0, t, testFileName);
  }

  private void logStackTracePart(
      StackTraceElement[] trace, int m, int framesInCommon, Throwable t, String testFileName) {
    final int m0 = m;
    int top = 0;
    for (int i = top; i <= m; i++) {
      if (trace[i].toString().startsWith("org.junit.")
          || trace[i].toString().startsWith("org.hamcrest.")
          || trace[i].toString().startsWith("org.scalatest.")) {
        if (i == top) top++;
        else {
          m = i - 1;
          while (m > top) {
            String s = trace[m].toString();
            if (!s.startsWith("java.lang.reflect.") && !s.startsWith("sun.reflect.")) break;
            m--;
          }
          break;
        }
      }
    }
    for (int i = top; i <= m; i++) {
      if (!trace[i].getClassName().startsWith("scala.runtime."))
        error(stackTraceElementToString(trace[i], testFileName));
    }
    if (m0 != m) {
      // skip junit-related frames
      error("    ...");
    } else if (framesInCommon != 0) {
      // skip frames that were in the previous trace too
      error("    ... " + framesInCommon + " more");
    }
    logStackTraceAsCause(trace, t.getCause(), testFileName);
  }

  private void logStackTraceAsCause(
      StackTraceElement[] causedTrace, Throwable t, String testFileName) {
    if (t == null) return;
    StackTraceElement[] trace = t.getStackTrace();
    int m = trace.length - 1, n = causedTrace.length - 1;
    while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n])) {
      m--;
      n--;
    }
    error("Caused by: " + t);
    logStackTracePart(trace, m, trace.length - 1 - m, t, testFileName);
  }

  private String findTestFileName(StackTraceElement[] trace, String testClassName) {
    for (StackTraceElement e : trace) {
      String cln = e.getClassName();
      if (testClassName.equals(cln)) return e.getFileName();
    }
    return null;
  }

  private boolean isHighlightedCached(String className) {
    return highlightedCache.computeIfAbsent(className, name -> isHighlighted(name));
  }

  private boolean isHighlighted(String className) {
    try {
      int dot = className.lastIndexOf('.');
      String classfile = className.substring(0, dot + 1).replace('.', '/');
      URL resource = runner.testClassLoader.getResource(classfile);
      return resource.getProtocol().equals("file");
    } catch (Exception ex) {
      return false;
    }
  }

  private String stackTraceElementToString(StackTraceElement e, String testFileName) {
    boolean highlight =
        settings.color
            && (testClassName.equals(e.getClassName())
                || (testFileName != null && testFileName.equals(e.getFileName()))
                || isHighlightedCached(e.getClassName()));
    String color = highlight ? BOLD : FAINT;
    StringBuilder b = new StringBuilder();
    b.append(c("    at ", color));
    b.append(c(settings.decodeName(e.getClassName() + '.' + e.getMethodName()), color));
    b.append(c("(", color));

    if (e.isNativeMethod()) b.append(c("Native Method", color));
    else if (e.getFileName() == null) b.append(c("Unknown Source", color));
    else {
      b.append(c(e.getFileName(), color));
      if (e.getLineNumber() >= 0) b.append(':').append(c(String.valueOf(e.getLineNumber()), color));
    }
    return b.append(c(")", color)).toString();
  }
}
