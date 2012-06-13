package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.util.JSONParserPool.borrowParser
import com.rackspace.com.papi.components.checker.util.JSONParserPool.returnParser

import com.rackspace.com.papi.components.checker.servlet._

import org.json.simple.parser.JSONParser

import java.io.InputStreamReader
import javax.servlet.FilterChain

class WellFormedJSON(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON is not well formed!"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var ret = -1
    var parser : JSONParser = null

    try {
      if (req.parsedJSON == null) {
        val encoding : String = {
          var e = req.getCharacterEncoding()
          if (e == null) {
            e = "UTF-8" // According to the RFC it's UTF-8 unless if encoding is not specified
          }
          e
        }

        parser = borrowParser
        req.parsedJSON = parser.parse(new InputStreamReader (req.getInputStream(), encoding))
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
