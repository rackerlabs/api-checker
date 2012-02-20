package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

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
    return Some(new AcceptResult("ACCEPTED", uriLevel, id))
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
    var result : Option[URLFailResult] = None

    if (uriLevel < req.URISegment.size) {
      var ufr = new URLFailResult("Could not find the given resource", uriLevel, id)
      result = Some(ufr)
    }

    return result
  }
}

//
//  Like URLFail, but fails only if the current uri path is not matched
//  against the uri regex
//
class URLFailMatch(id : String, label : String, val uri : Regex) extends URLFail(id, label) {
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
    var result : Option[Result] = super.check (req, resp, uriLevel)
    if (result != None) {
      req.URISegment(uriLevel) match {
        case uri() => result = None
        case _ => ; // Pass our parent's result on the match.
      }
    }
    result
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
      val mfr = new MethodFailResult("Bad method", uriLevel, id)
      result = Some(mfr)
    }

    return result
  }
}

//
//  Like MethodFail, but fails only if the current method is not
//  matched against the uri regex
//
class MethodFailMatch(id : String, label : String, val method : Regex) extends MethodFail(id, label) {
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Option[Result] = {
    var result : Option[Result] = super.check(req, resp, uriLevel)
    if (result != None) {
      req.getMethod() match {
        case method() => result = None
        case _ => ; // Pass our parent's result on the match.
      }
    }
    result
  }
}
