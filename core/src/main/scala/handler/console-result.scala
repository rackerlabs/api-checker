package com.rackspace.com.papi.components.checker.handler

import java.io.PrintStream
import java.net.URLDecoder

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.MultiFailResult

class ConsoleResultHandler(val out : PrintStream=System.out) extends ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result)  : Unit = {
    Console.withOut(out) {
      def valid(v : Boolean) = {
        var vout = "["
        if (v) {vout = vout+Console.GREEN+"VALID"} else {vout = vout+Console.RED+"NOPE"}
        vout+Console.RESET+"]"
      }

      printf ("%s %s %s ", valid(result.valid), req.getMethod() , URLDecoder.decode(req.getRequestURI(), "UTF-8"))
      printPath (req, result)
      println
    }
  }

  private def printPath (req : CheckerServletRequest, result : Result) : Unit = {
    Console.withOut(out) {
      print ("[")
      result.stepIDs.foreach (s => out.print(" "+s))
      if (result.isInstanceOf[MultiFailResult]) {
        val mfr = result.asInstanceOf[MultiFailResult]
        mfr.fails.foreach ( f => printPath(req, f))
      }
      print ("]")
    }
  }
}
