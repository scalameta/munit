package munit.internal.junitinterface;

public class Ansi {
  // Standard ANSI sequences
  private static final String NORMAL = "\u001B[0m";
  private static final String HIGH_INTENSITY = "\u001B[1m";
  private static final String LOW_INTESITY = "\u001B[2m";
  private static final String BLACK = "\u001B[30m";
  private static final String RED = "\u001B[31m";
  private static final String GREEN = "\u001B[32m";
  private static final String YELLOW = "\u001B[33m";
  private static final String BLUE = "\u001B[34m";
  private static final String MAGENTA = "\u001B[35m";
  private static final String CYAN = "\u001B[36m";
  private static final String WHITE = "\u001B[37m";

  private static final String DARK_GREY = "\u001B[90m";
  private static final String LIGHT_RED = "\u001B[91m";
  private static final String LIGHT_GREEN = "\u001B[92m";
  private static final String LIGHT_YELLOW = "\u001B[93m";
  private static final String LIGHT_BLUE = "\u001B[94m";
  private static final String LIGHT_MAGENTA = "\u001B[95m";
  private static final String LIGHT_CYAN = "\u001B[96m";

  public static String c(String s, String colorSequence) {
    if (colorSequence == null) return s;
    else return colorSequence + s + NORMAL;
  }

  public static String filterAnsi(String s) {
    if (s == null) return null;
    int len = s.length();
    StringBuilder b = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == '\u001B') {
        do {
          i++;
        } while (s.charAt(i) != 'm');
      } else b.append(c);
    }
    return b.toString();
  }

  private Ansi() {}

  public static final String INFO = LIGHT_BLUE;
  public static final String SKIPPED = YELLOW;
  public static final String SUCCESS1 = GREEN;
  public static final String SUCCESS2 = GREEN;
  public static final String ERRCOUNT = LIGHT_RED;
  public static final String IGNCOUNT = YELLOW;
  public static final String WARNMSG = LIGHT_YELLOW;
  public static final String ERRMSG = LIGHT_RED;
  public static final String NNAME1 = YELLOW;
  public static final String NNAME2 = YELLOW;
  public static final String NNAME3 = YELLOW;
  public static final String ENAME1 = YELLOW;
  public static final String ENAME2 = LIGHT_RED;
  public static final String ENAME3 = YELLOW;
  public static final String FAILURE1 = LIGHT_RED;
  public static final String FAILURE2 = LIGHT_RED;
  public static final String BOLD = HIGH_INTENSITY;
  public static final String FAINT = DARK_GREY;
}
