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

import java.io.PrintStream
import java.net.URI
import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import org.w3c.dom.Document

class ConsoleResultHandler(val out : PrintStream=System.out) extends ResultHandler {
  def init (validator : Validator, checker : Option[Document]) : Unit = {}

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    Console.withOut(out) {
      def valid(v : Boolean) = {
        var vout = "["
        if (v) {vout = vout+Console.GREEN+"VALID"} else {vout = vout+Console.RED+"NOPE"}
        vout+Console.RESET+"]"
      }

      printf ("%s %s %s %s\n", valid(result.valid), req.getMethod , (new URI(req.getRequestURI)).getPath, result.toString)
    }
  }
}
