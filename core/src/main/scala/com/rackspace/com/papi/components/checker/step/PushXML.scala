/***
 *   Copyright 2017 Rackspace US, Inc.
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

import java.io.ByteArrayInputStream

import java.nio.charset.StandardCharsets

import javax.servlet.FilterChain
import javax.xml.parsers.DocumentBuilder

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{Step, PushRepresentation}
import com.rackspace.com.papi.components.checker.util.XMLParserPool.{borrowParser, returnParser}
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext

class PushXML(id : String, label : String, name : String, expression : String,
              nc : ImmutableNamespaceContext, version : Int, priority : Long,
              next : Array[Step]) extends
    PushRepresentation (id, label, name, expression, nc, version, priority, false, "XML", next) {

  override def pushRepresentation(req : CheckerServletRequest, rep : String) : Unit = {
    var parser : DocumentBuilder = null
    val capture = new ErrorCapture

    try {
      parser = borrowParser
      parser.setErrorHandler(capture)
      req.pushRepresentation(parser.parse(new ByteArrayInputStream(rep.getBytes(StandardCharsets.UTF_8.name))))
    } finally {
      if (parser != null) returnParser(parser)
    }
  }

}
