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
package com.rackspace.com.papi.components.checker.step.startend

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results._

import scala.util.matching.Regex

//
//  Fail with a 415 if the request content type doesn't match one of
//  the accepted types
//
class ReqTypeFail(id : String, label : String, val types : Regex, val priority : Long) extends Step(id, label) {

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    var result : Option[BadMediaTypeResult] = None
    req.getContentType() match {
      case types() => result = None
      case _ => result = Some(new BadMediaTypeResult("The content type did not match the pattern: '"+types.toString.replaceAll("\\(\\?i\\)","")+"'", context, id, priority))
    }
    result
  }
}
