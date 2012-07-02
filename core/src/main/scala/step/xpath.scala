package com.rackspace.com.papi.components.checker.step

import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathConstants.BOOLEAN
import javax.xml.namespace.NamespaceContext

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.XPathExpressionPool._

import org.xml.sax.SAXParseException

class XPath(id : String, label : String, val expression : String, val nc : NamespaceContext, val version : Int, next : Array[Step]) extends ConnectedStep(id, label, next) {

  override val mismatchMessage : String = "The expression "+expression+" does not evaluate to true"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var xpath : XPathExpression = null

    try {
      xpath = borrowExpression (expression, nc, version)
      if (!xpath.evaluate (req.parsedXML, BOOLEAN).asInstanceOf[Boolean]) {
        req.contentError = new SAXParseException ("Expecting "+expression, null)
      } else {
        ret = uriLevel
      }
    } catch {
      case e : Exception => req.contentError = e
    } finally {
      if (xpath != null) returnExpression (expression, version, xpath)
    }

    ret
  }
}
