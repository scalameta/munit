/*
rule = J2M
 */
package fix

import org.junit.Assert._

object J2MSuite {
  def foo(): Unit = {
    assertEquals(List(1), List(2))
  }
}
