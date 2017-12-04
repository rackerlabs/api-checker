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

import javax.servlet.FilterChain

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{Step, PushRepresentation, RepresentationException}
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool.{borrowParser, returnParser}



class PushJSON(id : String, label : String, name : String, expression : String,
               nc : ImmutableNamespaceContext, version : Int, priority : Long,
               next : Array[Step]) extends
    PushRepresentation (id, label, name, expression, nc, version, priority, true, "JSON", next) {

  override def pushRepresentation(req : CheckerServletRequest, rep : String) : Unit = {
    var parser : ObjectMapper = null
    try {
      parser = borrowParser
      val jnode = parser.readTree(rep)
      //
      //  Doc says that parser will return null on empty JSON. In
      //  theory, we should never get to this code path on empty JSON,
      //  but just in case...
      //
      if (jnode == null) {
        throw new RepresentationException(s"Expecting $repCommonName at $expression but got an empty string.")
      }
      req.pushRepresentation(jnode)
    } finally {
      if (parser != null) returnParser(parser)
    }
  }
}
