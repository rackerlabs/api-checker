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

  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
    if (checkStep (req, resp)) {
      val results : Array[Option[CheckerResult]] = next.map (n =>n.check(req, resp)).filter(s => s.isDefined)
      results.foreach(n => if (n.get.valid) return n)
      new MultiFailResult (results.map(n=>n.get))
    }
    None
  }
}

//
//  The start step
//
class Start(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override def checkStep(req : HttpServletRequest, resp : HttpServletResponse) : Boolean = true
}

//
//  The accept state, send the request over
//
class Accept(id : String, label : String) extends Step(id, label) {
  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
    //
    //  Send request...
    //
    return Some(AcceptResult)
  }
}

//
//  The URLFail state, return a 404
//
class URLFail(id : String, label : String) extends Step(id, label) {
  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
    //
    //  Generate a good error message with context
    //
    return Some(new URLFailResult("Could not find the given resource"))
  }
}

//
// Method fail state
//

class MethodFail(id : String, label : String) extends Step(id, label) {
  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Option[CheckerResult] = {
    //
    //  Generate a good error message with context
    //
    return Some(new MethodFailResult("Expecting method "))
  }
}
