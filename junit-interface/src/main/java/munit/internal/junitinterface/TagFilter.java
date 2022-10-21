package munit.internal.junitinterface;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class TagFilter extends Filter {
  final Set<String> includeTags, excludeTags;

  public TagFilter(Set<String> includeTags, Set<String> excludeTags) {
    this.includeTags = includeTags;
    this.excludeTags = excludeTags;
  }

  @Override
  public boolean shouldRun(Description description) {
    if (includeTags.isEmpty() && excludeTags.isEmpty()) return true;
    boolean isIncluded = includeTags.isEmpty();
    for (Annotation annotation : description.getAnnotations()) {
      if (annotation instanceof Tag) {
        Tag tag = (Tag) annotation;
        isIncluded = isIncluded || includeTags.contains(tag.value());
        boolean isExcluded = excludeTags.contains(tag.value());
        if (isExcluded) {
          return false;
        }
      }
    }
    return isIncluded;
  }

  @Override
  public String toString() {
    return "TagFilter{" + "includeTags=" + includeTags + ", excludeTags=" + excludeTags + '}';
  }

  @Override
  public String describe() {
    return toString();
  }
}
