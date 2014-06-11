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

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class URIXSD(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step])  extends ConnectedStep(id, label, next) {

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def check(req : CheckerServletRequest,
                        resp : CheckerServletResponse,
                        chain : FilterChain,
                        uriLevel : Int) : Option[Result] = {
       var result : Option[Result] = None
       if (uriLevel < req.URISegment.size) {
         val error = xsd.validate(req.URISegment(uriLevel))
         if (error != None) {
           result = Some(new MismatchResult(error.get.getMessage(), uriLevel, id))
         } else {
           val results : Array[Result] = nextStep (req, resp, chain, uriLevel + 1)
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
         result = Some( new MismatchResult( mismatchMessage, uriLevel, id) )
       }
       result
     }
}

