package com.rackspace.com.papi.components.checker.step

import javax.xml.transform.Templates
import javax.xml.transform.Transformer

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
    val ret = uriLevel
    var parser : DocumentBuilder = null
    var transform : Transformer = null

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

      transform.transform (source, new DOMResult(result))
      req.parsedXML = result
    } finally {
      if (transform != null) returnTransformer(templates, transform)
      if (parser != null) returnParser(parser)
    }
    ret
  }
}
