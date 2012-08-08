package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class Header(id : String, label : String, val name : String, val value : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = name+" : "+value.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    val headerValue = req.getHeader(name)
    if (headerValue != null) {
      headerValue match {
        case value() => ret= uriLevel
        case _ => ret= -1
      }
    }
    if (ret == -1) {
      req.contentError = new Exception("Expecting an HTTP header "+name+" with value matching "+value.toString)
    }
    ret
  }
}
