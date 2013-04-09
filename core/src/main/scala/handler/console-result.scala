package com.rackspace.com.papi.components.checker.handler

import java.io.PrintStream
import java.net.URLDecoder

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result

import javax.servlet.FilterChain

import org.w3c.dom.Document

/*
 * This handler prints the request & Result to the given PrintStream.
 */
class ConsoleResultHandler(val out : PrintStream=System.out) extends ResultHandler {
  def init (validator : Validator, checker : Option[Document]) : Unit = {}

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    Console.withOut(out) {
      def valid(v : Boolean) = {
        var vout = "["
        if (v) {vout = vout+Console.GREEN+"VALID"} else {vout = vout+Console.RED+"NOPE"}
        vout+Console.RESET+"]"
      }

      printf ("%s %s %s %s\n", valid(result.valid), req.getMethod() , URLDecoder.decode(req.getRequestURI(), "UTF-8"),result.toString)
    }
  }
}
