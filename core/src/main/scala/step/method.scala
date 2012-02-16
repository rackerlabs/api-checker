package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._

class Method(id : String, label : String, val method : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = next.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Int = {
    req.getMethod() match {
      case method() => return uriLevel
      case _ => return -1
    }
  }
}
