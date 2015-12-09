/***
 *   Copyright 2015 Rackspace US, Inc.
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

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.HeaderUtil._


class SetHeader(id : String, label : String, val name : String, val value : String,
                next : Array[Step]) extends ConnectedStep(id, label, next) {

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    //
    //  If a header by the name exists then continue, if not, then set
    //  it to the default value.
    //
    //  Note that the context itself is searched for the header so a
    //  value set in the context is not overwritten.
    //
    if (hasHeader(context, req, name)) {
      Some(context)
    } else {
      Some(context.copy(requestHeaders = context.requestHeaders.addHeader(name, value)))
    }
  }
}
