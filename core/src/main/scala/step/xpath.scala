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

import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathConstants.BOOLEAN
import javax.xml.namespace.NamespaceContext

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.XPathExpressionPool._

import org.xml.sax.SAXParseException

class XPath(id : String, label : String, val expression : String, val message : Option[String],
            val code : Option[Int], val nc : NamespaceContext, val version : Int,
            val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) {

  override val mismatchMessage : String = {
    if (message == None) {
      "Expecting "+expression
    } else {
      message.get
    }
  }

  val mismatchCode : Int = {
    if (code == None) {
      400
    } else {
      code.get
    }
  }

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var xpath : XPathExpression = null

    try {
      xpath = borrowExpression (expression, nc, version)
      if (!xpath.evaluate (req.parsedXML, BOOLEAN).asInstanceOf[Boolean]) {
        req.contentError(new SAXParseException (mismatchMessage, null), mismatchCode, priority)
      } else {
        ret = uriLevel
      }
    } catch {
      case e : Exception => req.contentError = e
                            req.contentErrorPriority = priority
    } finally {
      if (xpath != null) returnExpression (expression, nc, version, xpath)
    }

    ret
  }
}
