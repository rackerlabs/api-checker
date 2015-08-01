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
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results.{MismatchResult, MultiFailResult, Result}

class URIXSD(id : String, label : String, val simpleType : QName, val schema : Schema, val captureHeader : Option[String], next : Array[Step])
      extends ConnectedStep(id, label, next) {

     def this(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step]) =
       this(id, label, simpleType, schema, None, next)

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def check(req : CheckerServletRequest,
                        resp : HttpServletResponse,
                        chain : FilterChain,
                        context : StepContext) : Option[Result] = {
       var result : Option[Result] = None
       if (context.uriLevel < req.URISegment.size) {
         val v = req.URISegment(context.uriLevel)
         val error = xsd.validate(v)
         if (error != None) {
           result = Some(new MismatchResult(error.get.getMessage(), context, id))
         } else {
           val newContext = captureHeader match {
             case None => context.copy(uriLevel = context.uriLevel + 1)
             case Some(h) => context.copy(uriLevel = context.uriLevel + 1,
                                         requestHeaders = context.requestHeaders.addHeader(h, v))
           }
           val results : Array[Result] = nextStep (req, resp, chain, newContext)
           results.size match {
             case 0 =>
               result = None
             case 1 =>
               results(0).addStepId(id)
               result = Some(results(0))
             case _ =>
               result = Some(new MultiFailResult (results, id))
           }
         }
       } else {
         result = Some( new MismatchResult( mismatchMessage, context, id) )
       }
       result
     }
}

