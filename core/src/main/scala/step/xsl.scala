package com.rackspace.com.papi.components.checker.step

import javax.xml.transform.Templates
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.ErrorListener

import javax.xml.transform.Source

import javax.xml.transform.dom.DOMSource
import javax.xml.transform.dom.DOMResult

import javax.xml.transform.stream.StreamSource

import javax.xml.parsers.DocumentBuilder

import javax.servlet.FilterChain

import org.w3c.dom.Document

import com.rackspace.com.papi.components.checker.servlet._

import com.rackspace.com.papi.components.checker.util.TransformPool.borrowTransformer
import com.rackspace.com.papi.components.checker.util.TransformPool.returnTransformer
import com.rackspace.com.papi.components.checker.util.XMLParserPool.borrowParser
import com.rackspace.com.papi.components.checker.util.XMLParserPool.returnParser


class XSL(id : String, label : String, templates : Templates, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "Error while performing translation"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = uriLevel
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

    if (capture.error != None) {
      req.contentError(capture.error.get, capture.code.get)
      ret = -1
    } else if (error != null) {
      req.contentError = error
      ret = -1
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

import TransformErrorCapture._

private class TransformErrorCapture extends ErrorListener {
  var error : Option[TransformerException] = None
  var msg : Option[String] = None
  var code : Option[Int] = None

  def error (exception : TransformerException) : Unit = {
    if (error == None) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("xsl:message") && msg != None) {
        error = Some(new TransformerException(msg.get, error.get))
      }

      code = getCodeFromError(error)
      error = cleanUpError(error)
    }
  }

  def fatalError (exception : TransformerException) : Unit = {
    if (error == None) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("termination") && msg != None) {
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
