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

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}

import scala.util.matching.Regex

class ReqType(id : String, label : String, val rtype : Regex, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = rtype.toString()

  override def checkStep(req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    req.getContentType match {
      case rtype(_,_) => Some(context)
      case _ => None
    }
  }
}
