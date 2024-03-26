package org.junit.runner

import org.junit.runner.notification.RunNotifier

abstract class Runner {
  def run(notifier: RunNotifier): Unit
  def getDescription(): Description
}
