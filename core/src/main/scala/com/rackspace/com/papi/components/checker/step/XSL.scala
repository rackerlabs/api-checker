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

import javax.servlet.FilterChain
import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.{ErrorListener, Source, Templates, Transformer, TransformerException}
import javax.xml.transform.dom.{DOMResult, DOMSource}
import javax.xml.transform.stream.StreamSource

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.TransformPool.{borrowTransformer, returnTransformer}
import com.rackspace.com.papi.components.checker.util.XMLParserPool.{borrowParser, returnParser}


class XSL(id : String, label : String, templates : Templates, val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "Error while performing translation"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var ret : Option[StepContext] = Some(context)
    var parser : DocumentBuilder = null
    var transform : Transformer = null
    val capture = new TransformErrorCapture
    var error : Exception = null

    try {
      transform = borrowTransformer(templates)

      //
      //  We create a new document because Saxon doesn't pool
      //  DocumentBuilders and letting saxon create a new builder
      //  slows things down.
      //
      parser = borrowParser
      val result = parser.newDocument()
      returnParser(parser) ; parser = null

      val source : Source = {
        if (req.parsedXML != null) {
          new DOMSource(req.parsedXML)
        } else {
          new StreamSource(req.getInputStream())
        }
      }

      transform.setErrorListener (capture)
      transform.transform (source, new DOMResult(result))
      req.parsedXML = result
    } catch {
      case e : Exception => error = e
    } finally {
      if (transform != null) returnTransformer(templates, transform)
      if (parser != null) returnParser(parser)
    }

    if (capture.error.isDefined) {
      req.contentError(capture.error.get, capture.code.get, priority)
      ret = None
    } else if (error != null) {
      req.contentError = error
      req.contentErrorPriority = priority
      ret = None
    }

    ret
  }
}

object TransformErrorCapture {
  //
  //  An error code can be annotated in an output message of a
  //  transformation.  This will be used instead of DEFAULT_ERROR_CODE
  //  if present.
  //
  //  The format for the code is <<C:501>>, which will result in a 501.
  //
  //  TODO: Find a better way of interfacing with XSL. This technique
  //  is brittle. XPathExtension(?)
  //
  val errorCode = "C:(\\d+):C".r

  //
  //  If no error code is found in the output the following will be
  //  used.
  //
  val DEFAULT_ERROR_CODE = 400
}

import com.rackspace.com.papi.components.checker.step.TransformErrorCapture._

private class TransformErrorCapture extends ErrorListener {
  var error : Option[TransformerException] = None
  var msg : Option[String] = None
  var code : Option[Int] = None

  def error (exception : TransformerException) : Unit = {
    if (error.isEmpty) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("xsl:message") && msg.isDefined) {
        error = Some(new TransformerException(msg.get, error.get))
      }

      code = getCodeFromError(error)
      error = cleanUpError(error)
    }
  }

  def fatalError (exception : TransformerException) : Unit = {
    if (error.isEmpty) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("termination") && msg.isDefined) {
        error = Some(new TransformerException(msg.get, error.get))
      }

      code = getCodeFromError(error)
      error = cleanUpError(error)
    }
  }

  def warning (exception : TransformerException) : Unit = {
    msg = Some(exception.getMessage())
  }

  private def getCodeFromError(exception : Option[TransformerException]) : Option[Int] = {
    exception match {
      case None => None
      case Some(e) =>
        errorCode findFirstIn e.getMessage()  match {
          case Some(errorCode(code)) => Some(code.toInt)
          case None => Some(DEFAULT_ERROR_CODE)
        }
    }
  }

  private def cleanUpError(exception : Option[TransformerException]) : Option[TransformerException] = {
    exception match {
      case None => None
      case Some(e) =>
        errorCode findFirstIn e.getMessage()  match {
          case Some(c) => Some(new TransformerException(errorCode.replaceAllIn(e.getMessage(), ""), e))
          case None => exception
        }
    }
  }
}
