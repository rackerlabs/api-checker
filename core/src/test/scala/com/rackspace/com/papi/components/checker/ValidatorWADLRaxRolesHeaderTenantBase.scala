/***
 *   Copyright 2018 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.RunAssertionsHandler.ASSERT_FUNCTION
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import com.rackspace.com.papi.components.checker.step.results.Result

import scala.collection.JavaConversions._

class ValidatorWADLRaxRolesHeaderTenantBase extends BaseValidatorSuite {
  //
  //  Configs
  //
  val raxRolesDisabled = ("rax roles disabled", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesDisabledRemoveDups = ("rax roles disabled, remove dups", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabled = ("rax roles enabled", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledRemoveDups = ("rax roles enabled, remove dups", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })


  val raxRolesEnabledIsTenantEnabled = ("rax roles enabled, isTenant enabled", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledIsTenantEnabledRemoveDups = ("rax roles enabled, isTenant enabled, remove dups", {
    val c = TestConfig()
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })


  //
  //  Tenant Header spicific types and func
  //
  type HeaderValue = List[String]
  type Request = (String /* Method */, String /* URI */)
  type RequestSet = Set[Request]

  val AnyRequests : RequestSet = Set(
    ("GET","/v1/resource/other")
  )
  val ObserverRequests : RequestSet = Set(
    ("GET","/v1/resource")
  )
  val CreatorRequests : RequestSet = Set(
    ("POST","/v1/resource"),
    ("POST","/v1/resource/other")
  )
  val UpdaterRequests : RequestSet = Set(
    ("PUT","/v1/resource"),
    ("PUT","/v1/resource/other")
  )
  val AdminOnlyRequests : RequestSet = Set(
    ("DELETE","/v1/resource"),
    ("DELETE","/v1/resource/other")
  )

  val AllRequests : RequestSet =
    AnyRequests ++ ObserverRequests ++
  CreatorRequests ++ UpdaterRequests ++
  AdminOnlyRequests

  def validateHeaderRequest (validator : Validator, req : Request,
    tenant : Option[HeaderValue],
    other : Option[HeaderValue],
    roles : Option[HeaderValue],
    mapRoles : Option[HeaderValue],
    expectedRevRoles : Option[HeaderValue]=None) : Result = {
    val headerMap : Map[String, List[String]] = {
      var hm = Map[String, List[String]]()
      tenant.foreach (hm += "X-TENANT" -> _)
      other.foreach (hm += "X-OTHER" -> _)
      roles.foreach (hm += ROLES_HEADER -> _)
      mapRoles.foreach (hm += MAP_ROLES_HEADER -> _)
      hm
    }

    val chkReq = request(req._1, req._2, null, "", false, headerMap)

    expectedRevRoles.foreach( rr => {
      chkReq.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        val rrSet = Set[String]() ++ csReq.getHeaders("X-RELEVANT-ROLES").toList
        rr.foreach(r => {assert(rrSet.contains(r))})
      })
    })

    validator.validate(chkReq,response, chain)
  }
}
