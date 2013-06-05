package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.util.ObjectMapperPool.borrowParser
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool.returnParser

import com.rackspace.com.papi.components.checker.servlet._

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.TokenBuffer

import javax.servlet.FilterChain

class WellFormedJSON(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON is not well formed!"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var parser : ObjectMapper = null

    try {
      if (req.parsedJSON == null) {
        parser = borrowParser
        req.parsedJSON = parser.readValue(req.getInputStream(),classOf[TokenBuffer])
      }
      ret = uriLevel
    } catch {
      case e : Exception => req.contentError = e
    } finally {
      if (parser != null) returnParser(parser)
    }

    ret
  }
}
