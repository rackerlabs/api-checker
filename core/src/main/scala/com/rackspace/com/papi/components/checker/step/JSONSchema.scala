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

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.exceptions.ProcessingException
import com.github.fge.jsonschema.main.JsonSchema
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}

class JSONSchema(id : String, label : String, schema : JsonSchema, val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON does not validate against the schema."

  override def checkStep(req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    try {
      schema.validate(req.parsedJSON)
      Some(context)
    } catch {
      case pe : ProcessingException => {
        val message = {
          val fmsg = formatMessage(pe.getProcessingMessage().asJson())
          if (fmsg != null) fmsg else pe.getProcessingMessage().toString()
        }

        req.contentError = new Exception(message, pe)
        req.contentErrorPriority = priority
        None
      }
      case e : Exception => {
        req.contentError = e
        req.contentErrorPriority = priority
        None
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
