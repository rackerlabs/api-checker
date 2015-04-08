/***
 *   Copyright 2014 Rackspace US, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.{Schema, ValidatorHandler}

import com.rackspace.com.papi.components.checker.util.ValidatorHandlerPool._
import org.xml.sax.{Attributes, ErrorHandler, Locator, SAXException, SAXParseException}
import org.xml.sax.helpers.AttributesImpl

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
