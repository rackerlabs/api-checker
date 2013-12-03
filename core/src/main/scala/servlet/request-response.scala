package com.rackspace.com.papi.components.checker.servlet

import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.URI
import java.io.BufferedReader
import java.io.InputStreamReader

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import org.w3c.dom.Document

import java.util.Enumeration

import com.rackspace.com.papi.components.checker.util.IdentityTransformPool._
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool

import scala.collection.JavaConversions._

//
//  Request Keys
//
object RequestAttributes {
  val PARSED_XML    = "com.rackspace.com.papi.components.checker.servlet.ParsedXML"
  val PARSED_JSON   = "com.rackspace.com.papi.components.checker.servlet.ParsedJSONTokens"
  val CONTENT_ERROR = "com.rackspace.com.papi.components.checker.servlet.ContentError"
  val CONTENT_ERROR_CODE = "com.rackspace.com.papi.components.checker.servlet.ContentErrorCode"
}

import RequestAttributes._

//
//  An HTTP Request with some additional helper functions
//
class CheckerServletRequest(val request : HttpServletRequest) extends HttpServletRequestWrapper(request) {
  val URISegment : Array[String] = (new URI(request.getRequestURI())).getPath.split("/").filterNot(e => e == "")
  def pathToSegment(uriLevel : Int) : String = {
    "/" + URISegment.slice(0, uriLevel).reduceLeft( _ + "/" +_ )
  }

  def parsedXML : Document = request.getAttribute(PARSED_XML).asInstanceOf[Document]
  def parsedXML_= (doc : Document):Unit = request.setAttribute (PARSED_XML, doc)

  def parsedJSON : JsonNode = request.getAttribute(PARSED_JSON).asInstanceOf[JsonNode]
  def parsedJSON_= (tb : JsonNode):Unit = request.setAttribute (PARSED_JSON, tb)

  def contentError : Exception = request.getAttribute(CONTENT_ERROR).asInstanceOf[Exception]
  def contentError_= (e : Exception):Unit = {
    request.setAttribute(CONTENT_ERROR, e)
    request.setAttribute(CONTENT_ERROR_CODE, 400)
  }
  def contentError(e : Exception, c : Int) : Unit = {
    request.setAttribute(CONTENT_ERROR, e)
    request.setAttribute(CONTENT_ERROR_CODE, c)
  }
  def contentErrorCode : Int = request.getAttribute(CONTENT_ERROR_CODE).asInstanceOf[Int]

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
      var om : ObjectMapper = null
      try {
        om = ObjectMapperPool.borrowParser
        new ByteArrayServletInputStream(om.writeValueAsBytes(parsedJSON))
      } finally {
        if (om != null) {
          ObjectMapperPool.returnParser(om)
        }
      }
    } else {
      super.getInputStream()
    }
  }

  override def getReader : BufferedReader = {
    if (parsedXML != null) {
      new BufferedReader(new InputStreamReader (getInputStream(), parsedXML.getInputEncoding()))
    } else if (parsedJSON != null) {
      new BufferedReader(new InputStreamReader (getInputStream(), "UTF-8"))
    }else {
      super.getReader
    }
  }
}

//
//  An HTTP Response with some additional helper functions
//
class CheckerServletResponse(val request : HttpServletResponse) extends HttpServletResponseWrapper(request) {}
