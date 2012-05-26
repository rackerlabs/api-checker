package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given URI level.
  //
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Option[Result]

  //
  //  Checks the step at the beginnig of the PATH
  //
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain) : Option[Result] = {
    return check(req,resp,chain)
  }
}

//
//  Steps that ore connected to other steps
//
abstract class ConnectedStep(id : String, label : String, val next : Array[Step]) extends Step (id, label) {

  //
  //  Check the currest step.  If the step is a match return the next
  //  uriLevel.  If not return -1.
  //
  def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = -1

  //
  //  The error message when there is a step mismatch.
  //
  val mismatchMessage : String = "Step Mismatch"

  //
  //  Go to the next step.
  //
  def nextStep (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Array[Result] = {
    val results : Array[Result] = next.map (n =>n.check(req, resp, chain, uriLevel)).filter(s => s.isDefined).map(r => r.get)
    results.foreach(n => if (n.valid) { return Array(n) })
    results
  }

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Option[Result] = {
    var result : Option[Result] = None
    val nextURILevel = checkStep(req, resp, chain, uriLevel)

    if (nextURILevel != -1) {
      val results : Array[Result] = nextStep (req, resp, chain, nextURILevel)
      if (results.size == 1) {
        results(0).addStepId(id)
        result = Some(results(0))
      } else {
        result = Some(new MultiFailResult (results, uriLevel, id))
      }

    } else {
      result = Some(new MismatchResult(mismatchMessage, uriLevel, id))
    }

    return result
  }
}
