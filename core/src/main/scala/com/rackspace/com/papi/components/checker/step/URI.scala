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

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}

import com.rackspace.com.papi.components.checker.util.TenantUtil._

import scala.util.matching.Regex

class URI(id : String, label : String, name : Option[String], val uri : Regex, val captureHeader : Option[String],
          val isTenant : Boolean, next : Array[Step]) extends ConnectedStep(id, label, next) {

  def this (id : String, label : String, uri : Regex, next : Array[Step]) =
    this(id, label, None, uri, None, false, next)

  def this (id : String, label : String, uri : Regex, captureHeader : Option[String], next : Array[Step]) =
    this(id, label, None, uri, captureHeader, false, next)

  override val mismatchMessage : String = uri.toString

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    if (context.uriLevel < req.URISegment.length) {
      val v = req.URISegment(context.uriLevel)
      v match {
        case uri() =>
          val contextWithCaptureHeaders = captureHeader match {
            case None => context.copy(uriLevel = context.uriLevel+1)
            case Some(h) => context.copy(uriLevel = context.uriLevel+1,
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
          Some(contextWithTenantRoles)
        case _ => None
      }
    } else {
      None
    }
  }
}
