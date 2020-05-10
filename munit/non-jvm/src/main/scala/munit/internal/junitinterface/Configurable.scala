package munit.internal.junitinterface

trait Configurable {
  def configure(settings: Settings): Unit
}
