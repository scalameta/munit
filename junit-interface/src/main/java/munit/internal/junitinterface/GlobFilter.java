package munit.internal.junitinterface;

import java.util.ArrayList;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public final class GlobFilter extends Filter {
  private final ArrayList<Pattern> patterns = new ArrayList<Pattern>();
  private final RunSettings settings;

  public GlobFilter(RunSettings settings, Iterable<String> globPatterns) {
    this.settings = settings;
    for (String p : globPatterns) patterns.add(compileGlobPattern(p));
  }

  @Override
  public String describe() {
    return "Filters out all tests not matched by the glob patterns";
  }

  @Override
  public boolean shouldRun(Description d) {
    if (d.isSuite()) return true;
    String plainName = settings.buildPlainName(d);

    for (Pattern p : patterns) if (p.matcher(plainName).matches()) return true;

    return false;
  }

  private static Pattern compileGlobPattern(String expr) {
    String[] a = expr.split("\\*", -1);
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < a.length; i++) {
      if (i != 0) b.append(".*");
      if (!a[i].isEmpty()) b.append(Pattern.quote(a[i].replaceAll("\n", "\\n")));
    }
    return Pattern.compile(b.toString());
  }
}
