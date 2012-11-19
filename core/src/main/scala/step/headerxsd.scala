package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

import org.xml.sax.SAXParseException

import scala.collection.JavaConversions._

class HeaderXSD(id : String, label : String, val name : String, val value : QName, schema : Schema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = name+" : "+value.toString
  val xsd = new XSDStringValidator(value, schema, id)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    val headers : Iterator[String] = req.getHeaders(name)
    var last_err : Option[SAXParseException] = None

    //
    //  If there exists at least one header matching the the name AND
    //  all of the headers with the name match the value type in the
    //  XSD, then return the uriLevel otherwise set an error and
    //  return -1
    //
    if (!headers.isEmpty && headers.filterNot(v => { last_err = xsd.validate(v);  last_err match { case None => true ; case Some(_) => false } }).isEmpty) {
      uriLevel
    } else {
     last_err match {
        case Some(_) => req.contentError = new Exception("Expecting requeried HTTP header "+name+" to match "+value+" "+last_err.get.getMessage(), last_err.get)
        case None => req.contentError = new Exception("Expecting required HTTP header "+name)
      }
      -1
    }
  }
}
