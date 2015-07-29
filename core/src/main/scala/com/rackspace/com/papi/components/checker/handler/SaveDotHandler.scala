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
package com.rackspace.com.papi.components.checker.handler

import java.io.File
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder
import org.w3c.dom.Document

class SaveDotHandler(val out : File, val ignoreSinks : Boolean, nfaMode : Boolean) extends ResultHandler {

  val dotBuilder = new WADLDotBuilder()

  def init (validator : Validator, checker : Option[Document]) : Unit = {
    if (checker != None) {
      dotBuilder.buildFromChecker (new DOMSource(checker.get), new StreamResult (out), ignoreSinks, nfaMode)
    }
  }
  def handle (req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, result : Result)  : Unit = {}
}
