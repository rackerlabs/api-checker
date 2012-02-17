package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._

class URI(id : String, label : String, val uri : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = next.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Int = {
    var ret = uriLevel
    if (uriLevel < req.URISegment.size) {
      req.URISegment(uriLevel) match {
        case uri() => ret=ret+1
        case _ => ret= -1
      }
    }
    ret
  }
}
