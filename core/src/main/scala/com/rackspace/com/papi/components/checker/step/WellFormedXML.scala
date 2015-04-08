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
import javax.xml.parsers.DocumentBuilder

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.XMLParserPool.{borrowParser, returnParser}


class WellFormedXML(id : String, label : String, val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The XML is not well formed!"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var ret : Option[StepContext] = None
    var parser : DocumentBuilder = null
    val capture = new ErrorCapture

    try {
      if (req.parsedXML == null) {
        parser = borrowParser
        parser.setErrorHandler(capture)
        req.parsedXML = parser.parse(req.getInputStream)
      }
      ret = Some(context)
    }catch {
      case e : Exception => req.contentError = e
                            req.contentErrorPriority = priority
    }
    finally {
      if (parser != null) returnParser(parser)
    }
    ret
  }
}
