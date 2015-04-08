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
//  Like MethodFail, but fails only if the current method is not
//  matched against the uri regex
//
class MethodFailMatch(id : String, label : String, val method : Regex, priority : Long) extends MethodFail(id, label, priority) {
  private val allowHeaders = new HashMap[String, String](1)
  allowHeaders.put("Allow", method.toString.replaceAll("\\|",", "))

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    var result : Option[Result] = super.check(req, resp, chain, context)
    if (result != None) {
      req.getMethod() match {
        case method() => result = None
        case _ => result = Some(new MethodFailResult (result.get.message+". The Method does not match the pattern: '"+method+"'",
                                                      context,
                                                      id,
                                                      priority,
                                                      allowHeaders.clone.asInstanceOf[java.util.Map[String,String]])) // Augment our parents result with match info
      }
    }
    result
  }
}
