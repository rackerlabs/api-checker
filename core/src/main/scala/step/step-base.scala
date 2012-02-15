package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {
  def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult]
}

//
//  Steps that ore connected to other steps
//
abstract class ConnectedStep(id : String, label : String, val next : Array[Step]) extends Step (id, label) {

  def checkStep(req : HttpServletRequest, resp : HttpServletResponse) : Boolean
  val mismatchMessage : String = "Step Mismatch"

  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
    if (checkStep (req, resp)) {
      val results : Array[Option[CheckerResult]] = next.map (n =>n.check(req, resp)).filter(s => s.isDefined)
      results.foreach(n => if (n.get.valid) return n)
      return Some (new MultiFailResult (results.map(n=>n.get)))
    }
    Some(new MismatchResult(mismatchMessage))
  }
}
