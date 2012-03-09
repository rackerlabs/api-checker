package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class Method(id : String, label : String, val method : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = method.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    if (uriLevel >= req.URISegment.size) {
      req.getMethod() match {
        case method() => ret= uriLevel
        case _ => ret= -1
      }
    }
    ret
  }
}
