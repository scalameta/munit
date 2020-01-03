package funsuite

import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths

class Arguments {
  var includeTags: List[Tag] = Nil
  var excludeTags: List[Tag] = Nil
  var includeFilter: List[PathMatcher] = Nil
  var excludeFilter: List[PathMatcher] = Nil

  def matchingTests(suite: FunSuite): List[Test] = {
    val parents = suite.getClass.getCanonicalName().split('.')
    if (parents.isEmpty) suite.tests.toList
    else {
      val it = parents.iterator
      val path = it.foldLeft(Paths.get(it.next())) {
        case (dir, name) => dir.resolve(name)
      }
      suite.tests.iterator.filter(test => matchesFilter(path, test)).toList
    }
  }

  private def matchesFilter(
      parent: Path,
      test: Test
  ): Boolean = {
    val path = parent.resolve(test.name)
    val isIncluded =
      includeFilter.isEmpty ||
        includeFilter.exists(_.matches(path))
    val isExcluded =
      excludeFilter.exists(_.matches(path))
    isIncluded && !isExcluded
  }
  private def matchesTags(
      test: Test
  ): Boolean = {
    val isIncluded =
      includeTags.isEmpty ||
        includeTags.exists(test.tags.contains)
    val isExcluded =
      includeTags.exists(test.tags.contains)
    isIncluded && !isExcluded
  }
}
