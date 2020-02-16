package munit.internal.junitinterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;

final class OutputCapture
{
  private final PrintStream originalOut = System.out;
  private final PrintStream originalScalaOut = getScalaOut();
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private final PrintStream prBuffer = new PrintStream(buffer, true);
  private final boolean scalaOutSet;

  private OutputCapture()
  {
    //System.err.println("***** Replacing "+System.out+" with buffer "+prBuffer);
    System.out.flush();
    System.setOut(prBuffer);
    scalaOutSet = setScalaOut(prBuffer);
  }

  static OutputCapture start() { return new OutputCapture(); }

  void stop()
  {
    //System.err.println("***** Restoring "+originalOut);
    System.out.flush();
    System.setOut(originalOut);
    if(scalaOutSet) setScalaOut(originalScalaOut);
  }

  void replay() throws IOException
  {
    //System.err.println("***** Replaying to "+System.out);
    System.out.write(buffer.toByteArray());
    System.out.flush();
  }

  private static PrintStream getScalaOut()
  {
    try
    {
      Class<?> cl = Class.forName("scala.Console");
      Method m = cl.getMethod("out");
      return (PrintStream)m.invoke(null);
    }
    catch(Throwable t)
    {
      //System.err.println("Error getting Scala console:");
      //t.printStackTrace(System.err);
      return null;
    }
  }

  private static boolean setScalaOut(PrintStream p)
  {
    try
    {
      Class<?> cl = Class.forName("scala.Console");
      Method m = cl.getMethod("setOut", PrintStream.class);
      m.invoke(null, p);
      return true;
    }
    catch(Throwable t)
    {
      //System.err.println("Error setting Scala console:");
      //t.printStackTrace(System.err);
      return false;
    }
  }
}
