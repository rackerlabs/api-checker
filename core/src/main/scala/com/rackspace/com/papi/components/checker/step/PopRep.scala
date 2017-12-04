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

import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}


class PopRep(id : String, label : String, next : Array[Step])
    extends ConnectedStep(id, label, next) {

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    //
    //  We simply pop the representation in this step.  No questions
    //  asked.  If the repstack is empty a ValidatorException will
    //  eventually be thrown.
    //
    req.popRepresentation
    Some(context)
  }
}
