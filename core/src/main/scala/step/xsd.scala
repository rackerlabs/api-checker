package com.rackspace.com.papi.components.checker.step

import javax.xml.validation.Schema
import javax.xml.validation.Validator

import javax.xml.transform.dom.DOMSource
import javax.xml.transform.dom.DOMResult

import javax.xml.parsers.DocumentBuilder

import javax.servlet.FilterChain

import org.w3c.dom.Document

import com.rackspace.com.papi.components.checker.servlet._

import com.rackspace.com.papi.components.checker.util.ValidatorPool.borrowValidator
import com.rackspace.com.papi.components.checker.util.ValidatorPool.returnValidator
import com.rackspace.com.papi.components.checker.util.XMLParserPool.borrowParser
import com.rackspace.com.papi.components.checker.util.XMLParserPool.returnParser

class XSD(id : String, label : String, schema : Schema, transform : Boolean, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The XML does not validate against the schema."

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var validator : Validator = null
    var parser : DocumentBuilder = null
    val capture = new ErrorCapture //Used to capture parse errors
    var error : Exception = null   //Other errors may be caught here

    try {
      validator = borrowValidator(schema)
      validator.setErrorHandler(capture)

      if (transform) {
        //
        //  We create a new document because Saxon doesn't pool
        //  DocumentBuilders and letting saxon create a new builder
        //  slows things down.
        //
        parser = borrowParser
        val result = parser.newDocument()
        returnParser(parser); parser = null

        validator.validate (new DOMSource (req.parsedXML), new DOMResult(result))
        req.parsedXML = result
      } else {
        validator.validate (new DOMSource (req.parsedXML))
      }
      ret = uriLevel
    } catch {
      case e : Exception => error = e
    } finally {
      if (validator != null) returnValidator (schema, validator)
      if (parser != null) returnParser(parser)
    }

    //
    //  Always give precedence to parse errors.
    //
    if (capture.error != None) {
      req.contentError = capture.error.get
      ret = -1
    } else if (error != null) {
      req.contentError = error
      ret = -1
    }

    ret
  }
}
