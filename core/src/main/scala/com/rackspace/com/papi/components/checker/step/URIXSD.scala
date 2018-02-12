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
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results.{MismatchResult, MultiFailResult, Result}

import com.rackspace.com.papi.components.checker.util.TenantUtil._

class URIXSD(id : String, label : String, name : Option[String], val simpleType : QName, val schema : Schema, val captureHeader : Option[String],
             isTenant : Boolean, next : Array[Step])
      extends ConnectedStep(id, label, next) {

     def this(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step]) =
       this(id, label, None, simpleType, schema, None, false, next)

     def this(id : String, label : String, simpleType : QName, schema : Schema, captureHeader : Option[String], next : Array[Step]) =
       this(id, label, None, simpleType, schema, captureHeader, false, next)

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def check(req : CheckerServletRequest,
                        resp : CheckerServletResponse,
                        chain : FilterChain,
                        context : StepContext) : Option[Result] = {
       var result : Option[Result] = None
       if (context.uriLevel < req.URISegment.length) {
         val v = req.URISegment(context.uriLevel)
         val error = xsd.validate(v)
         if (error.isDefined) {
           result = Some(new MismatchResult(error.get.getMessage, context, id))
         } else {
           val contextWithCaptureHeaders = captureHeader match {
             case None => context.copy(uriLevel = context.uriLevel + 1)
             case Some(h) => context.copy(uriLevel = context.uriLevel + 1,
                                         requestHeaders = context.requestHeaders.addHeader(h, v))
           }
           val contextWithTenantRoles = isTenant match {
            case false => contextWithCaptureHeaders
            case true =>
               //
               //  Note, if isTenant is true, then name will be set.  This is
               //  enforced by validation of the checker format.
               //
               //  A valid machine should never have an empty name at this
               //  point.
               //
               require(!name.isEmpty, "If isTenant is ture then a name should be specified.")
               addTenantRoles(contextWithCaptureHeaders, req, name.get, v)
           }
           val results : Array[Result] = nextStep (req, resp, chain, contextWithTenantRoles)
           results.length match {
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
