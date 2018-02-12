/***
 *   Copyright 2016 Rackspace US, Inc.
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
import javax.xml.namespace.QName
import javax.xml.namespace.NamespaceContext
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.XPathExpressionPool._
import com.rackspace.com.papi.components.checker.util.XMLParserPool._
import com.rackspace.com.papi.components.checker.util.VarXPathExpression
import org.xml.sax.SAXParseException

import com.rackspace.com.papi.components.checker.util.TenantUtil._

import com.fasterxml.jackson.core.JsonParser.NumberType._
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.JsonNodeType._

object JSONXPath {
  private val inDoc = {
    val parser = borrowParser
    try {
      parser.newDocument
    } finally {
      returnParser(parser)
    }
  }
}

import JSONXPath._

class JSONXPath(id : String, label : String, val name : Option[String], val expression : String, val message : Option[String],
            val code : Option[Int], val nc : NamespaceContext, val version : Int,
            val captureHeader : Option[String], val isTenant : Boolean, val priority : Long,
            next : Array[Step]) extends ConnectedStep(id, label, next) {

  def this(id : String, label : String,  expression : String,  message : Option[String],
           code : Option[Int],  nc : NamespaceContext,  version : Int,
           priority : Long, next : Array[Step]) =
    this (id, label, None, expression, message, code, nc, version, None, false, priority, next)

  def this(id : String, label : String,  expression : String,  message : Option[String],
           code : Option[Int],  nc : NamespaceContext,  version : Int,
           captureHeader : Option[String],  priority : Long, next : Array[Step]) =
    this (id, label, None, expression, message, code, nc, version, captureHeader, false, priority, next)

  override val mismatchMessage : String = message.getOrElse (s"Expecting $expression")

  val mismatchCode : Int = code.getOrElse(400)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var xpath : VarXPathExpression = null

    val sjson = req.parsedJSONSequence
    val vars = Map(new QName("_") -> sjson,
                   new QName("body") -> sjson)

    try {
      xpath = borrowExpression (expression, nc, version).asInstanceOf[VarXPathExpression]
      if (!xpath.evaluate (inDoc, XPathConstants.BOOLEAN, vars).asInstanceOf[Boolean]) {
        req.contentError(new SAXParseException (mismatchMessage, null), mismatchCode, priority)
        None
      } else {
        val value = xpath.evaluate (inDoc, XPathConstants.STRING, vars).asInstanceOf[String]
        val contextWithCaptureHeaders = captureHeader match {
          case None => context
          case Some(h) => context.copy (requestHeaders = context.requestHeaders.addHeader(h, value))
        }
        val contextWithTenantRoles = isTenant match {
          case false => contextWithCaptureHeaders
          case true =>
            //
            //  Note, if isTenant is true, then name will be set.  This is
            //  enforced by validation of the checker format.
            //
            //  A valid machine should never have an empty name at this
            //  point.
            //
            require(!name.isEmpty, "If isTenant is ture then a name should be specified.")
            addTenantRoles(contextWithCaptureHeaders, req, name.get, value)
        }
        Some(contextWithTenantRoles)
      }
    } catch {
      case e : Exception =>
        req.contentError(new SAXParseException(mismatchMessage+" : "+e.getMessage, null, e), mismatchCode, priority)
        None
    } finally {
      if (xpath != null) returnExpression (expression, nc, version, xpath)
    }
  }
}
