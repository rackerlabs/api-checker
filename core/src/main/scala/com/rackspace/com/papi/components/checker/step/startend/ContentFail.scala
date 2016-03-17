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

//
// Content fail state
//
class ContentFail(id : String, label : String, val priority : Long) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    //  If there is a contentError in the request, return it,
    //  otherwise return NONE.
    //
    var result : Option[Result] = None

    if (req.contentError != null) {
      val msg = {
        var m = req.contentError.getMessage
        if (m == null) {
          m = req.contentError.toString
        }
        m
      }

      val prepend = if ( req.contentErrorCode == 400 ) "Bad Content: " else ""

      val actualPriority : Long = {
        if (req.contentErrorPriority != -1) {
          req.contentErrorPriority
        } else {
          priority
        }
      }

      // I'm assuming everything generated within this step is a content error
      val bcr = new BadContentResult ( prepend + msg, req.contentErrorCode, context, id, actualPriority)

      result = Some(bcr)
    }

    result
  }
}
