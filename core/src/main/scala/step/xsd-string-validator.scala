package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema
import javax.xml.validation.ValidatorHandler

import org.xml.sax.SAXParseException
import org.xml.sax.ErrorHandler
import org.xml.sax.Attributes

import org.xml.sax.helpers.AttributesImpl

trait XSDStringValidator {
  val simpleType : QName
  val schema : Schema
  val elementName : String
  lazy val attributes : Attributes = {
    val ah = new AttributesImpl()
    ah.addAttribute ("http://www.w3.org/2001/XMLSchema-instance", "type",
                     "xsi:type", "", simpleType.getPrefix+":"+simpleType.getLocalPart)
    ah
  }

  def validate (in : String) : Boolean = {
    val capture = new ErrorCapture
    val handler = schema.newValidatorHandler
    val inArray = in.toCharArray()

    handler.setErrorHandler(capture)
    handler.startDocument
    handler.startPrefixMapping(simpleType.getPrefix, simpleType.getNamespaceURI)
    handler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance")
    handler.startElement("", elementName, elementName, attributes)
    handler.characters(inArray, 0, inArray.length)
    handler.endElement("", elementName, elementName)
    handler.endPrefixMapping(simpleType.getPrefix)
    handler.endPrefixMapping("xsi")
    handler.endDocument

    return capture.error == None
  }
}

//
//  An error handler that simply captures the last error.
//
private class ErrorCapture extends ErrorHandler {
  var error : Option[SAXParseException] = None

  def error(exception : SAXParseException) : Unit = {
    println (exception.getMessage)
    error = Some(exception)
  }

  def fatalError(exception : SAXParseException) : Unit = {
    println (exception.getMessage)
    error = Some(exception)
  }

  def warning(exception : SAXParseException) : Unit = {
    println (exception.getMessage)
  } //Log?
}
