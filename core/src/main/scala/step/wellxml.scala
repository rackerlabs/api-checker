package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.util.XMLParserPool.borrowParser
import com.rackspace.com.papi.components.checker.util.XMLParserPool.returnParser

import com.rackspace.com.papi.components.checker.servlet._

import javax.xml.parsers.DocumentBuilder

import javax.servlet.FilterChain


class WellFormedXML(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The XML is not well formed!"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var parser : DocumentBuilder = null
    try {
      if (req.parsedXML == null) {
        parser = borrowParser
        req.parsedXML = parser.parse(req.getInputStream)
      }
      ret = uriLevel
    }catch {
      case e : Exception => req.contentError = e
    }
    finally {
      if (parser != null) returnParser(parser)
    }
    ret
  }
}
