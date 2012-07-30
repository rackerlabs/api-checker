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
      req.contentError = capture.error.get
      ret = -1
    } else if (error != null) {
      req.contentError = error
      ret = -1
    }

    ret
  }
}

private class TransformErrorCapture extends ErrorListener {
  var error : Option[TransformerException] = None
  var msg : Option[String] = None

  def error (exception : TransformerException) : Unit = {
    if (error == None) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("xsl:message") && msg != None) {
        error = Some(new TransformerException(msg.get, error.get))
      }
    }
  }

  def fatalError (exception : TransformerException) : Unit = {
    if (error == None) {
      error = Some(exception)

      val message = exception.getMessage()
      if (message.contains("termination") && msg != None) {
        error = Some(new TransformerException(msg.get, error.get))
      }
    }
  }

  def warning (exception : TransformerException) : Unit = {
    msg = Some(exception.getMessage())
  }
}
