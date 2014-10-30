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
package com.rackspace.com.papi.components.checker.servlet

import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.URI
import java.io.BufferedReader
import java.io.InputStreamReader

import java.net.URISyntaxException
import java.util

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

import com.typesafe.scalalogging.slf4j.LazyLogging

import com.netaporter.uri.encoding.PercentEncoder

import com.rackspace.com.papi.components.checker.util.IdentityTransformPool._
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

//
//  Request Keys
//
object RequestAttributes {
  val PARSED_XML    = "com.rackspace.com.papi.components.checker.servlet.ParsedXML"
  val PARSED_JSON   = "com.rackspace.com.papi.components.checker.servlet.ParsedJSONTokens"
  val CONTENT_ERROR = "com.rackspace.com.papi.components.checker.servlet.ContentError"
  val CONTENT_ERROR_CODE = "com.rackspace.com.papi.components.checker.servlet.ContentErrorCode"
  val CONTENT_ERROR_PRIORITY = "com.rackspace.com.papi.components.checker.servlet.ContentErrorPriority"
}

import RequestAttributes._

object CherkerServletRequest {
  val DEFAULT_CONTENT_ERROR_CODE : Integer = 400
  val DEFAULT_URI_CHARSET : String = "ASCII"

  val uriEncoder = new PercentEncoder()
}

import CherkerServletRequest._

//
//  An HTTP Request with some additional helper functions
//
class CheckerServletRequest(val request : HttpServletRequest) extends HttpServletRequestWrapper(request) with LazyLogging {

  private val auxiliaryHeaders = new mutable.HashMap[String, mutable.Set[String]] with mutable.MultiMap[String, String]

  val parsedRequestURI : (Option[URI], Option[URISyntaxException]) = {
    try {
      (Some(new URI(request.getRequestURI())), None)
    } catch {
      case u : URISyntaxException => (None, Some(u))
    }
  }

  val URISegment : Array[String] = parsedRequestURI match {
    case (Some(u), None) => u.getPath.split("/").filterNot(e => e == "")
    case (None, Some(e)) => Array[String]()
    case (Some(u), Some(e)) => val ru = request.getRequestURI()
                               logger.warn (s"Very strange, was simultaneously able to parse the request uri: '{$ru}' and also got a syntax error. Assuming a bad URI.")
                               Array[String]()
    case (None, None) => val ru = request.getRequestURI()
                         val e = new URISyntaxException(ru, s"Unable to parse URI, don't know why???")
                         logger.error ("Very strange, unable to parse the URI, but didn't recieve a URISyntaxException, so I'm generating one anyway", e)
                         Array[String]()
  }

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
    request.setAttribute(CONTENT_ERROR_CODE, DEFAULT_CONTENT_ERROR_CODE)
  }
  def contentError(e : Exception, c : Int, p : Long = -1) : Unit = {
    request.setAttribute(CONTENT_ERROR, e)
    request.setAttribute(CONTENT_ERROR_CODE, c)
    request.setAttribute(CONTENT_ERROR_PRIORITY, p)
  }

  def contentErrorCode : Int = request.getAttribute(CONTENT_ERROR_CODE).asInstanceOf[Int]

  def contentErrorPriority : Long = request.getAttribute(CONTENT_ERROR_PRIORITY) match {
    case l : Object => l.asInstanceOf[Long]
    case null => -1
  }
  def contentErrorPriority_= (p : Long) : Unit = request.setAttribute (CONTENT_ERROR_PRIORITY, p)

  def addHeader(name: String, value: String): Unit = auxiliaryHeaders.addBinding(name, value)

  // TODO: Implement this. I am opting to skip implementing this method since there are not plans to use it at this time,
  // and the implementation is non-trivial. This method will work for headers in the wrapped request, but will always
  // return -1 for headers added via this wrapper.
  override def getDateHeader(name: String): Long = super.getDateHeader(name)

  override def getHeader(name: String): String = {
    auxiliaryHeaders.find { case (headerName, headerValues) =>
      headerName.equalsIgnoreCase(name)
    } match {
      case Some((_, headerValues)) => headerValues.headOption.orNull
      case None => super.getHeader(name)
    }
  }

  override def getHeaders(name: String): util.Enumeration[String] = {
    auxiliaryHeaders.find { case (headerName, headerValues) =>
      headerName.equalsIgnoreCase(name)
    } match {
      case Some((_, auxiliaryHeaderValues)) =>
        Option(super.getHeaders(name)) match {
          case Some(primaryHeaderValues) =>
            // Note: We store a Scala set to prevent inconsistent unions (probably due to primaryHeaderNames
            // being a Java Enumeration).
            val headerValuesSet = primaryHeaderValues.asScala.toSet
            (auxiliaryHeaderValues ++ headerValuesSet).toIterator.asJavaEnumeration
          case None => null
        }
      case None => super.getHeaders(name)
    }
  }

  override def getHeaderNames: util.Enumeration[String] = {
    Option(super.getHeaderNames) match {
      case Some(primaryHeaderNames) =>
        // Note: We store a Scala set to prevent inconsistent unions (probably due to primaryHeaderNames
        // being a Java Enumeration).
        val headerNamesSet = primaryHeaderNames.asScala.toSet
        (auxiliaryHeaders.keySet ++ headerNamesSet).toIterator.asJavaEnumeration
      case None => null
    }
  }

  override def getIntHeader(name: String): Int = {
    Option(getHeader(name)) match {
      case Some(headerValue) => headerValue.toInt
      case None => -1
    }
  }

  override def getRequestURI : String = parsedRequestURI match {
    case (Some(u), _) => request.getRequestURI()
    case _  => // Try to encode the URI if there was a syntax error.
               // Handlers may try to parse it.
               uriEncoder.encode(super.getRequestURI(), DEFAULT_URI_CHARSET)
  }

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
