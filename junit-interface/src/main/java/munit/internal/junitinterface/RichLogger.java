package munit.internal.junitinterface;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.runner.Description;
import sbt.testing.Logger;
import static munit.internal.junitinterface.Ansi.*;


final class RichLogger
{
  private final Logger[] loggers;
  private final RunSettings settings;
  private final JUnitRunner runner;
  /* The top element is the test class of the currently executing test */
  private final Stack<String> currentTestClassName = new Stack<String>();
  private final Map<String, Boolean> highlightedCache = new HashMap<>();
  private final ConcurrentHashMap<String, ConcurrentLinkedDeque<String>> buffers = new ConcurrentHashMap<>();

  RichLogger(Logger[] loggers, RunSettings settings, String testClassName, JUnitRunner runner)
  {
    this.loggers = loggers;
    this.settings = settings;
    this.runner = runner;
    currentTestClassName.push(testClassName);
  }

  void pushCurrentTestClassName(String s) { currentTestClassName.push(s); }

  void popCurrentTestClassName()
  {
    if(currentTestClassName.size() > 1) currentTestClassName.pop();
  }

  void error(Description desc, String s)
  {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.error(s);
        else l.error(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(desc, s);
    } else {
      System.out.println(s);
    }
  }

  void error(Description desc, String s, Throwable t)
  {
    error(desc, s);
    if(t != null && (settings.logAssert || !(t instanceof AssertionError))) logStackTrace(desc, t);
  }

  void info(Description desc, String s)
  {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.info(s);
        else l.info(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(desc, s);
    } else {
      System.out.println(s);
    }
  }

  void warn(Description desc, String s)
  {
    if (settings.useSbtLoggers) {
      for (Logger l : loggers)
        if (settings.color && l.ansiCodesSupported()) l.warn(s);
        else l.warn(filterAnsi(s));
    } else if (settings.useBufferedLoggers) {
      bufferMessage(desc, s);
    } else {
      System.out.println(s);
    }
  }

  void flush(Description desc) {
    ConcurrentLinkedDeque<String> logs = buffers.remove(String.valueOf(desc.getClassName()));
    if (logs != null) {
      System.out.println(String.join("\n", logs));
    }
  }

  private void bufferMessage(Description desc, String message) {
    ConcurrentLinkedDeque<String> logs = buffers.computeIfAbsent(String.valueOf(desc.getClassName()), d -> new ConcurrentLinkedDeque<>());
    logs.push(message);
  }
  private void logStackTrace(Description desc, Throwable t)
  {
    StackTraceElement[] trace = t.getStackTrace();
    String testClassName = currentTestClassName.peek();
    String testFileName = settings.color ? findTestFileName(trace, testClassName) : null;
    logStackTracePart(desc, trace, trace.length-1, 0, t, testClassName, testFileName);
  }

  private void logStackTracePart(Description desc, StackTraceElement[] trace, int m, int framesInCommon, Throwable t, String testClassName, String testFileName)
  {
    final int m0 = m;
    int top = 0;
    for(int i=top; i<=m; i++)
    {
      if(trace[i].toString().startsWith("org.junit.") ||
          trace[i].toString().startsWith("org.hamcrest.") ||
          trace[i].toString().startsWith("org.scalatest."))
      {
        if(i == top) top++;
        else
        {
          m = i-1;
          while(m > top)
          {
            String s = trace[m].toString();
            if(!s.startsWith("java.lang.reflect.") && !s.startsWith("sun.reflect.")) break;
            m--;
          }
          break;
        }
      }
    }
    for(int i=top; i<=m; i++) {
      if (!trace[i].getClassName().startsWith("scala.runtime."))
        error(desc, stackTraceElementToString(trace[i], testClassName, testFileName));
    }
    if(m0 != m)
    {
      // skip junit-related frames
      error(desc, "    ...");
    }
    else if(framesInCommon != 0)
    {
      // skip frames that were in the previous trace too
      error(desc, "    ... " + framesInCommon + " more");
    }
    logStackTraceAsCause(desc, trace, t.getCause(), testClassName, testFileName);
  }

  private void logStackTraceAsCause(Description desc, StackTraceElement[] causedTrace, Throwable t, String testClassName, String testFileName)
  {
    if(t == null) return;
    StackTraceElement[] trace = t.getStackTrace();
    int m = trace.length - 1, n = causedTrace.length - 1;
    while(m >= 0 && n >= 0 && trace[m].equals(causedTrace[n]))
    {
      m--;
      n--;
    }
    error(desc, "Caused by: " + t);
    logStackTracePart(desc, trace, m, trace.length-1-m, t, testClassName, testFileName);
  }

  private String findTestFileName(StackTraceElement[] trace, String testClassName)
  {
    for(StackTraceElement e : trace)
    {
      String cln = e.getClassName();
      if(testClassName.equals(cln)) return e.getFileName();
    }
    return null;
  }

  private boolean isHighlightedCached(String className) {
    return highlightedCache.computeIfAbsent(className, name -> isHighlighted(name));
  }

  private boolean isHighlighted(String className) {
    try {
      int dot = className.lastIndexOf('.');
      String classfile = className.substring(0, dot + 1).replace('.','/');
      URL resource = runner.testClassLoader.getResource(classfile);
      return resource.getProtocol().equals("file");
    } catch (Exception ex) {
      return false;
    }
  }

  private String stackTraceElementToString(StackTraceElement e, String testClassName, String testFileName)
  {
    boolean highlight = settings.color && (
        testClassName.equals(e.getClassName()) ||
        (testFileName != null && testFileName.equals(e.getFileName())) ||
        isHighlightedCached(e.getClassName())
      );
    String color = highlight ? BOLD : FAINT;
    StringBuilder b = new StringBuilder();
    b.append(c("    at ", color));
    b.append(c(settings.decodeName(e.getClassName() + '.' + e.getMethodName()) , color));
    b.append(c("(", color));

    if(e.isNativeMethod()) b.append(c("Native Method", color));
    else if(e.getFileName() == null) b.append(c("Unknown Source", color));
    else
    {
      b.append(c(e.getFileName(), color));
      if(e.getLineNumber() >= 0)
        b.append(':').append(c(String.valueOf(e.getLineNumber()), color));
    }
    return b.append(c(")", color)).toString();
  }

}
