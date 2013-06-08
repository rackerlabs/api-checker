package com.rackspace.com.papi.components.checker.step

import javax.servlet.FilterChain

import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.exceptions.ProcessingException

import com.fasterxml.jackson.databind.ObjectMapper

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool

class JSONSchema(id : String, label : String, schema : JsonSchema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON does not validate against the schema."

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    var om : ObjectMapper = null
    try {
      om = ObjectMapperPool.borrowParser
      schema.validate(om.readTree(req.parsedJSON.asParser()))
      uriLevel
    } catch {
      case e : Exception => {
        req.contentError = e
        -1
      }
    } finally {
      if (om != null) ObjectMapperPool.returnParser(om)
    }
  }
}
