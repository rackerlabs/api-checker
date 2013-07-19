package com.rackspace.com.papi.components.checker.step

import javax.servlet.FilterChain

import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.exceptions.ProcessingException

import com.fasterxml.jackson.databind.JsonNode

import com.rackspace.com.papi.components.checker.servlet._

class JSONSchema(id : String, label : String, schema : JsonSchema, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON does not validate against the schema."

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    try {
      schema.validate(req.parsedJSON)
      uriLevel
    } catch {
      case pe : ProcessingException => {
        val message = {
          val fmsg = formatMessage(pe.getProcessingMessage().asJson())
          if (fmsg != null) fmsg else pe.getProcessingMessage().toString()
        }

        req.contentError = new ProcessingException(message, pe)
        -1
      }
      case e : Exception => {
        req.contentError = e
        -1
      }
    }
  }

  private def formatMessage (jpmsg : JsonNode) : String = {
    val pointer = getPointer(jpmsg)

    if (pointer != null) {
      "In "+pointer+", "+getMessage(jpmsg)
    } else {
      getMessage(jpmsg)
    }
  }

  private def nullEmpty (in : String) : String = {
    if (in == null || in == "") {
      null
    } else {
      in
    }
  }

  private def getMessage (jpmsg : JsonNode) : String = {
    val jms = jpmsg.findValue("message")
    if (jms != null) nullEmpty(jms.asText()) else null
  }

  private def getPointer(jpmsg : JsonNode) : String = {
    val inst = jpmsg.findValue("instance")
    if (inst != null) {
      val p = inst.findValue("pointer")
      if (p != null) {
        nullEmpty(p.asText())
      } else {
        null
      }
    } else {
      null
    }
  }
}
