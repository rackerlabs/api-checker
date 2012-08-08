package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

import org.xml.sax.SAXParseException

class HeaderXSD(id : String, label : String, val name : String, val value : QName, schema : Schema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = name+" : "+value.toString
  val xsd = new XSDStringValidator(value, schema, id)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var err : Option[SAXParseException] = None
    val headerValue = req.getHeader(name)
    if (headerValue != null) {
      err = xsd.validate(headerValue)
      if (err != None) {
        req.contentError = err.get
      } else {
        ret = uriLevel
      }
    }
    if (ret == -1) {
      if (err== None) {
        req.contentError = new Exception("Expecting required HTTP header "+name)
      }else {
        req.contentError = new Exception("Expecting requeried HTTP header "+name+" to match "+value+" "+err.get.getMessage(), err.get)
      }
    }
    ret
  }
}
