package com.rackspace.com.papi.components.checker.handler

import java.io.PrintStream

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.ErrorResult

class ConsoleResult(val out : PrintStream=System.out) extends ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result)  : Unit = {
    out.printf ("%s %s [valid=%s]", (req.getMethod() , req.getRequestURI(), result.valid))
  }
}
