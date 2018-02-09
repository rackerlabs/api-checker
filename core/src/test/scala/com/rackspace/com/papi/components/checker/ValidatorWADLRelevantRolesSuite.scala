/** *
  * Copyright 2017 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.com.papi.components.checker.RunAssertionsHandler.ASSERT_FUNCTION
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.results.Result
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRelevantRolesSuite extends BaseValidatorSuite {
  //
  //  Configs
  //
  val baseConfig: Config = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = false
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val baseConfigWithMaskRaxRoles: Config = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  //
  // WADLs
  //
  val WADL_withRaxRoles: Elem = {
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="an:admin">
          <method name="GET" rax:roles="an:observer"/>
          <method name="POST"/>
        </resource>
        <resource path="/b" rax:roles="an:admin">
          <resource path="/a" rax:roles="#all">
            <method name="GET"/>
          </resource>
        </resource>
        <resource path="/c">
          <method name="GET"/>
        </resource>
        <resource path="/quotes">
          <resource path="/apos" rax:roles="f&apos;oo bar">
            <method name="GET"/>
          </resource>
          <resource path="/quot" rax:roles="f&quot;oo bar">
            <method name="GET"/>
          </resource>
          <resource path="/aposquot" rax:roles="f&apos;&quot;oo bar">
            <method name="GET"/>
          </resource>
        </resource>
        <resource path="/nbsp">
          <resource path="/raw" rax:roles="f oo bar">
            <method name="GET"/>
          </resource>
          <resource path="/encoded" rax:roles="f&#160;oo bar">
            <method name="GET"/>
          </resource>
       </resource>
       <resource path="/multi-line">
           <method name="GET" rax:roles="foo
                                         bar
               "/>
       </resource>
      </resources>
    </application>
  }

  //
  // Tests
  //
  Map(
    "base config with maskRaxRoles disabled" -> baseConfig,
    "base config with maskRaxRoles enabled" -> baseConfigWithMaskRaxRoles
  ) foreach { case (configDesc, config) =>
    test(s"X-RELEVANT-ROLES header should contain relevant roles using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/a", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("an:admin", "foo", "bar", "an:observer")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("an:admin", "an:observer"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES should only contain case-sensitive equal roles using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/a", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("an:ADMIN", "an:admin")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("an:admin"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should be all roles if rax:roles is set to #all using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/b/a", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("an:admin", "admin", "foo", "bar", "observer")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("an:admin", "admin", "foo", "bar", "observer"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with an apostrophe using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/quotes/apos", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("f'oo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("f'oo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with a quote using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/quotes/quot", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("f\"oo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("f\"oo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with an apostrophe and quote using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/quotes/aposquot", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("f'\"oo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("f'\"oo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with a NBSP using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/nbsp/raw", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("f oo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("f oo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with an encoded NBSP using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/nbsp/encoded", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("f oo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("f oo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role with multiline modes using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/multi-line", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("buz", "foo")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("foo"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role (bar) with multiline modes using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/multi-line", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("bar","biz")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("bar"))
      })

      validator.validate(req, response, chain)
    }

    test(s"X-RELEVANT-ROLES header should contain a matching role (foo, bar) with multiline modes using $configDesc") {
      val validator = Validator(WADL_withRaxRoles, config)
      val req = request("GET", "/multi-line", "application/xml", goodXML, parseContent = false,
        Map("X-Roles" -> List("foo","bar")))

      req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-RELEVANT-ROLES").toList == List("foo","bar"))
      })

      validator.validate(req, response, chain)
    }

  }
}
