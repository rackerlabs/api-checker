package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class HeaderXSD(id : String, label : String, val name : String, val value : QName, schema : Schema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = name+" : "+value.toString
  val xsd = new XSDStringValidator(value, schema, id)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    val headerValue = req.getHeader(name)
    if (headerValue != null) {
      val error = xsd.validate(headerValue)
      if (error != None) {
        req.contentError = error.get
      } else {
        ret = uriLevel
      }
    }
    ret
  }
}
