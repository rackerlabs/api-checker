package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema
import javax.xml.validation.ValidatorHandler

import org.xml.sax.SAXParseException
import org.xml.sax.SAXException
import org.xml.sax.ErrorHandler
import org.xml.sax.Attributes
import org.xml.sax.Locator

import org.xml.sax.helpers.AttributesImpl

import com.rackspace.com.papi.components.checker.util.ValidatorHandlerPool._

class XSDStringValidator(val simpleType : QName, val schema : Schema, val elementName : String) extends Locator {
  lazy val attributes : Attributes = {
    val ah = new AttributesImpl()
    ah.addAttribute ("http://www.w3.org/2001/XMLSchema-instance", "type",
                     "xsi:type", "", simpleType.getPrefix+":"+simpleType.getLocalPart)
    ah
  }

  def validate (in : String) : Option[SAXParseException] = {
    val capture = new ErrorCapture
    var handler : ValidatorHandler = null

    try {
      val inArray = in.toCharArray()

      handler = borrowValidatorHandler(schema)
      handler.setErrorHandler(capture)
      handler.setDocumentLocator (this)
      handler.startDocument
      handler.startPrefixMapping(simpleType.getPrefix, simpleType.getNamespaceURI)
      handler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance")
      handler.startElement("", elementName, elementName, attributes)
      handler.characters(inArray, 0, inArray.length)
      handler.endElement("", elementName, elementName)
      handler.endPrefixMapping(simpleType.getPrefix)
      handler.endPrefixMapping("xsi")
      handler.endDocument

    } catch {
      case e : SAXException => /* Ignore here, the error is reported by capture */
    } finally {
      if (handler != null) {
        returnValidatorHandler(schema, handler)
      }
    }

    return capture.error
  }

  //
  // Locator calls
  //
  def getPublicId = ""
  def getSystemId = ""
  def getLineNumber = 1
  def getColumnNumber = 1
}

//
//  An error handler that simply captures the first error it sees.  It
//  ignores warnings.
//
private class ErrorCapture extends ErrorHandler {
  var error : Option[SAXParseException] = None

  def error(exception : SAXParseException) : Unit = {
    if (error == None) {
      error = Some(exception)
      throw exception
    }
  }

  def fatalError(exception : SAXParseException) : Unit = {
    if (error == None) {
      error = Some(exception)
      throw exception
    }
  }

  def warning(exception : SAXParseException) : Unit = {
    //Log?
  }
}
