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

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class URI(id : String, label : String, val uri : Regex, val captureHeader : Option[String], next : Array[Step]) extends ConnectedStep(id, label, next) {

  def this (id : String, label : String, uri : Regex, next : Array[Step]) =
    this(id, label, uri, None, next)

  override val mismatchMessage : String = uri.toString;

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var ret : Option[StepContext] = None
    if (context.uriLevel < req.URISegment.size) {
      val v = req.URISegment(context.uriLevel)
      v match {
        case uri() => captureHeader match {
          case None => ret= Some(context.copy(uriLevel = context.uriLevel+1))
          case Some(h) => ret = Some(context.copy(uriLevel = context.uriLevel+1,
                                     requestHeaders = context.requestHeaders.addHeader(h, v)))
        }
        case _ => ret= None
      }
    }
    ret
  }
}
