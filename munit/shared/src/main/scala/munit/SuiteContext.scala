package munit

import scala.concurrent.ExecutionContext

trait SuiteContext {

  def ec: ExecutionContext

}
