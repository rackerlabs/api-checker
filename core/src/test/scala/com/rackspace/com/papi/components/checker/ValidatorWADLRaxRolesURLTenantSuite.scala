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

import com.rackspace.com.papi.components.checker.RunAssertionsHandler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRaxRolesURLTenantSuite extends BaseValidatorSuite with VaryTestSuite {
  //
  //  Configs
  //
  val raxRolesDisabled : CaseConfig = ("rax roles disabled", {
    val c = TestConfig()
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c
  })

  val raxRolesDisabledRemoveDups : CaseConfig = ("rax roles disabled, remove dups", {
    val c = TestConfig()
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c
  })

  val raxRolesEnabled : CaseConfig = ("rax roles enabled", {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c
  })

  val raxRolesEnabledRemoveDups : CaseConfig = ("rax roles enabled, remove dups", {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c
  })

  val raxRolesEnabledIsTenantEnabled : CaseConfig = ("rax roles enabled, isTenant enabled", {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = false
    c
  })

  val raxRolesEnabledIsTenantEnabledRemoveDups : CaseConfig = ("rax roles enabled, isTenant enabled, remove dups", {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = true
    c
  })

  //
  //  Test WADLs
  //
  val urlTenant : TestWADL = ("URL Tenant WADL",
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{tenant}">
            <param name="tenant" style="template"/>
            <method name="POST" rax:roles="a:creator/{tenant}"/>
            <method name="GET" rax:roles="a:observer/{tenant} observer"/>
            <method name="PUT" rax:roles="a:creator/{tenant}"/>
            <method name="DELETE" rax:roles="a:creator/{tenant}"/>
        </resource>
    </resources>
      </application>)

  val urlTenantExplicit : TestWADL = ("URL Tenant WADL (explicit isTenant)",
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource" rax:roles="a:admin/{tenant}">
                 <param name="tenant" style="template" rax:isTenant="true"/>
                 <method name="POST" rax:roles="a:creator/{tenant}"/>
                 <method name="GET" rax:roles="a:observer/{tenant} observer"/>
                 <method name="PUT" rax:roles="a:creator/{tenant}"/>
                 <method name="DELETE" rax:roles="a:creator/{tenant}"/>
        </resource>
    </resources>
      </application>)

  val urlXSDTenant : TestWADL = ("URL XSD Tenant WADL",
    <application xmlns="http://wadl.dev.java.net/2009/02"
          xmlns:rax="http://docs.rackspace.com/api"
          xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource"
                  rax:roles="a:admin/{tenant}">
            <param name="tenant" style="template" type="xsd:int"/>
            <method name="POST" rax:roles="a:creator/{tenant}"/>
            <method name="GET" rax:roles="a:observer/{tenant} observer"/>
            <method name="PUT" rax:roles="a:creator/{tenant}"/>
            <method name="DELETE" rax:roles="a:creator/{tenant}"/>
        </resource>
    </resources>
      </application>)

  val urlXSDTenantExplicit : TestWADL = ("URL XSD Tenant WADL (explicit isTenant)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{tenant}/resource" rax:roles="a:admin/{tenant}">
                 <param name="tenant" style="template" type="xsd:int" rax:isTenant="true"/>
                 <method name="POST" rax:roles="a:creator/{tenant}"/>
                 <method name="GET" rax:roles="a:observer/{tenant} observer"/>
                 <method name="PUT" rax:roles="a:creator/{tenant}"/>
                 <method name="DELETE" rax:roles="a:creator/{tenant}"/>
        </resource>
    </resources>
      </application>)


  //
  //  Suites
  //

  //
  //  These should always fail, regardless of configs, they're a
  //  simple sanity test on validation.
  //
  def sadSanity(desc : String, validator : Validator) : Unit = {
    test(s"$desc : Should fail with a 405 on patch /v1/0/resource") {
      assertResultFailed(validator.validate(request("PATCH","/v1/0/resource"), response, chain), 405)
    }

    test(s"$desc : Should fail with 404 on GET /v2/resoruce"){
      assertResultFailed(validator.validate(request("GET","/v2/resource"), response, chain), 404)
    }
  }

  //
  //  Should give positive results only when rax-roles is disabled.
  //
  def happyWhenRAXRolesIsDisabled (desc : String, validator : Validator) : Unit = {
    //
    // No roles sent
    //
    test(s"$desc : should allow GET on /v1/0/resource"){
      validator.validate(request("GET","/v1/0/resource"), response, chain)
    }

    test(s"$desc : should allow POST on /v1/0/resource"){
      validator.validate(request("POST","/v1/0/resource"), response, chain)
    }

    test(s"$desc : should allow PUT on /v1/0/resource"){
      validator.validate(request("PUT","/v1/0/resource"), response, chain)
    }

    test(s"$desc : should allow DELETE on /v1/0/resource"){
      validator.validate(request("DELETE","/v1/0/resource"), response, chain)
    }

    //
    // Bad roles sent
    //
    test(s"$desc : should allow GET on /v1/777/resource"){
      validator.validate(request("GET","/v1/777/resource", null, "", false, Map(ROLES_HEADER->List("baz","biz","wooga"))), response, chain)
    }

    test(s"$desc : should allow POST on /v1/777/resource"){
      validator.validate(request("POST","/v1/777/resource", null, "", false, Map(ROLES_HEADER->List("baz","biz","wooga"))), response, chain)
    }

    test(s"$desc : should allow PUT on /v1/777/resource"){
      validator.validate(request("PUT","/v1/777/resource", null, "", false, Map(ROLES_HEADER->List("baz","biz","wooga"))), response, chain)
    }

    test(s"$desc : should allow DELETE on /v1/777/resource"){
      validator.validate(request("DELETE","/v1/777/resource", null, "", false, Map(ROLES_HEADER->List("baz","biz","wooga"))), response, chain)
    }
  }

  def happyWhenRAXRolesIsEnabled (desc : String, validator : Validator) : Unit = {
      val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","a:observer","bar"],
         "2" : ["a:admin", "foo"],
         "3" : ["a:reviewer", "bar", "biz", "a:creator"],
         "4" : ["a:observer"]
      }
    """)

    val observerTenants = List("1","2","4")

    observerTenants.foreach( t => {
      test(s"$desc : Should allow GET on /v1/$t/resource") {
        validator.validate(request("GET",s"/v1/$t/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }
    })


    test(s"$desc : Should allow GET on /v1/3/resource with observer") {
      validator.validate(request("GET","/v1/3/resource", null, "", false,
        Map(
          ROLES_HEADER->List("baz","observer","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : Should reject GET on /v1/3/resource") {
      assertResultFailed(
        validator.validate(request("GET","/v1/3/resource", null, "", false,
          Map(
            ROLES_HEADER->List("baz","a:observer","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain), 403)
    }

    val creatorMethods = List("POST","PUT","DELETE")
    val creatorTenants = List("1","2","3")

    creatorMethods.foreach (m => {
      creatorTenants.foreach (t => {
        test(s"$desc : Should allow $m on /v1/$t/resource") {
          validator.validate(request(m,s"/v1/$t/resource", null, "", false,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain)
        }
      })

      test(s"$desc : Should reject $m on /v1/4/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/4/resource", null, "", false,
            Map(
              ROLES_HEADER->List("baz","a:observer","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

    })
  }

  //
  //  Run testcases
  //
  val disabledURLTestCase : TestCase = (
    List(urlTenant, urlTenantExplicit,
         urlXSDTenant, urlXSDTenantExplicit),           // WADLs
    List(raxRolesDisabled, raxRolesDisabledRemoveDups), // Configs
    List(sadSanity, happyWhenRAXRolesIsDisabled)        // Suites
  )
  run(disabledURLTestCase)

  val enabledURLTestCase : TestCase = (
    List(urlTenant, urlTenantExplicit,
         urlXSDTenant, urlXSDTenantExplicit),           // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
         raxRolesEnabledIsTenantEnabled,
         raxRolesEnabledIsTenantEnabledRemoveDups),     // Configs
    List(sadSanity, happyWhenRAXRolesIsEnabled)         // Suites
  )
  run(enabledURLTestCase)

}
