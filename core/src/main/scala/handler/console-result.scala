package com.rackspace.com.papi.components.checker.handler

import java.io.PrintStream

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.MultiFailResult

class ConsoleResultHandler(val out : PrintStream=System.out) extends ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result)  : Unit = {
    out.printf ("%s %s [valid=%s]\n", (req.getMethod() , req.getRequestURI(), result.valid))
    printPath (req, result)
    out.println
  }

  private def printPath (req : CheckerServletRequest, result : Result) : Unit = {
    out.print ("[")
    result.stepIDs.foreach (s => out.print(" "+s))
    if (result.isInstanceOf[MultiFailResult]) {
      val mfr = result.asInstanceOf[MultiFailResult]
      mfr.fails.foreach ( f => printPath(req, f))
    }
    out.print ("]")
  }
}
