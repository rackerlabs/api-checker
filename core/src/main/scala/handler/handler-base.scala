package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.servlet._

abstract class ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result) : Unit
}

class NullHandler extends ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result) : Unit = {}
}
