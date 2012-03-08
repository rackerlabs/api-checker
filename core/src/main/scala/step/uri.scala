package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class URI(id : String, label : String, val uri : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = uri.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    if (uriLevel < req.URISegment.size) {
      req.URISegment(uriLevel) match {
        case uri() => ret= uriLevel+1
        case _ => ret= -1
      }
    }
    ret
  }
}
