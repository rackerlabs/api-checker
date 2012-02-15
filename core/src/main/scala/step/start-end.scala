package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//
//  The start step
//
class Start(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override def checkStep(req : HttpServletRequest, resp : HttpServletResponse) : Boolean = true
  override val mismatchMessage : String = "Bad Start Node?"
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
    //  If there is stuff in the path, then this error is
    //  applicable. Generate the error, commit the message. No URI
    //  stuff, then return None.
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
    //  If there is URL stuff return NONE.  Otherwise generate an
    //  error, commit the message.
    //
    return Some(new MethodFailResult("Expecting method "))
  }
}
