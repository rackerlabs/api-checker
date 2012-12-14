package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.MultiFailResult

import com.rackspace.com.papi.components.checker.servlet._

import com.rackspace.com.papi.components.checker.Validator

import javax.servlet.FilterChain

import org.w3c.dom.Document
import org.w3c.dom.Element

import com.yammer.metrics.scala.Instrumented
import com.yammer.metrics.scala.Meter

class InstrumentedHandler extends ResultHandler with Instrumented {
  private var stepMeters : Map[String, Meter] = Map.empty

  def init (validator : Validator, checker : Option[Document]) : Unit = {
    if (checker != None) {
      val elms = checker.get.getElementsByTagNameNS("http://www.rackspace.com/repose/wadl/checker",
                                                    "step")
      for (i <- 0 to (elms.getLength-1)) {
        val elm = elms.item(i).asInstanceOf[Element]

        val id = elm.getAttribute("id")
        val etype = elm.getAttribute("type")

        stepMeters = stepMeters + (id -> metrics.meter(id, etype, validator.name))
      }
    }
  }

  private def markResult (result : Result) : Unit = {
    result.stepIDs.foreach (s => stepMeters(s).mark)
    if (result.isInstanceOf[MultiFailResult]) {
      result.asInstanceOf[MultiFailResult].fails.foreach (f => markResult(f))
    }
  }

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    markResult(result)
  }

}
