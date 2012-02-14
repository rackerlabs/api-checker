package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//
//  Base class for all other steps
//

abstract class Step(val id : String, val label : String) {
  def check(req : HttpServletRequest, resp : HttpServletResponse) : Boolean 
}

//
//  Steps that ore connected to other steps
//
abstract class ConnectedStep(id : String, label : String, val next : Array[Step]) extends Step (id, label) {

  def checkStep(req : HttpServletRequest, resp : HttpServletResponse) : Boolean

  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Boolean = {
    if (checkStep (req, resp)) {
      next.foreach {n => if (n.check(req, resp)) return true}
    }
    false
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
  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Boolean = {
    //
    //  Send request...
    //
    return true
  }
}

//
//  The URLFail state, return a 404
//
class URLFail(id : String, label : String) extends Step(id, label) {
  override def check(req : HttpServletRequest, resp : HttpServletResponse) : Boolean = {
    //
    //  Send 404
    //
    return true
  }
}
