package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

import scala.collection.JavaConversions._

class Header(id : String, label : String, val name : String, val value : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = name+" : "+value.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    val headers : Iterator[String] = req.getHeaders(name)

    //
    //  If there exists at least one header matching the the name AND
    //  all of the headers with the name match the value regex, then
    //  return the uriLevel otherwise set an error and return -1
    //
    if (!headers.isEmpty && headers.filterNot(v => v match { case value() => true ; case _ => false } ).isEmpty) {
      uriLevel
    } else {
      req.contentError = new Exception("Expecting an HTTP header "+name+" to have a value matching "+value.toString())
      -1
    }
  }
}
