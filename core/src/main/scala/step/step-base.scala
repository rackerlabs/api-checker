package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.servlet._

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given URI level.
  //
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result]

  //
  //  Checks the step at the beginnig of the PATH
  //
  def check(req : CheckerServletRequest, resp : CheckerServletResponse) : Option[Result] = {
    return check(req,resp,0)
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
  def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Int

  //
  //  The error message when there is a step mismatch.
  //
  val mismatchMessage : String = "Step Mismatch"

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
    val nextURILevel = checkStep(req, resp, uriLevel)
    if (nextURILevel != -1) {
      val results : Array[Option[Result]] = next.map (n =>n.check(req, resp, nextURILevel)).filter(s => s.isDefined)
      results.foreach(n => if (n.get.valid) return n)
      return Some (new MultiFailResult (results.map(n=>n.get), uriLevel))
    }
    Some(new MismatchResult(mismatchMessage, uriLevel))
  }
}
