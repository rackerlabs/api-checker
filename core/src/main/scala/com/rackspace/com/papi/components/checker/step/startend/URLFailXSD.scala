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
import javax.servlet.http.HttpServletResponse
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.XSDStringValidator
import com.rackspace.com.papi.components.checker.step.base.StepContext
import com.rackspace.com.papi.components.checker.step.results._

//
//  Like URLFail, but fails only if the current uri path is not
//  matched by any of the simple XSD types.
//
class URLFailXSD(id : String, label : String, types : Array[QName], schema : Schema, priority : Long) extends URLFail(id, label, priority) {
  //
  //  XSD validators
  //
  val validators : Array[XSDStringValidator] = types.map (t => new XSDStringValidator(t, schema, id))


  override def check(req : CheckerServletRequest,
                     resp : HttpServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = super.check (req, resp, chain, context)
    if (result != None) {
      val in = req.URISegment(context.uriLevel)
      val errors = for (validator <- validators) yield {
        val e = validator.validate(in)
        if (e == None) return None
        e.get.getMessage()
      }

      val message = errors.foldLeft(result.get.message)(_ + " "+_)
      result = Some(new URLFailResult (message, context, id, priority))
    }
    result
  }
}
