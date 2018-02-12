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
class ValidatorWADLRaxRolesXPathTenantSuite extends BaseValidatorSuite with VaryTestSuite {
  //
  //  Configs
  //
  val raxRolesDisabled : CaseConfig = ("rax roles disabled", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesDisabledRemoveDupsJoinXPath : CaseConfig = ("rax roles disabled, remove dups, joinXPath", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = false
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = true
    c
  })

  val raxRolesEnabled : CaseConfig = ("rax roles enabled", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledRemoveDups : CaseConfig = ("rax roles enabled, remove dups", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledRemoveDupsJoinXPath : CaseConfig = ("rax roles enabled, remove dups, joinXPath", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = false
    c.removeDups = true
    c.joinXPathChecks = true
    c
  })

  val raxRolesEnabledIsTenantEnabled : CaseConfig = ("rax roles enabled, isTenant enabled", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = false
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledIsTenantEnabledRemoveDups : CaseConfig = ("rax roles enabled, isTenant enabled, remove dups", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = true
    c.joinXPathChecks = false
    c
  })

  val raxRolesEnabledIsTenantEnabledRemoveDupsJoinXPath : CaseConfig = ("rax roles enabled, isTenant enabled, remove dups, joinXPath", {
    val c = TestConfig()
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.enableRaxIsTenantExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c
  })


  //
  //  Test WADLs
  //
  val xpathTenant : TestWADL = ("XPath and URL Tenant",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{X-TENANT}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="template"/>
            <method name="POST" rax:roles="a:admin/{X-TENANT2} a:creator/{X-TENANT2}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:admin/{X-TENANT2} a:updater/{X-TENANT2}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
    </resources>
    <representation id="xmlRep" mediaType="application/xml">
        <param name="X-TENANT2" style="plain"
               path="//@tenant2" required="true"/>
    </representation>
    <representation id="jsonRep" mediaType="application/json">
        <param name="X-TENANT2" style="plain"
               path="$body?tenant2" required="true"/>
    </representation>
  </application>)

  val xpathTenantExplicit : TestWADL = ("XPath and URL Tenant (explicit isTenant)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{X-TENANT}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="template" rax:isTenant="true"/>
            <method name="POST" rax:roles="a:admin/{X-TENANT2} a:creator/{X-TENANT2}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:admin/{X-TENANT2} a:updater/{X-TENANT2}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
    </resources>
    <representation id="xmlRep" mediaType="application/xml">
        <param name="X-TENANT2" style="plain"
               path="//@tenant2" required="true"
               rax:isTenant="true"/>
    </representation>
    <representation id="jsonRep" mediaType="application/json">
        <param name="X-TENANT2" style="plain"
               path="$body?tenant2" required="true"
               rax:isTenant="true"/>
    </representation>
  </application>)

  val xpathSameNameTenant : TestWADL = ("XPath and URL Tenant (with the same name)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{X-TENANT}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="template"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
    </resources>
    <representation id="xmlRep" mediaType="application/xml">
        <param name="X-TENANT" style="plain"
               path="//@tenant2" required="true"/>
    </representation>
    <representation id="jsonRep" mediaType="application/json">
        <param name="X-TENANT" style="plain"
               path="$body?tenant2" required="true"/>
    </representation>
  </application>)

  val xpathSameNameTenantExplicit : TestWADL = ("XPath and URL Tenant (with the same name, explicit isTenant)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/{X-TENANT}/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="template" rax:isTenant="true"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="GET" rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <representation href="#xmlRep"/>
                    <representation href="#jsonRep"/>
                </request>
            </method>
            <method name="DELETE"/>
        </resource>
    </resources>
    <representation id="xmlRep" mediaType="application/xml">
        <param name="X-TENANT" style="plain"
               path="//@tenant2" required="true"
               rax:isTenant="true"/>
    </representation>
    <representation id="jsonRep" mediaType="application/json">
        <param name="X-TENANT" style="plain"
               path="$body?tenant2" required="true"
               rax:isTenant="true"/>
    </representation>
  </application>)


  //
  // Suites
  //


  //
  // These are useful for the tests below
  //
  val nonContentMethods = List("GET", "DELETE")
  val contentMethods = List("POST", "PUT")


  //
  //  These sholud always fail regardless of configs, they're a simple
  //  sanity test on validation.
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
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant2="foo"/>, true,
          Map[String, List[String]]()), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/777/resource (bad roles)") {
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant2="foo"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should fail a $m with XML if the XML is bad on /v1/777/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant3="foo"/>, true,
            Map[String, List[String]]()), response, chain), 400)
      }

      test(s"$desc : should allow a $m with JSON on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant2" : "foo" }""", true,
          Map[String, List[String]]()), response, chain)
      }

      test(s"$desc : should allow a $m with JSON on /v1/777/resource (bad roles)") {
        validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant2" : "foo" }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should fail a $m with JSON if the JSON is bad on /v1/777/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant3" : "foo" }""", true,
            Map[String, List[String]]()), response, chain), 400)
      }
    })
  }

  def happyWhenRAXRolesIsEnabled (desc : String, validator : Validator) : Unit = {
      val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "2" : ["a:creator", "foo", "a:observer"],
         "3" : ["a:updater", "bar", "biz", "a:creator"],
         "4" : ["a:observer"]
      }
    """)

    //
    // Admin tenants should be able to perform all operations. Here we
    // get a:admin because we are accessing tenant 1 on the URI.
    //
    // We are deliberately not matching anything on tenant2 to test
    // that case.
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
      test(s"$desc : should allow a $m with JSON on /v1/1/resource") {
        validator.validate(request(m,"/v1/1/resource", "application/json", """{ "tenant2" : "foo" }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

      test(s"$desc : should allow a $m with XML on /v1/1/resource") {
        validator.validate(request(m,"/v1/1/resource", "application/xml", <xml tenant2="foo"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }
    })

    //
    //  Check admin tenants here, this time we don't match the first
    //  tenant, but we do match on the 2nd tenant.  In this case
    //  nonContentMethods fail, but content methods pass.
    //
    nonContentMethods.foreach (m => {
      test(s"$desc : should fail $m on /v1/777/resource (no role match!)") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", null, "", false,
            Map(
              ROLES_HEADER->List("baz","biz","observer","notAnAdmin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }
    })

    contentMethods.foreach (m => {

      test(s"$desc : should allow a $m with XML on /v1/777/resource") {
        validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant2="1"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain)
      }

    })

    //
    //  In this case we match a:observer on the URL template. All
    //  methods should fail except for GET.
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
          validator.validate(request(m,"/v1/4/resource", "application/json", """{ "tenant2" : "foo" }""", true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      test(s"$desc : should fail a $m with XML on /v1/4/resource") {
        assertResultFailed(
          validator.validate(request(m,"/v1/4/resource", "application/xml", <xml tenant2="foo"/>, true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }
    })

    //
    // Here we show content methods failing on observer when there's
    // no match in the URL param.
    //
    //

    contentMethods.foreach (m => {
      test(s"$desc : should fail a $m with JSON on /v1/777/resource with 4 on tenant 2") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", "application/json", """{ "tenant2" : "4" }""", true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }

      test(s"$desc : should fail a $m with XML on /v1/777/resource with 4 on tenant 2") {
        assertResultFailed(
          validator.validate(request(m,"/v1/777/resource", "application/xml", <xml tenant2="4"/>, true,
            Map(
              ROLES_HEADER->List("baz","biz","b:admin","admin"),
              MAP_ROLES_HEADER->List(mapHeaderValue)
            )), response, chain), 403)
      }
    })


    //
    //  In this case we match the a:creator method in the plain
    //  parameter, and no match in the URI parameter.  We can perform
    //  a POST, but all other operations will fail.
    //
    //  (Note that failing on 777 match for nonContent methods already
    //  checked above.)
    //
    //
    test(s"$desc : should allow a POST with JSON on /v1/777/resource, when we match 2 on tenant 2") {
      validator.validate(request("POST","/v1/777/resource", "application/json", """{ "tenant2" : "2" }""", true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : should allow a POST with XML on /v1/777/resource, when we match 2 on tenant 2") {
      validator.validate(request("POST","/v1/777/resource", "application/xml", <xml tenant2="2"/>, true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : should fail a PUT with JSON on /v1/777/resource, when we match 2 on tenant 2") {
      assertResultFailed(
        validator.validate(request("PUT","/v1/777/resource", "application/json", """{ "tenant2" : "2" }""", true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain), 403)
    }

    test(s"$desc : should fail a PUT with XML on /v1/777/resource, when we match 2 on tenant 2") {
      assertResultFailed(
        validator.validate(request("PUT","/v1/777/resource", "application/xml", <xml tenant2="2"/>, true,
          Map(
            ROLES_HEADER->List("baz","biz","b:admin","admin"),
            MAP_ROLES_HEADER->List(mapHeaderValue)
          )), response, chain), 403)
    }


    //
    // Same as above, but this case we check creator and updater
    // simultaniously.
    //
    // (Note that failing on 777 match for nonContent methods already
    //  checked above)
    //
    //
    test(s"$desc : should allow a POST with JSON on /v1/777/resource, when we match 3 on tenant 2") {
      validator.validate(request("POST","/v1/777/resource", "application/json", """{ "tenant2" : "3" }""", true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : should allow a POST with XML on /v1/777/resource, when we match 3 on tenant 2") {
      validator.validate(request("POST","/v1/777/resource", "application/xml", <xml tenant2="3"/>, true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : should allow a PUT with JSON on /v1/777/resource, when we match 3 on tenant 2") {
      validator.validate(request("PUT","/v1/777/resource", "application/json", """{ "tenant2" : "3" }""", true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

    test(s"$desc : should fail a PUT with XML on /v1/777/resource, when we match 3 on tenant 2") {
      validator.validate(request("PUT","/v1/777/resource", "application/xml", <xml tenant2="3"/>, true,
        Map(
          ROLES_HEADER->List("baz","biz","b:admin","admin"),
          MAP_ROLES_HEADER->List(mapHeaderValue)
        )), response, chain)
    }

  }

  //
  //  Run testcases
  //
  val disabledXPathTestCase : TestCase = (
    List(xpathTenant, xpathTenantExplicit),                      // WADLs
    List(raxRolesDisabled, raxRolesDisabledRemoveDupsJoinXPath), //Configs
    List(sadSanity, happyWhenRaxRolesIsDisabled)                 // Suites
  )
  run (disabledXPathTestCase)

  val enabledXPathTestCase : TestCase = (
    List(xpathTenant, xpathTenantExplicit,
         xpathSameNameTenant, xpathSameNameTenantExplicit),     // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
         raxRolesEnabledRemoveDupsJoinXPath,
         raxRolesEnabledIsTenantEnabled,
         raxRolesEnabledIsTenantEnabledRemoveDups,
         raxRolesEnabledIsTenantEnabledRemoveDupsJoinXPath),    //Configs
    List(sadSanity, happyWhenRAXRolesIsEnabled)                 // Suites
  )
  run (enabledXPathTestCase)

}
