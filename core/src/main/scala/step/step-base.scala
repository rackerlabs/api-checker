package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given URI level.
  //
  def check(req : HttpServletRequest, resp : HttpServletResponse, uriLevel : Int) : Option[CheckerResult]

  //
  //  Checks the step at the beginnig of the PATH
  //
  def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
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
  def checkStep(req : HttpServletRequest, resp : HttpServletResponse, uriLevel : Int) : Int

  //
  //  The error message when there is a step mismatch.
  //
  val mismatchMessage : String = "Step Mismatch"

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : HttpServletRequest, resp : HttpServletResponse, uriLevel : Int) : Option[CheckerResult] = {
    val nextURILevel = checkStep(req, resp, uriLevel)
    if (nextURILevel != -1) {
      val results : Array[Option[CheckerResult]] = next.map (n =>n.check(req, resp, nextURILevel)).filter(s => s.isDefined)
      results.foreach(n => if (n.get.valid) return n)
      return Some (new MultiFailResult (results.map(n=>n.get)))
    }
    Some(new MismatchResult(mismatchMessage))
  }
}
