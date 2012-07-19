package com.rackspace.com.papi.components.checker.servlet

import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.URLDecoder

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.json.simple.JSONAware
import org.w3c.dom.Document

import com.rackspace.com.papi.components.checker.util.IdentityTransformPool._

//
//  Request Keys
//
object RequestAttributes {
  val PARSED_XML    = "com.rackspace.com.papi.components.checker.servlet.ParsedXML"
  val PARSED_JSON   = "com.rackspace.com.papi.components.checker.servlet.ParsedJSON"
  val CONTENT_ERROR = "com.rackspace.com.papi.components.checker.servlet.ContentError"
}

import RequestAttributes._

//
//  An HTTP Request with some additional helper functions
//
class CheckerServletRequest(val request : HttpServletRequest) extends HttpServletRequestWrapper(request) {
  val URISegment : Array[String] = request.getRequestURI().split("/").filterNot(e => e == "").map(u => URLDecoder.decode(u,"UTF-8"))
  def pathToSegment(uriLevel : Int) : String = {
    "/" + URISegment.slice(0, uriLevel).reduceLeft( _ + "/" +_ )
  }

  def parsedXML : Document = request.getAttribute(PARSED_XML).asInstanceOf[Document]
  def parsedXML_= (doc : Document):Unit = request.setAttribute (PARSED_XML, doc)

  def parsedJSON : Object = request.getAttribute(PARSED_JSON)
  def parsedJSON_= (obj : Object):Unit = request.setAttribute (PARSED_JSON, obj)

  def contentError : Exception = request.getAttribute(CONTENT_ERROR).asInstanceOf[Exception]
  def contentError_= (e : Exception):Unit = request.setAttribute(CONTENT_ERROR, e)

  override def getInputStream : ServletInputStream = {
    if (parsedXML != null) {
      var transformer : Transformer = null
      val bout = new ByteArrayOutputStream()
      try {
        parsedXML.normalizeDocument
        transformer = borrowTransformer
        transformer.transform (new DOMSource(parsedXML), new StreamResult(bout))
        new ByteArrayServletInputStream(bout.toByteArray())
      } catch {
        case e : Exception => throw new IOException("Error while serializing!", e)
      } finally {
        returnTransformer(transformer)
      }
    } else if (parsedJSON != null) {
      new ByteArrayServletInputStream(parsedJSON.asInstanceOf[JSONAware].toJSONString().getBytes())
    } else {
      super.getInputStream()
    }
  }
}

//
//  An HTTP Response with some additional helper functions
//
class CheckerServletResponse(val request : HttpServletResponse) extends HttpServletResponseWrapper(request) {}
