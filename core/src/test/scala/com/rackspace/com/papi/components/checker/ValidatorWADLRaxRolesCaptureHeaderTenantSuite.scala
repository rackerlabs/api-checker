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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml.XML

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRaxRolesCaptureHeaderTenantSuite extends BaseValidatorSuite with VaryTestSuite {
  //
  // Configs
  //
  val raxRolesDisabled : CaseConfig = ("rax roles disabled", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesDisabledRemoveDups : CaseConfig = ("rax roles disabled, remove dups", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabled : CaseConfig = ("rax roles enabled", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledRemoveDups : CaseConfig = ("rax roles enabled, remove dups", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })


  val raxRolesEnabledIsTenantEnabled : CaseConfig = ("rax roles enabled, isTenant enabled", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledIsTenantEnabledRemoveDups : CaseConfig = ("rax roles enabled, isTenant enabled, remove dups", {
    val c = TestConfig()
    c.enableCaptureHeaderExtension = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })

  //
  //  TestWADLs
  //
  val captureHeaderTenant : TestWADL = ("CaptureHeader Tenant",
    XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
        <rax:captureHeader name="X-TENANT"
                           path="
                                 (:
                                   If there's a body and it's XML look in //@tenant
                                   else look for a field named 'tenant' in JSON

                                   And if there is no body split the URI by / and
                                   look at fourth component.
                                 :)
                                 if (exists($body)) then
                                    if ($body instance of node()) then tokenize(string($body//@tenant),' ') 
                                                                  else $body('tenant')?*
                                 else tokenize($req:uri,'/')[3]
                                 "/>
     </resources>
   </application>"""))

   val captureHeaderTenantExplicit : TestWADL = ("CaptureHeader Tenant (explicit isTenant)",
     XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
       <rax:captureHeader name="X-TENANT"
                          isTenant="true"
                          path="
                                 (:
                                   If there's a body and it's XML look in //@tenant
                                   else look for a field named 'tenant' in JSON

                                   And if there is no body split the URI by / and
                                   look at fourth component.
                                 :)
                                 if (exists($body)) then
                                    if ($body instance of node()) then tokenize(string($body//@tenant),' ') 
                                                                  else $body('tenant')?*
                                 else tokenize($req:uri,'/')[3]
                                 "/>
     </resources>
   </application>"""))

   val captureHeaderTenantAtResource : TestWADL = ("CaptureHeader Tenant at resource",
     XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
             <method name="DELETE"/>
             <rax:captureHeader name="X-TENANT"
                   path="
                            (:
                              If there's a body and it's XML look in //@tenant
                              else look for a field named 'tenant' in JSON

                              And if there is no body split the URI by / and
                              look at fourth component.
                            :)
                            if (exists($body)) then
                               if ($body instance of node()) then tokenize(string($body//@tenant),' ') 
                                                             else $body('tenant')?*
                            else tokenize($req:uri,'/')[3]
                         "/>

        </resource>
     </resources>
   </application>"""))

   val captureHeaderTenantAtResourceExplicit : TestWADL = ("CaptureHeader Tenant at resource (explicit isTenant)",
     XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                </request>
            </method>
             <method name="DELETE"/>
             <rax:captureHeader name="X-TENANT"
                   isTenant="true"
                   path="
                            (:
                              If there's a body and it's XML look in //@tenant
                              else look for a field named 'tenant' in JSON

                              And if there is no body split the URI by / and
                              look at fourth component.
                            :)
                            if (exists($body)) then
                               if ($body instance of node()) then tokenize(string($body//@tenant),' ') 
                                                             else $body('tenant')?*
                            else tokenize($req:uri,'/')[3]
                         "/>

        </resource>
     </resources>
   </application>"""))

 val captureHeaderTenantAtMethod : TestWADL = ("CaptureHeader Tenant at method",
   XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml">
                     <rax:captureHeader name="X-TENANT" path="tokenize(string($body//@tenant),' ')"/>
                    </representation>
                    <representation mediaType="application/json">
                     <rax:captureHeader name="X-TENANT" path="$body('tenant')?*"/>
                    </representation>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}">
             <request>
              <rax:captureHeader name="X-TENANT" path="tokenize($req:uri,'/')[3]"/>
             </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml">
                     <rax:captureHeader name="X-TENANT" path="tokenize(string($body//@tenant),' ')"/>
                    </representation>
                    <representation mediaType="application/json">
                     <rax:captureHeader name="X-TENANT" path="$body('tenant')?*"/>
                    </representation>
                </request>
            </method>
            <method name="DELETE">
              <request>
               <rax:captureHeader name="X-TENANT" path="tokenize($req:uri,'/')[3]"/>
              </request>
            </method>
        </resource>
     </resources>
   </application>"""))

  val captureHeaderTenantAtMethodExplicit : TestWADL = ("CaptureHeader Tenant at method (isTenant explicit)",
    XML.loadString("""<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml">
                      <rax:captureHeader name="X-TENANT" path="tokenize(string($body//@tenant),' ')"
                       isTenant="true"/>
                    </representation>
                    <representation mediaType="application/json">
                      <rax:captureHeader name="X-TENANT" path="$body('tenant')?*"
                       isTenant="true"/>
                    </representation>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}">
             <request>
              <rax:captureHeader name="X-TENANT" path="tokenize($req:uri,'/')[3]"
               isTenant="true"/>
             </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation mediaType="application/xml">
                     <rax:captureHeader name="X-TENANT" path="tokenize(string($body//@tenant), ' ')"
                       isTenant="true"/>
                    </representation>
                    <representation mediaType="application/json">
                      <rax:captureHeader name="X-TENANT" path="$body('tenant')?*"
                        isTenant="true"/>
                    </representation>
                </request>
            </method>
            <method name="DELETE">
             <request>
               <rax:captureHeader name="X-TENANT" path="tokenize($req:uri,'/')[3]"
               isTenant="true"/>
             </request>
            </method>
        </resource>
     </resources>
   </application>"""))

  //
  // Suites
  //

  //
  // These are useful for the tests below
  //
  val nonContentMethods = List("GET", "DELETE")
  val contentMethods = List("POST", "PUT")

  //
  // These should alaways fail regardless of configs, they're simple
  // sanity tets on validation.
  //

  def sadSanity( desc: String, validator : Validator) : Unit = {
    test(s"$desc : Should fail with a 405 on patch /v1/0/resource") {
      assertResultFailed(validator.validate(request("PATCH","/v1/0/resource"), response, chain), 405)
    }

    test(s"$desc : Should fail with 404 on GET /v2/resoruce"){
      assertResultFailed(validator.validate(request("GET","/v2/resource"), response, chain), 404)
    }
  }

  //
  // These should give positive results only when rax:roles is disabled.
  //
  def happyWhenRaxRolesIsDisabled(desc : String, validator : Validator) : Unit = {
     val mapHeaderValue = b64Encode("""
      {
         "a" : ["a:admin","a:observer","bar"],
         "b" : ["a:admin", "foo"],
         "c" : ["a:reviewer", "bar", "biz", "a:creator"],
         "d" : ["a:observer"]
      }
    """)

    //
    // No roles or bad roles set
    //
    nonContentMethods.foreach(m => {
      test(s"$desc : should allow $m on /v1/0/resource") {
        validator.validate(request(m,"/v1/0/resource"), response, chain)
      }

      test(s"$desc : should allow $m on /v1/0/resource (bad roles)") {
        validator.validate(request(m,"/v1/0/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }
    })

    contentMethods.foreach (m => {
      test(s"$desc : should allow a $m with XML on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant="foo"/>, true,
          Map[String, List[String]]()), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/777/resource (bad roles)") {
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant="foo"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should succeed on $m with XML if the XML is bad on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant3="foo"/>, true,
          Map[String, List[String]]()), response, chain)
      }

      test(s"$desc : should allow a $m with JSON on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant" : ["foo"] }""", true,
          Map[String, List[String]]()), response, chain)
      }

      test(s"$desc : should allow a $m with JSON on /v1/777/resource (bad roles)") {
        validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant" : ["foo"] }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should succeed a $m with JSON if the JSON is bad on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant3" : ["foo"] }""", true,
          Map[String, List[String]]()), response, chain)
      }
    })
  }

  //
  // These tests should succeed in all cases when rax:roles is enabled
  //
  def happyWhenRaxRolesIsEnabled(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "2" : ["a:creator", "foo", "a:observer"],
         "3" : ["a:updater", "bar", "biz", "a:creator"],
         "4" : ["a:observer"],
         "5" : ["a:admin", "bar", "biz", "a:creator"]
      }
    """)

    //
    // Admins should be a able to perform all operations. According to
    // the WADLs we get the tenent from the URI for GET and DELETE
    // requests and from the tenant body for POST and PUT requests. So
    // we match the appropriate parameter and deliberatly mismatch the
    // other.
    //
    nonContentMethods.foreach (m => {
      test(s"$desc : should allow $m on /v1/1/resource") {
        validator.validate(request(m,"/v1/1/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","biz","observer","notAnAdmin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }
    })

    contentMethods.foreach (m => {

      //
      //  The following match 1 : admin
      //
      test(s"$desc : should allow a $m with JSON on /v1/4/resource") {
        validator.validate(request(m,"/v1/4/resource", "application/json", """{ "tenant" : ["1"] }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/4/resource") {
        validator.validate(request(m,"/v1/4/resource", "application/xml", <xml tenant="1"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      //
      //  In the following multi-match check we match the 1 : admin
      //  and 5 : admin
      //
      test(s"$desc : should allow a $m with JSON on /v1/4/resource (multi-match tenants)") {
        validator.validate(request(m,"/v1/4/resource", "application/json", """{ "tenant" : ["5", "1"] }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/4/resource (multi-match tenants)") {
        validator.validate(request(m,"/v1/4/resource", "application/xml", <xml tenant="5 1"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      //
      //  In the following multi-match check we match the 1 : admin
      //  and 3 : creator or updater
      //
      test(s"$desc : should allow a $m with JSON on /v1/4/resource (multi-match tenants, mismatch)") {
        validator.validate(request(m,"/v1/4/resource", "application/json", """{ "tenant" : ["3", "1"] }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/4/resource (multi-match tenants, mismatch)") {
        validator.validate(request(m,"/v1/4/resource", "application/xml", <xml tenant="3 1"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }
    })

    //
    // In nonContent methods, a mismatch in the URI should fail.
    //
    nonContentMethods.foreach (m => {
      test(s"$desc : should fail $m on /v1/777/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", null, "", false,
            Map(
              ROLES_HEADER->List("baz","biz","observer","notAnAdmin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }
    })

    contentMethods.foreach (m => {
      //
      //  For content methods an admin match in the URI, but observer
      //  match in the body should fail...
      //
      test(s"$desc : should fail a $m with JSON on /v1/1/resource if observer in body") {
        assertResultFailed(
          validator.validate(request(m,"/v1/1/resource", "application/json", """{ "tenant" : ["4"] }""", true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      test(s"$desc : should fail a $m with XML on /v1/1/resource if observer in body") {
        assertResultFailed(
          validator.validate(request(m,"/v1/1/resource", "application/xml", <xml tenant="4"/>, true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      //
      //  ...This should fail even if the observer match is also
      //  followed by a match which contains admin.
      //  
      //
      test(s"$desc : should fail a $m with JSON on /v1/1/resource if observer, admin in body") {
        assertResultFailed(
          validator.validate(request(m,"/v1/1/resource", "application/json", """{ "tenant" : ["1", "4"] }""", true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      test(s"$desc : should fail a $m with XML on /v1/1/resource if observer, admin in body") {
        assertResultFailed(
          validator.validate(request(m,"/v1/1/resource", "application/xml", <xml tenant="1 4"/>, true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

    })

    //
    //  Match observer in URI and body, all methods should fail except
    //  for GET.
    //
    test(s"$desc : should allow GET on /v1/4/resource") {
        validator.validate(request("GET","/v1/4/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","biz","observer","notAnAdmin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
    }

    test(s"$desc : should fail DELETE on /v1/4/resource") {
      assertResultFailed(
        validator.validate(request("DELETE","/v1/4/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","biz","observer","notAnAdmin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain), 403)
    }

    contentMethods.foreach (m => {
      test(s"$desc : should fail a $m with JSON on /v1/4/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/4/resource", "application/json", """{ "tenant" : ["4"] }""", true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      test(s"$desc : should fail a $m with XML on /v1/4/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/4/resource", "application/xml", <xml tenant="4"/>, true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }
    })

  }

  //
  // Run testcases
  //

  val disabledCaptureHeaderTestCase : TestCase = (
    List(captureHeaderTenant, captureHeaderTenantExplicit),    // WADLs
    List(raxRolesDisabled, raxRolesDisabledRemoveDups),        // Configs
    List(sadSanity, happyWhenRaxRolesIsDisabled)               // Suites
  )
  run(disabledCaptureHeaderTestCase)

  val enabledCaptureHeaderTestCase : TestCase = (
    List(captureHeaderTenant, captureHeaderTenantExplicit,
      captureHeaderTenantAtResource,
      captureHeaderTenantAtResourceExplicit,
      captureHeaderTenantAtMethod,
      captureHeaderTenantAtMethodExplicit),                    // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),               // Configs
    List(sadSanity, happyWhenRaxRolesIsEnabled)                // Suites
  )
  run(enabledCaptureHeaderTestCase)
}
