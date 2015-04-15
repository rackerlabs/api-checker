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
package com.rackspace.com.papi.components.checker.step.base

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given context
  //
  def check(req : CheckerServletRequest,
            resp : CheckerServletResponse,
            chain : FilterChain,
            context : StepContext) : Option[Result]

  //
  //  Checks the step at the given URI level. This method is written
  //  for compatibility reasons, there was a time where the only
  //  context was the URI level -- and for most of the existing code
  //  that's all the context that matters.
  //
  final def check(req : CheckerServletRequest,
            resp : CheckerServletResponse,
            chain : FilterChain,
            uriLevel : Int) : Option[Result] = {
    check(req, resp, chain, StepContext(uriLevel))
  }

}
