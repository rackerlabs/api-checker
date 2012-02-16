package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.ErrorResult

abstract class ResultHandler {
  def handle (result : Result) : Unit
}
