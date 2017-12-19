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

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import com.rackspace.com.papi.components.checker.step.results.Result
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRaxRolesHeaderTenantSuite extends ValidatorWADLRaxRolesHeaderTenantBase with VaryTestSuite {
  //
  // Test WADLs
  //
  val headerTenant : TestWADL = ("Header Single Tenant",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="false"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
            </resource>
        </resource>
    </resources>
      </application>)

  val headerTenantExplicit : TestWADL = ("Header Single Tenant Explicit",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}" rax:isTenant="true">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="false"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
            </resource>
        </resource>
    </resources>
      </application>)



  val headerAtMethodTenant : TestWADL = ("Header Single Tenant in a method",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false"/>
                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
      </application>)

  val headerAtMethodTenantExplicit : TestWADL = ("Header Single Tenant in a method explicit isTenant",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false" rax:isTenant="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false" rax:isTenant="true"/>
                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false" rax:isTenant="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="false" rax:isTenant="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="false" rax:isTenant="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
    </application>)


  //
  // Suites
  //


  //
  //  These should fail regardless of configs they are simple sanity
  //  tests on the validator.
  //

  def sanity(desc : String, validator : Validator) : Unit = {
    test(s"$desc : Should fail with a 405 on patch /v1/resource") {
      assertResultFailed(validator.validate(request("PATCH","/v1/resource", null, "", false,
        Map("X-TENANT"->List("5"),
            "X-OTHER"->List("other"))), response, chain), 405)
    }

    test(s"$desc : Should fail with 404 on GET /v2/resoruce"){
      assertResultFailed(validator.validate(request("GET","/v2/resource", null, "", false,
        Map("X-TENANT"->List("5"),
            "X-OTHER"->List("other"))), response, chain), 404)
    }
  }

  def happyWhenRaxRolesIsDisabled (desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","a:observer","bar"],
         "2" : ["a:admin", "foo"],
         "3" : ["a:reviewer", "bar", "biz", "a:creator"],
         "4" : ["a:observer"],
         "5" : ["none"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      //
      //  All requests sholud succeed if appropriate headers are set,
      //  and fail if they are malformed or absent, if no roles are set.
      //

      test(s"$desc : Should succeed on $method $url when appropriate headers are set (no roles)"){
        validateHeaderRequest(validator,r, Some(List("5")), Some(List("other")), None, None)
      }

      test(s"$desc : Should fail on $method $url when the X-TENANT header is malformed (no roles)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("foo")), Some(List("other")), None, None),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should fail on $method $url when the X-OTHER header is missing (no roles)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("10")), None, None, None),
          400, List("X-OTHER"))
      }

      //
      //  All requests sholud succeed if appropriate headers are set,
      //  and fail if they are malformed or absent, if bad roles are set.
      //
      test(s"$desc : Should succeed on $method $url when appropriate headers are set (bad roles)"){
        validateHeaderRequest(validator,r, Some(List("5")), Some(List("other")), Some(List("foo","bar")), None)
      }

      test(s"$desc : Should fail on $method $url when the X-TENANT header is malformed (bad roles)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("foo")), Some(List("other")), Some(List("foo","bar")), None),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should fail on $method $url when the X-OTHER header is missing (bad roles)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("10")), None, Some(List("foo","bar")), None),
          400, List("X-OTHER"))
      }

      //
      //  All requests sholud succeed if appropriate headers are set,
      //  and fail if they are malformed or absent, if bad map-role is set
      //
      test(s"$desc : Should succeed on $method $url when appropriate headers are set (bad map role)"){
        validateHeaderRequest(validator,r, Some(List("5")), Some(List("other")), None, Some(List(mapHeaderValue)))
      }

      test(s"$desc : Should fail on $method $url when the X-TENANT header is malformed (bad map role)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("foo")), Some(List("other")), None, Some(List(mapHeaderValue))),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should fail on $method $url when the X-OTHER header is missing (bad map role)") {
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("10")), None, None, Some(List(mapHeaderValue))),
          400, List("X-OTHER"))
      }
    })
  }

  def happyWhenRaxRolesIsEnabledSingleHeader(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "2" : ["a:creator", "foo", "a:observer"],
         "3" : ["a:updater", "bar", "biz", "a:creator"],
         "4" : ["a:observer"],
         "5" : ["a:admin", "bar", "biz", "a:creator"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      //
      //  All requests should pass if headers are correctly set and
      //  there is an admin role assigned via tenant
      //
      test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 1 selected)"){
        validateHeaderRequest(validator,r, Some(List("1")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
      }

      test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 5 selected)"){
        validateHeaderRequest(validator,r, Some(List("5")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
      }

      //
      //  All requests should fail if there is a mismatch of the
      //  tenant with a 403, unless it is a GET on /v1/resource/other,
      //  because that's open to the world.
      //
      //  Same basic behavior should occur if Roles or header maps are
      //  missing.
      //
      r match {
        case r : Request if (AnyRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 7 selected)"){
            validateHeaderRequest(validator,r, Some(List("7")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (no role info)"){
            validateHeaderRequest(validator,r, Some(List("7")), Some(List("other")), None, None)
          }

        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but there's no tenant access (tenant 7 selected)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("7")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }

          test(s"$desc : Should fail on $method $url when appropriate headers are set but there's no tenant access (no role info)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("7")), Some(List("other")), None, None),
              403)
          }
      }

      //
      //  Single tenant match of observer should only succeed on
      //  observer (and Any) match
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || ObserverRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 4 selected)"){
            validateHeaderRequest(validator,r, Some(List("4")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
          }
        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but it's an observer only tenant"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("4")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }
      }

      //
      //  Single tenant match of creator / observer should only
      //  succeed on creator, observer, and anyMatch.
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || CreatorRequests.contains(r) || ObserverRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 2 selected)"){
            validateHeaderRequest(validator,r, Some(List("2")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
          }
        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but there is no match for creator or observer"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("2")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }
      }

      //
      //  Single tenant match of creator / updater should only succed
      //  on creator, updater, and anyMatch.
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || CreatorRequests.contains(r) || UpdaterRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 3 selected)"){
            validateHeaderRequest(validator,r, Some(List("3")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
          }
        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but there is no match for creator or updater"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("3")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }
      }


      //
      // All requests sholud fail if the tenant value is in the wrong
      // format, or we miss the required X-OTHER header
      //
      test(s"$desc : Should fail with a 400 on $method $url when appropriate headers are set but there's a malformed tenant (foo)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("foo")), Some(List("other")), None, None),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should fail with a 400 on $method $url when appropriate headers the correct tenant, but we miss required X-OTHER header"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("1")), None, None, None),
          400, List("X-OTHER"))
      }
    })
  }

  //
  // Run testcases
  //
  val disabledHeaderTestCase : TestCase = (
    List(headerTenant, headerTenantExplicit),           // WADLs
    List(raxRolesDisabled, raxRolesDisabledRemoveDups), // Configs
    List(sanity, happyWhenRaxRolesIsDisabled)           // Suites
  )
  run(disabledHeaderTestCase)

  val enabledHeaderSingleTestCase : TestCase = (
    List(headerTenant, headerTenantExplicit,
      headerAtMethodTenant,
      headerAtMethodTenantExplicit),                      // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),          // Configs
    List(sanity, happyWhenRaxRolesIsEnabledSingleHeader)  // Suites
  )
  run(enabledHeaderSingleTestCase)
}
