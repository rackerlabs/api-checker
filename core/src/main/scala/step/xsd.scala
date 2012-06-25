package com.rackspace.com.papi.components.checker.step

import javax.xml.validation.Schema
import javax.xml.validation.Validator

import javax.xml.transform.dom.DOMSource

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.ValidatorPool._

class XSD(id : String, label : String, schema : Schema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The XML does not validate against the schema."

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var validator : Validator = null
    val capture = new ErrorCapture

    try {
      validator = borrowValidator (schema)
      validator.setErrorHandler(capture)
      validator.validate (new DOMSource (req.parsedXML))
      ret = uriLevel
    } catch {
      case e : Exception => req.contentError = e
    } finally {
      if (validator != null) returnValidator (schema, validator)
    }

    //
    //  Xerces, doesn't always propigate the ErrorCapture Fault...
    //
    if ((req.contentError == null) && (capture.error != None)) {
      req.contentError = capture.error.get
      ret = -1
    }

    ret
  }
}
