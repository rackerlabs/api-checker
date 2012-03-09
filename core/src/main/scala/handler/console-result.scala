package com.rackspace.com.papi.components.checker.handler

import java.io.PrintStream
import java.net.URLDecoder

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.MultiFailResult
import com.rackspace.com.papi.components.checker.step.MismatchResult

import javax.servlet.FilterChain

import org.w3c.dom.Document

class ConsoleResultHandler(val out : PrintStream=System.out) extends ResultHandler {
  def init (checker : Option[Document]) : Unit = {}

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    Console.withOut(out) {
      def valid(v : Boolean) = {
        var vout = "["
        if (v) {vout = vout+Console.GREEN+"VALID"} else {vout = vout+Console.RED+"NOPE"}
        vout+Console.RESET+"]"
      }

      printf ("%s %s %s ", valid(result.valid), req.getMethod() , URLDecoder.decode(req.getRequestURI(), "UTF-8"))
      printPath (req, result)
      print (" ")
      printResult(result)
      println
    }
  }

  private def printPath (req : CheckerServletRequest, result : Result) : Unit = {
    Console.withOut(out) {
      result match {
        case mfr : MultiFailResult => print ("{")
        case mr  : MismatchResult => print ("(")
        case other => print ("[")
      }
      result.stepIDs.foreach (s => out.print(" "+s+" "))
      result match {
        case mfr : MultiFailResult => mfr.fails.foreach ( f => printPath(req, f)) ; print ("}")
        case mr  : MismatchResult => print (")")
        case other => print ("]")
      }
    }
  }

  private def printResult (result : Result) : Unit = {
    Console.withOut(out) {
      result match {
        case mrf : MultiFailResult => print (mrf.reduce.get)
        case other => print (other)
      }
    }
  }
}
