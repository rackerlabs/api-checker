package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.servlet._

//
//  The start step
//
class Start(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Int = uriLevel
  override val mismatchMessage : String = "Bad Start Node?"
}

//
//  The accept state, send the request over
//
class Accept(id : String, label : String) extends Step(id, label) {
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
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
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
    //
    //  If there is stuff in the path, then this error is
    //  applicable. Generate the error, commit the message. No URI
    //  stuff, then return None.
    //
    return Some(new URLFailResult("Could not find the given resource", uriLevel))
  }
}

//
// Method fail state
//

class MethodFail(id : String, label : String) extends Step(id, label) {
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
    //
    //  If there is URL stuff return NONE.  Otherwise generate an
    //  error, commit the message.
    //
    var result : Option[MethodFailResult] = None

    if (uriLevel >= req.URISegment.size) {
      val mfr = new MethodFailResult("Bad method", uriLevel)
      result = Some(mfr)
      resp.setStatus(mfr.code)
    }

    return result
  }
}
