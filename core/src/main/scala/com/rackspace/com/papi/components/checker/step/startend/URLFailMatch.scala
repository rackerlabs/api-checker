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

import java.util.HashMap
import javax.servlet.FilterChain
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.XSDStringValidator
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results._

import scala.util.matching.Regex

//
//  Like URLFail, but fails only if the current uri path is not matched
//  against the uri regex
//
class URLFailMatch(id : String, label : String, val uri : Regex, priority : Long) extends URLFail(id, label, priority) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = super.check (req, resp, chain, context)
    if (result != None) {
      req.URISegment(context.uriLevel) match {
        case uri() => result = None
        case _ => result = Some(new URLFailResult (result.get.message+". The URI segment does not match the pattern: '"+uri+"'", context, id, priority)) // Augment our parents result with match info
      }
    }
    result
  }
}
