/** *
  * Copyright 2014 Rackspace US, Inc.
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

import javax.servlet.http.HttpServletResponse

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.com.papi.components.checker.RunAssertionsHandler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._
import scala.xml.Elem

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRAXCaptureHeaderSuite extends BaseValidatorSuite {

  val baseConfig = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.checkHeaders = true
    c
  }

  val baseWithCaptureHeaders = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val baseWithRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val baseWithJoinXPaths = {
    val c = TestConfig()
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val baseWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }


  val raxrolesWithOutCaptureHeaders = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.checkHeaders = true
    c
  }

  val raxrolesWithCaptureHeaders = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesWithRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesWithJoinXPaths = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesMaskWithOutCaptureHeaders = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.checkHeaders = true
    c
  }

  val raxrolesMaskWithCaptureHeaders = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesMaskWithRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesMaskWithJoinXPaths = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }

  val raxrolesMaskWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkElements = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c.checkHeaders = true
    c
  }


  val captureHeaderWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
    <resources>
      <resource path="path/to/resource4" rax:roles="admin">
        <method name="POST">
          <request>
            <representation mediaType="application/xml">
              <param id="foo2" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foo3" style="plain" required="true" path="/tst:e/@foo3"/>
              <param id="foo" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
            </representation>
          </request>
        </method>
        <method name="PUT" rax:roles="put:admin">
          <request>
            <representation mediaType="application/xml">
              <param id="foo2pu" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foopu" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
              <param id="foo3pu" style="plain" required="true" path="/tst:e/@foo3"/>
            </representation>
          </request>
        </method>
        <method name="PATCH">
          <request>
            <representation mediaType="application/xml">
              <param id="foopa" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
              <param id="foo2pa" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foo3pa" style="plain" required="true" path="/tst:e/@foo3"/>
            </representation>
          </request>
        </method>
      </resource>
      <resource path="path/to/resource2/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
        <resource path="{subResource2}">
          <param name="subResource2" style="template" required="true"
                 type="xs:int" rax:captureHeader="X-SUB-RESOURCE2"/>
          <method name="GET" rax:roles="subresourceRole"/>
        </resource>
      </resource>
      <resource path="path/to/resource3/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
        <resource path="{subResource2}">
          <param name="subResource2" style="template" required="true"
                 rax:captureHeader="X-SUB-RESOURCE"/>
          <method name="GET"/>
        </resource>
      </resource>
      <resource path="path/to/resource1/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
      </resource>
      <resource path="path/to/resource1">
        <method name="GET" rax:roles="headerRole">
          <request>
            <param name="MyHeader" style="header" required="true"
                   rax:captureHeader="X-DEVICE-ID"/>
            <param name="OtherHeader" style="header" required="true"
                   type="xs:int"/>
          </request>
        </method>
        <method name="POST">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="FOO"
                   rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="FAR"
                   rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="OUT"
                   rax:captureHeader="X-DEVICE-ID"/>
          </request>
        </method>
        <method name="PUT">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="FOO"
                   type="xs:string" rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="FAR"
                   type="xs:string"/>
            <param name="MyHeader" style="header" required="true" fixed="OUT"
                   type="xs:string"/>
          </request>
        </method>
        <method name="DELETE">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="1"
                   type="xs:int" rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="2"
                   type="xs:int" rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="3"
                   type="xs:int" rax:captureHeader="X-DEVICE-ID"/>
          </request>
        </method>
        <method name="PATCH">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="1"
                   type="xs:int" rax:captureHeader="X-DEVICE-ID"/>
            <param name="MyHeader" style="header" required="true" fixed="2"
                   type="xs:int"/>
            <param name="MyHeader" style="header" required="true" fixed="3"
                   type="xs:int"/>
          </request>
        </method>
      </resource>
    </resources>
  </application>

  val deviceWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                                xmlns:rax="http://docs.rackspace.com/api"
                                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
    <resources>
      <resource path="path/to/resource4" rax:roles="admin">
        <method name="POST">
          <request>
            <representation mediaType="application/xml">
              <param id="foo2" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foo3" style="plain" required="true" path="/tst:e/@foo3"/>
              <param id="foo" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
            </representation>
          </request>
        </method>
        <method name="PUT" rax:roles="put:admin">
          <request>
            <representation mediaType="application/xml">
              <param id="foo2pu" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foopu" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
              <param id="foo3pu" style="plain" required="true" path="/tst:e/@foo3"/>
            </representation>
          </request>
        </method>
        <method name="PATCH">
          <request>
            <representation mediaType="application/xml">
              <param id="foopa" style="plain" required="true" path="/tst:e/@foo"
                     rax:captureHeader="X-FOO"/>
              <param id="foo2pa" style="plain" required="true" path="/tst:e/@foo2"/>
              <param id="foo3pa" style="plain" required="true" path="/tst:e/@foo3"/>
            </representation>
          </request>
        </method>
      </resource>
      <resource path="path/to/resource2/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
        <resource path="{subResource2}">
          <param name="subResource2" style="template" required="true"
                 type="xs:int" rax:captureHeader="X-SUB-RESOURCE2"/>
          <method name="GET" rax:roles="subresourceRole"/>
        </resource>
      </resource>
      <resource path="path/to/resource3/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
        <resource path="{subResource2}">
          <param name="subResource2" style="template" required="true"
                 rax:captureHeader="X-SUB-RESOURCE"/>
          <method name="GET"/>
        </resource>
      </resource>
      <resource path="path/to/resource1/{subResource}">
        <param name="subResource" style="template" required="true"
               rax:captureHeader="X-SUB-RESOURCE"/>
        <method name="GET"/>
      </resource>
      <resource path="path/to/resource1">
        <method name="GET" rax:roles="headerRole">
          <request>
            <param name="MyHeader" style="header" required="true"
                   rax:device="true"/>
            <param name="OtherHeader" style="header" required="true"
                   type="xs:int"/>
          </request>
        </method>
        <method name="POST">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="FOO"
                   rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="FAR"
                   rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="OUT"
                   rax:device="true"/>
          </request>
        </method>
        <method name="PUT">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="FOO"
                   type="xs:string" rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="FAR"
                   type="xs:string"/>
            <param name="MyHeader" style="header" required="true" fixed="OUT"
                   type="xs:string"/>
          </request>
        </method>
        <method name="DELETE">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="1"
                   type="xs:int" rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="2"
                   type="xs:int" rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="3"
                   type="xs:int" rax:device="true"/>
          </request>
        </method>
        <method name="PATCH">
          <request>
            <param name="MyHeader" style="header" required="true" fixed="1"
                   type="xs:int" rax:device="true"/>
            <param name="MyHeader" style="header" required="true" fixed="2"
                   type="xs:int"/>
            <param name="MyHeader" style="header" required="true" fixed="3"
                   type="xs:int"/>
          </request>
        </method>
      </resource>
    </resources>
  </application>

  val captureHeaderWADLs = Map[String, Elem]("for rax:captureHeader" -> captureHeaderWADL,
    "for rax:device" -> deviceWADL)
  captureHeaderWADLs foreach { case (attr, wadl) =>
    val captureHeaderConfigs = Map[String, Config]("base config with captureHeaders " -> baseWithCaptureHeaders,
      "base config with captureHeaders and removeDups " -> baseWithRemoveDups,
      "base config with captureHeaders and joinXPath " -> baseWithJoinXPaths,
      "base config with captureHeaders and joinXPath and removeDups " -> baseWithJoinXPathsAndRemoveDups)


    for ((desc, config) <- captureHeaderConfigs) {
      val validator = Validator(wadl, config)

      test("A GET on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("Foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A GET on path/to/resource1 should fail if OtherHeader is malformed " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("two")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("OtherHeader", "two"))
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (FOO) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FOO"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (FAR) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FAR")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FAR"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (OUT) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("OUT")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("OUT"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A POST on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PUT on path/to/resource1 of FOO should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FOO"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 of FAR should *not* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FAR")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // X-DEVICE-ID should *not* be set
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set either
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 of OUT should *not* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("OUT")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // X-DEVICE-ID should *not* be set
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set either
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A DELETE on path/to/resource1 of 1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("1")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("1"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 of 2 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("2"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 of 3 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("3"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A DELETE on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PATCH on path/to/resource1 of 1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("1")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("1"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 of 2 should *NOT* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 of 3 should *NOT* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PATCH on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A GET on path/to/resource2/foo should set X-SUB-RESOURCE to foo with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/bar should set X-SUB-RESOURCE to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/bar", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/foo/2 should set X-SUB-RESOURCE to foo and X-SUB-RESOURCE2 to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("foo"))
          assert(csReq.getHeaders("X-SUB-RESOURCE2").toList == List("2"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/bar/2000 should set X-SUB-RESOURCE to bar and X-SUB-RESOURCE2 to 2000 with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/bar/2000", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))
          assert(csReq.getHeaders("X-SUB-RESOURCE2").toList == List("2000"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2 should fail with a 405 with " + desc + attr) {
        val req = request("GET", "/path/to/resource2", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("GET"))
      }

      test("A GET on path/to/resource2/foo/bar should fail with a 404 because bar is malformed with" + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/bar", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("{bar}"))
      }

      test("A GET on path/to/resource3/bar should set X-SUB-RESOURCE to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource3/bar", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource3/bar/none should set X-SUB-RESOURCE to bar and none with " + desc + attr) {
        val req = request("GET", "/path/to/resource3/bar/none", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar", "none"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource3 should fail with a 405 with " + desc + attr) {
        val req = request("GET", "/path/to/resource3", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("GET"))
      }

      test("A POST on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

    }

    val captureHeaderRaxRolesConfigsAll = Map[String, Config]("raxroles config with captureHeaders" -> raxrolesWithCaptureHeaders,
      "raxroles config with captureHeaders and removeDups" -> raxrolesWithRemoveDups,
      "raxroles config with captureHeaders and joinXPath" -> raxrolesWithJoinXPaths,
      "raxroles config with captureHeaders and joinXPath and removeDups" -> raxrolesWithJoinXPathsAndRemoveDups,
      "raxroles mask config with captureHeaders" -> raxrolesMaskWithCaptureHeaders,
      "raxroles mask config with captureHeaders and removeDups" -> raxrolesMaskWithRemoveDups,
      "raxroles mask config with captureHeaders and joinXPath" -> raxrolesMaskWithJoinXPaths,
      "raxroles mask config with captureHeaders and joinXPath and removeDups" -> raxrolesMaskWithJoinXPathsAndRemoveDups)


    for ((desc, config) <- captureHeaderRaxRolesConfigsAll) {
      val validator = Validator(wadl, config)

      test("A GET on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2"), "X-ROLES" -> List("headerRole")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("Foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2"), "X-ROLES" -> List("headerRole")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A GET on path/to/resource1 should fail if OtherHeader is malformed " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("two"), "X-ROLES" -> List("headerRole")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("OtherHeader", "two"))
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (FOO) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FOO"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (FAR) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FAR")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FAR"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should set X-DEVICE-ID with the contents of MyHeader (OUT) with " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("OUT")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("OUT"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A POST on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("POST", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PUT on path/to/resource1 of FOO should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("FOO"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 of FAR should *not* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FAR")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // X-DEVICE-ID should *not* be set
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set either
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 of OUT should *not* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("OUT")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // X-DEVICE-ID should *not* be set
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set either
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("PUT", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A DELETE on path/to/resource1 of 1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("1")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("1"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 of 2 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("2"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 of 3 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("3"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A DELETE on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A DELETE on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("DELETE", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PATCH on path/to/resource1 of 1 should set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("1")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-DEVICE-ID").toList == List("1"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 of 2 should *NOT* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("2")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 of 3 should *NOT* set X-DEVICE-ID with the contents of MyHeader with " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeader("X-DEVICE-ID") == null)

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource1 should  fail if MyHeader is missing " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A PATCH on path/to/resource1 should  fail if MyHeader is malformed " + desc + attr) {
        val req = request("PATCH", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("MyHeader"))
      }

      test("A GET on path/to/resource2/foo should set X-SUB-RESOURCE to foo with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/bar should set X-SUB-RESOURCE to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/bar", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/foo/2 should set X-SUB-RESOURCE to foo and X-SUB-RESOURCE2 to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresourceRole")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("foo"))
          assert(csReq.getHeaders("X-SUB-RESOURCE2").toList == List("2"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2/bar/2000 should set X-SUB-RESOURCE to bar and X-SUB-RESOURCE2 to 2000 with " + desc + attr) {
        val req = request("GET", "/path/to/resource2/bar/2000", "", "", false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresourceRole")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct header should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))
          assert(csReq.getHeaders("X-SUB-RESOURCE2").toList == List("2000"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource2 should fail with a 405 with " + desc + attr) {
        val req = request("GET", "/path/to/resource2", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("GET"))
      }

      test("A GET on path/to/resource2/foo/bar should fail with a 404 because bar is malformed with" + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/bar", "", "", false, Map("MyHeader" -> List("FOO"), "X-ROLES" -> List("subresourceRole")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("{bar}"))
      }

      test("A GET on path/to/resource3/bar should set X-SUB-RESOURCE to bar with " + desc + attr) {
        val req = request("GET", "/path/to/resource3/bar", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource3/bar/none should set X-SUB-RESOURCE to bar and none with " + desc + attr) {
        val req = request("GET", "/path/to/resource3/bar/none", "", "", false, Map("MyHeader" -> List("3")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-SUB-RESOURCE").toList == List("bar", "none"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-FOO") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A GET on path/to/resource3 should fail with a 405 with " + desc + attr) {
        val req = request("GET", "/path/to/resource3", "", "", false, Map("MyHeader" -> List("FOO")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("GET"))
      }

      test("A POST on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A POST on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar, put:admin) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo, put:admin) " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A PUT on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (bar) " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("bar"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource4 with appropriate XML should set X-FOO header with the value of /tst:e/@foo (foo) " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='foo' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: HttpServletResponse, res: Result) => {
          // Correct headers should be set...
          assert(csReq.getHeaders("X-FOO").toList == List("foo"))

          //  Other capture headers should *not* be set
          assert(csReq.getHeader("X-SUB-RESOURCE") == null)
          assert(csReq.getHeader("X-SUB-RESOURCE2") == null)
          assert(csReq.getHeader("X-DEVICE-ID") == null)
        })
        validator.validate(req, response, chain)
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo"))
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo2 with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo2"))
      }

      test("A PATCH on path/to/resource4 should fail with missing attribute foo3 with 400 error code " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='hop' foo2='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin")))
        assertResultFailed(validator.validate(req, response, chain), 400, List("foo3"))
      }

    }

    val captureHeaderRaxRolesConfigs = Map[String, Config]("raxroles config without captureHeaders" -> raxrolesWithOutCaptureHeaders,
      "raxroles config with captureHeaders" -> raxrolesWithCaptureHeaders,
      "raxroles config with captureHeaders and removeDups" -> raxrolesWithRemoveDups,
      "raxroles config with captureHeaders and joinXPath" -> raxrolesWithJoinXPaths,
      "raxroles config with captureHeaders and joinXPath and removeDups" -> raxrolesWithJoinXPathsAndRemoveDups)

    for ((desc, config) <- captureHeaderRaxRolesConfigs) {
      val validator = Validator(wadl, config)

      test("A GET on path/to/resource1 should fail with 403 if headerRole is not set " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2"), "X-ROLES" -> List("headerRole2")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A GET on path/to/resource1 should fail with 403 if no roles are set " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A GET on path/to/resource2/foo/2 should fail with 403 if subresourceRole is not set " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresource")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A GET on path/to/resource2/foo/2 should fail with 403 if no roles are set " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if admin role is not set " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresourceRole")))

        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if admin role is not set (put:admin) " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))

        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if the x-roles header is not set " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))

        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should fail if admin role is not set " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("Admin")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should fail if x-roles header is not set " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if admin role is not correctly set " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin2")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if admin role is not correctly set (put:admin)" + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if x-roles is not set " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 403, List("forbidden"))
      }
    }

    val captureHeaderRaxRolesMaskConfigs = Map[String, Config]("raxroles mask config without captureHeaders" -> raxrolesMaskWithOutCaptureHeaders,
      "raxroles mask config with captureHeaders" -> raxrolesMaskWithCaptureHeaders,
      "raxroles mask config with captureHeaders and removeDups" -> raxrolesMaskWithRemoveDups,
      "raxroles mask config with captureHeaders and joinXPath" -> raxrolesMaskWithJoinXPaths,
      "raxroles mask config with captureHeaders and joinXPath and removeDups" -> raxrolesMaskWithJoinXPathsAndRemoveDups)

    for ((desc, config) <- captureHeaderRaxRolesMaskConfigs) {
      val validator = Validator(wadl, config)

      test("A GET on path/to/resource1 should fail with 405 if headerRole is not set " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2"), "X-ROLES" -> List("headerRole2")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("DELETE|PATCH|POST|PUT", "Method does not match"))
      }

      test("A GET on path/to/resource1 should fail with 405 if no roles are set " + desc + attr) {
        val req = request("GET", "/path/to/resource1", "", "", false, Map("MyHeader" -> List("Foo"), "OtherHeader" -> List("2")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("DELETE|PATCH|POST|PUT", "Method does not match"))
      }

      test("A GET on path/to/resource2/foo/2 should fail with 404 if subresourceRole is not set " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresource")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A GET on path/to/resource2/foo/2 should fail with 404 if no roles are set " + desc + attr) {
        val req = request("GET", "/path/to/resource2/foo/2", "", "", false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if admin role is not set " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("subresourceRole")))

        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if admin role is not set (put:admin)" + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))

        assertResultFailed(validator.validate(req, response, chain), 405, List("PUT"))
      }

      test("A POST on path/to/resource4 with appropriate XML should fail if the x-roles header is not set " + desc + attr) {
        val req = request("POST", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))

        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should fail if admin role is not set " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("Admin")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A PUT on path/to/resource4 with appropriate XML should fail if x-roles header is not set " + desc + attr) {
        val req = request("PUT", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if admin role is not correctly set " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("admin2")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if admin role is not correctly set (put:admin)" + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3"), "X-ROLES" -> List("put:admin")))
        assertResultFailed(validator.validate(req, response, chain), 405, List("PUT"))
      }

      test("A PATCH on path/to/resource4 with appropriate XML should fail if x-roles is not set " + desc + attr) {
        val req = request("PATCH", "/path/to/resource4", "application/xml",
            <e xmlns='http://www.rackspace.com/repose/wadl/checker/step/test'
               foo='bar' foo2='hop' foo3='yes'/>,
          false, Map("MyHeader" -> List("3")))
        assertResultFailed(validator.validate(req, response, chain), 404, List("not found"))
      }
    }
  }
}
