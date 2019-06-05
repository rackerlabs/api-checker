/***
 *   Copyright 2017 Rackspace US, Inc.
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
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.collection.JavaConversions._

import scala.xml.Elem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRAXCaptureHeaderElementSuite extends BaseValidatorSuite {
  ///
  ///  Configs
  ///
  val baseConfig = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = false
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithCaptureHeader = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseWithCaptureHeaderRemoveDups = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }


  val baseWithCaptureHeaderAssert = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = true
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseWithCaptureHeaderAssertRemoveDups = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = true
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }


  val baseWithCaptureHeaderParamDefaults = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = false
    c.setParamDefaults = true
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseWithCaptureHeaderParamDefaultsRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableAssertExtension = false
    c.setParamDefaults = true
    c.checkHeaders = true
    c.enableCaptureHeaderExtension = true
    c
  }


  val baseCaptureHeaderWithPlainParams = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseCaptureHeaderWithJoinXPaths = {
    val c = TestConfig()
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseCaptureHeaderWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c
  }


//  RaxRoles Configs

  val baseWithPlainParamsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }


  val baseWithRemoveDupsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }


//  RaxRoles Configs Masked

  val baseWithPlainParamsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }


  val baseWithRemoveDupsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableAssertExtension = false
    c.setParamDefaults = false
    c.enableCaptureHeaderExtension = true
    c.checkElements = true
    c
  }


  val WADL_withCaptureHeaders = <application xmlns="http://wadl.dev.java.net/2009/02"
                                             xmlns:rax="http://docs.rackspace.com/api"
                                             xmlns:req="http://www.rackspace.com/repose/wadl/checker/request"
                                             xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                             xmlns:tst="test.org"
                                             xmlns:tst2="http://www.rackspace.com/repose/wadl/checker/step/test">
  <resources base="https://test.api.openstack.com">
    <resource path="/a" rax:roles="user">
      <method name="PUT">
        <request>
          <representation mediaType="application/xml" element="tst:some_xml">
            <param name="test" style="plain" path="tst:some_xml/tst:an_element/tst:another_element" required="true"/>
            <rax:captureHeader name="X-ATT-ATTRIB" path="tst:some_xml/@att" />
            <rax:captureHeader name="X-AN-ELEM" path="local-name($body/tst:some_xml/tst:an_element)" />
            <rax:assert test="'foo!' = req:headers('X-AUTH', true())"/>
          </representation>
          <representation mediaType="application/json"/>
          <representation mediaType="text/x-yaml">
            <rax:captureHeader name="X-FALSE" path="false()" />
          </representation>
          <!-- assertion should be placed in all representations -->
          <representation>
            <rax:captureHeader name="X-BODY-EMPTY" path="empty($body)" />
            <rax:captureHeader name="X-BODY-EMPTY2" path="empty($_)" />
          </representation>
        </request>
      </method>
      <method name="POST">
        <request>
          <representation mediaType="application/xml" element="tst2:a">
            <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="expecting an id attribute"/>
            <!-- this assertion applies only to post /a when the representation is xml -->
            <rax:captureHeader name="X-X-METHOD" path="$req:method" />
            <rax:captureHeader name="X-X-URI" path="$req:uri='/a'"/>
            <rax:captureHeader name="X-X-ROOT" path="name(/tst2:a)"/>
            <rax:captureHeader name="X-X-ROOT2" path="local-name($_/tst2:a)"/>
          </representation>
          <representation mediaType="application/json">
            <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="need a first name" rax:code="403"/>
          </representation>
        </request>
      </method>
      <resource path="b">
        <method name="GET"/>
        <method name="DELETE" rax:roles="admin administrator">
          <request>
            <param name="x-auth" style="header" type="xs:string" default="baz!" required="true" repeating="true"/>
            <!-- should be treated as a request assertion -->
            <representation>
              <rax:captureHeader name="X-X-METHOD" path="$req:method"/>
              <rax:captureHeader name="X-X-AUTH" path="req:header('x-auth')"/>
            </representation>
          </request>
        </method>
        <method name="POST">
          <request>
            <representation mediaType="application/xml">
                                 </representation>
            <representation mediaType="application/json">
              <!-- this assertion applies only to post /a/b if the representation is json -->
              <rax:captureHeader name="X-X-URI-JSON" path="$req:uri" />
              <rax:captureHeader name="X-X-STUFF-STRING" path="$_?stuff?string" />
            </representation>
            <!-- this assertion applies only to post /a/b request regardless of the representation-->
            <rax:captureHeader name="X-X-URI" path="$req:uri" />
          </request>
        </method>
      </resource>
      <resource path="z">
        <method name="PATCH">
          <request>
            <representation mediaType="application/xml" element="tst2:a">
              <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="expecting an id attribute"/>
              <rax:captureHeader name="X-X-METHOD-XML" path="$req:method" />
            </representation>
          </request>
        </method>
        <method name="PATCH" rax:roles="#all">
          <request>
            <representation mediaType="application/json" rax:roles="#all">
              <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="need a first name" rax:code="403"/>
            </representation>
          </request>
        </method>
      </resource>
      <!-- this assertion applies to all requests in the resource /a -->
      <rax:captureHeader name="X-X-URI-LEVEL" path="$req:uriLevel" />
      <rax:captureHeader name="X-HEADERS-START-WITH-A" path="some $h in $req:headerNames satisfies starts-with($h, 'a')" />
      <!-- this assertion applies to all requests in the resource /a and all subresources of a /a/b for example-->
      <rax:captureHeader name="X-HEADERS-START-WITH-B" path="some $h in $req:headerNames satisfies starts-with($h,'b')"  applyToChildren="true"/>
    </resource>
    <!-- this assertion applies to all requests in the wadl -->
    <rax:captureHeader name="ALL-X-AUTH-HEADERS" path="req:headers('x-auth', true())"/>
    </resources>
  </application>


  val WADL_withCaptureHeaders2 = <application xmlns="http://wadl.dev.java.net/2009/02"
                                             xmlns:rax="http://docs.rackspace.com/api"
                                             xmlns:req="http://www.rackspace.com/repose/wadl/checker/request"
                                             xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                             xmlns:tst="test.org"
                                             xmlns:tst2="http://www.rackspace.com/repose/wadl/checker/step/test">
  <resources base="https://test.api.openstack.com">
    <resource path="/a" rax:roles="user">
      <method name="PUT">
        <request>
          <representation mediaType="application/xml" element="tst:some_xml">
            <param name="test" style="plain" path="tst:some_xml/tst:an_element/tst:another_element" required="true"/>
            <rax:captureHeader name="X-ATT-ATTRIB" path="tst:some_xml/@att" />
            <rax:captureHeader name="X-AN-ELEM" path="local-name($req:body/tst:some_xml/tst:an_element)" />
            <rax:assert test="'foo!' = req:headers('X-AUTH', true())"/>
          </representation>
          <representation mediaType="application/json"/>
          <representation mediaType="text/x-yaml">
            <rax:captureHeader name="X-FALSE" path="false()" />
          </representation>
          <!-- assertion should be placed in all representations -->
          <representation>
            <rax:captureHeader name="X-BODY-EMPTY" path="empty($req:body)" />
            <rax:captureHeader name="X-BODY-EMPTY2" path="empty($req:_)" />
          </representation>
        </request>
      </method>
      <method name="POST">
        <request>
          <representation mediaType="application/xml" element="tst2:a">
            <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="expecting an id attribute"/>
            <!-- this assertion applies only to post /a when the representation is xml -->
            <rax:captureHeader name="X-X-METHOD" path="$req:method" />
            <rax:captureHeader name="X-X-URI" path="$req:uri='/a'"/>
            <rax:captureHeader name="X-X-ROOT" path="name(/tst2:a)"/>
            <rax:captureHeader name="X-X-ROOT2" path="local-name($req:_/tst2:a)"/>
          </representation>
          <representation mediaType="application/json">
            <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="need a first name" rax:code="403"/>
          </representation>
        </request>
      </method>
      <resource path="b">
        <method name="GET"/>
        <method name="DELETE" rax:roles="admin administrator">
          <request>
            <param name="x-auth" style="header" type="xs:string" default="baz!" required="true" repeating="true"/>
            <!-- should be treated as a request assertion -->
            <representation>
              <rax:captureHeader name="X-X-METHOD" path="$req:method"/>
              <rax:captureHeader name="X-X-AUTH" path="req:header('x-auth')"/>
            </representation>
          </request>
        </method>
        <method name="POST">
          <request>
            <representation mediaType="application/xml">
                                 </representation>
            <representation mediaType="application/json">
              <!-- this assertion applies only to post /a/b if the representation is json -->
              <rax:captureHeader name="X-X-URI-JSON" path="$req:uri" />
              <rax:captureHeader name="X-X-STUFF-STRING" path="$req:_?stuff?string" />
            </representation>
            <!-- this assertion applies only to post /a/b request regardless of the representation-->
            <rax:captureHeader name="X-X-URI" path="$req:uri" />
          </request>
        </method>
      </resource>
      <resource path="z">
        <method name="PATCH">
          <request>
            <representation mediaType="application/xml" element="tst2:a">
              <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="expecting an id attribute"/>
              <rax:captureHeader name="X-X-METHOD-XML" path="$req:method" />
            </representation>
          </request>
        </method>
        <method name="PATCH" rax:roles="#all">
          <request>
            <representation mediaType="application/json" rax:roles="#all">
              <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="need a first name" rax:code="403"/>

            </representation>
          </request>
        </method>
      </resource>
      <!-- this assertion applies to all requests in the resource /a -->
      <rax:captureHeader name="X-X-URI-LEVEL" path="$req:uriLevel" />
      <rax:captureHeader name="X-HEADERS-START-WITH-A" path="some $h in $req:headerNames satisfies starts-with($h, 'a')" />
      <!-- this assertion applies to all requests in the resource /a and all subresources of a /a/b for example-->
      <rax:captureHeader name="X-HEADERS-START-WITH-B" path="some $h in $req:headerNames satisfies starts-with($h,'b')"  applyToChildren="true"/>
    </resource>
    <!-- this assertion applies to all requests in the wadl -->
    <rax:captureHeader name="ALL-X-AUTH-HEADERS" path="req:headers('x-auth', true())"/>
    </resources>
  </application>



  //
  // Config combinations
  //
  val captureHeaderDisabledConfigs = Map[String, Config]("base config with captureHeaders disabled"->baseConfig)

  val captureHeaderEnabledConfigs  = Map[String, Config]("Config with captureHeaders enabled"->baseWithCaptureHeader,
                                                  "Config with captureHeaders and remove dups"->baseWithCaptureHeaderRemoveDups)

  val captureHeaderAssertEnabledConfigs  = Map[String, Config]("Config with captureHeaders and Assert enabled"->baseWithCaptureHeaderAssert,
                                                       "Config with captureHeaders and Assert and remove dups"->baseWithCaptureHeaderAssertRemoveDups)


  val captureHeaderEnabledParamDefaultConfigs = Map[String, Config]("Config with captureHeaders enabled, param defaults"->baseWithCaptureHeaderParamDefaults,
                                                             "Config with captureHeaders enabled, param defaults and remove dups"->baseWithCaptureHeaderParamDefaultsRemoveDups)

  val captureHeaderEnabledPlainParamConfigs = Map[String, Config]("Config with captureHeaders enabled, plain params"->baseCaptureHeaderWithPlainParams,
                                                           "Config with captureHeaders enabled, plain params join XPath"->baseCaptureHeaderWithJoinXPaths,
                                                           "Config with captureHeaders enabled, plain params join XPath, remove dups"->baseCaptureHeaderWithJoinXPathsAndRemoveDups)

  val captureHeaderEnabledPlainRaxRoles = Map[String, Config]("Config with captureHeaders enabled, plain params, rax roles"->baseWithPlainParamsRaxRoles,
                                                       "Config with captureHeaders enabled, plain params, rax roles, remove dups"->baseWithRemoveDupsRaxRoles,
                                                       "Config with captureHeaders enabled, plain params, rax roles, join xpath"->baseWithJoinXPathsRaxRoles,
                                                       "Config with captureHeaders enabled, plain params, rax roles, remove dups, join xpath"->baseWithJoinXPathsAndRemoveDupsRaxRoles)

  val captureHeaderEnabledPlainRaxRolesMask = Map[String, Config]("Config with captureHeaders enabled, plain params, rax roles masked"->baseWithPlainParamsRaxRolesMask,
                                                           "Config with captureHeaders enabled, plain params, rax roles masked, remove dups"->baseWithRemoveDupsRaxRolesMask,
                                                           "Config with captureHeaders enabled, plain params, rax roles masked, join xpath"->baseWithJoinXPathsRaxRolesMask,
                                                           "Config with captureHeaders enabled, plain params, rax roles masked, remove dups, join xpath"->baseWithJoinXPathsAndRemoveDupsRaxRolesMask)


  //
  // WADL combinations
  //
  val captureHeaderWADLs = Map[String, Elem]("WADL with $body captureHeaderions"->WADL_withCaptureHeaders,
                                             "WADL with $req:body captureHeaderions"->WADL_withCaptureHeaders2)

  //
  // Assertions!
  //

  def happyPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should validate with goodXML on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }

    test (s"A PUT on /a should validate with goodJSON on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json",goodJSON, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }


     test (s"A POST on /a should validate with goodXML on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml",goodXML_XSD2, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }

    test (s"A POST on /a should validate with goodJSON on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json",goodJSON_Schema1, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }

    test (s"A GET on /a/b should validate on $wadlDesc with $configDesc") {
      validator.validate(request("GET", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A DELETE on /a/b should validate on $wadlDesc with $configDesc") {
      validator.validate(request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user", "Administrator"))), response, chain)
    }

    test (s"A POST on /a/b should validate with good XML on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A POST on /a/b should validate with good JSON on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }
  }

  def happyWhenCaptureHeadersAreDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should validate but capture headers sholud not be set with goodXML on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "b"->List("ababa"),
                          "X-Auth"->List("foo!"),
                          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-ATT-ATTRIB").hasMoreElements)
        assert(!csReq.getHeaders("X-AN-ELEM").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
        assert(!csReq.getHeaders("X-BODY-EMPTY").hasMoreElements)
        assert(!csReq.getHeaders("X-BODY-EMPTY2").hasMoreElements)
        }))
      validator.validate(req, response, chain)
    }

    test (s"A PUT on /a should validate but capture headers sholud not be set with goodJSON on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/json",goodJSON, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-FALSE").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
        assert(!csReq.getHeaders("X-BODY-EMPTY").hasMoreElements)
        assert(!csReq.getHeaders("X-BODY-EMPTY2").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a should validate but capture headers sholud not be set with goodXML on $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/xml",goodXML_XSD2, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-METHOD").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT2").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
     }

    test (s"A POST on /a should validate but capture header should not be set with goodJSON on $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/json",goodJSON_Schema1, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
     }

    test (s"A GET on /a/b should validate but capture header should not be set on $wadlDesc with $configDesc") {
      val req = request("GET", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A DELETE on /a/b should validate but capture header sholud not be set on $wadlDesc with $configDesc") {
      val req = request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user", "Administrator")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-METHOD").hasMoreElements)
        assert(!csReq.getHeaders("X-X-AUTH").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a/b should validate with good XML but capture header should not be set on $wadlDesc with $configDesc") {
      val req = request("POST", "/a/b", "application/xml", goodXML, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-URI").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a/b should validate with good JSON but capture header sholud not be set on $wadlDesc with $configDesc") {
      val req = request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-URI-JSON").hasMoreElements)
        assert(!csReq.getHeaders("X-X-STUFF-STRING").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PATCH on /a/z should validate with good XML but capture header should not be set on $wadlDesc with $configDesc") {
      val req = request("PATCH", "/a/z", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-X-METHOD-XML").hasMoreElements)
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PATCH on /a/z should validate with good JSON but capture header sholud not be set on $wadlDesc with $configDesc") {
      val req = request("PATCH", "/a/z", "application/json", goodJSON, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-B").hasMoreElements)
        assert(!csReq.getHeaders("ALL-X-AUTH-HEADERS").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }
  }


  //
  //  We ensure that capture headers are set correctly here
  //
  def happyWhenCaptureHeadersAreEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should validate capture headers sholud be set with goodXML on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "c"->List("ababa"),
                          "X-Auth"->List("foo!"),
                          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-ATT-ATTRIB").toList == List("1"))
        assert(csReq.getHeaders("X-AN-ELEM").toList == List("an_element"))
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("false"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("false"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PUT on /a should validate capture headers sholud be set with goodJSON on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/json",goodJSON, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("false"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("false"))
        assert(!csReq.getHeaders("X-FALSE").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PUT on /a should validate capture headers sholud be set with yaml on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "text/x-yaml","""
         repose : "Loves all the YAML!"
      """, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("true"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("true"))
        assert(csReq.getHeaders("X-FALSE").toList == List("false"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a should validate capture headers sholud be set with goodXML on $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/xml",goodXML_XSD2, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("POST"))
        assert(csReq.getHeaders("X-X-URI").toList == List("true"))
        assert(csReq.getHeaders("X-X-ROOT").toList == List("tst:a"))
        assert(csReq.getHeaders("X-X-ROOT2").toList == List("a"))
      }))

      validator.validate(req, response, chain)
     }

    test (s"A POST on /a should validate capture header should be set with goodJSON on $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/json",goodJSON_Schema1, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(!csReq.getHeaders("X-X-METHOD").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT2").hasMoreElements)
      }))

      validator.validate(req, response, chain)
     }

    test (s"A GET on /a/b should validate capture header should be set on $wadlDesc with $configDesc") {
      val req = request("GET", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("abba"),
                                   "b"->List("ababa"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A DELETE on /a/b validate but capture header sholud be set on $wadlDesc with $configDesc") {
      val req = request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "c"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user", "Administrator")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("DELETE"))
        assert(csReq.getHeaders("X-X-AUTH").toList == List("foo!"))
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a/b should validate with good XML capture header should be set on $wadlDesc with $configDesc") {
      val req = request("POST", "/a/b", "application/xml", goodXML, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI").toList == List("/a/b"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(!csReq.getHeaders("X-X-URI-JSON").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a/b should validate with good JSON capture header sholud be set on $wadlDesc with $configDesc") {
      val req = request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-JSON").toList == List("/a/b"))
        assert(csReq.getHeaders("X-X-URI").toList == List("/a/b"))
        assert(csReq.getHeaders("X-X-STUFF-STRING").toList == List("A String"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PATCH on /a/z should validate with good XML capture header should be set on $wadlDesc with $configDesc") {
      val req = request("PATCH", "/a/z", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-METHOD-XML").toList == List("PATCH"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A PATCH on /a/z should validate with good JSON capture header sholud be set on $wadlDesc with $configDesc") {
      val req = request("PATCH", "/a/z", "application/json", goodJSON, false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "b"->List("Dbaba"),
                                   "X-Auth"->List("foo!"),
                                   "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(!csReq.getHeaders("X-X-METHOD-XML").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }
  }

  //
  //  These are just sanity checks against the WADL, they should never validate
  //
  def happySadPaths (validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"Plain text PUT should fail on /a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT","/a","plain/text","hello!", false,
                                                    Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         415, List("content type","application/xml","application/json","text/x\\-yaml"))
    }

    test (s"A PATCH on /a should fail with a 405 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PATCH", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         405, List("Method", "POST", "PUT"))
    }

    test (s"A POST on /a/b/c should fail with a 404 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b/c", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         404, List("not found"))
    }

    test (s"Plain text PATCH should fail on /a/z $wadlDesc with $configDesc when X-ROLES is user") {
      assertResultFailed(validator.validate(request("PATCH","/a/z","plain/text","hello!", false,
                                                    Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         415, List("content type","did not match"))
    }

    test (s"Plain text PATCH should fail on /a/z $wadlDesc with $configDesc when X-ROLES is foo") {
      assertResultFailed(validator.validate(request("PATCH","/a/z","plain/text","hello!", false,
                                                    Map[String,List[String]]("X-Roles"->List("foo"))), response, chain),
                         415, List("content type","did not match"))
    }
  }


  def happyWhenAssertsAreDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should validate with goodXML and asserts enabled on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "b"->List("ababa"),
                          "X-Auth"->List("foo!"),
                          "X-Roles"->List("user")))

      validator.validate(req, response, chain)
    }
  }

  def happyWhenParamDefaultsEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A DELETE on /a/b validate but capture header sholud be set with param defaults on $wadlDesc with $configDesc") {
      val req = request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                   "c"->List("Dbaba"),
                                   "X-Roles"->List("user", "Administrator")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("baz!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("DELETE"))
        assert(csReq.getHeaders("X-X-AUTH").toList == List("baz!"))
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }
  }

  def sadWhenAssertsAreDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should not validate with goodXML and asserts enabled on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "b"->List("ababa"),
                          "X-Auth"->List("bar!"),
                          "X-Roles"->List("user")))

      assertResultFailed(validator.validate(req, response, chain),
        400, List("foo!"))
    }
  }

  def testsWithPlainParamsEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should not validate with goodXML that does not contain an_element (plain param) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",
                                                    <some_xml att='1' xmlns='test.org'>
                                                    <another_element>
                                                    <yet_another_element />
                                                    </another_element>
                                                    </some_xml>
                                                    , false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("Expecting","tst:some_xml/tst:an_element/tst:another_element"))
    }

    test (s"A POST on /a should not validate with goodXML (wrong schema) on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("root", "tst2:a"))
    }

    test (s"A POST on /a with tst2:a and no @id attribute should fail with plain params enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml",
                                                    <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                    stepType="ACCEPT"
                                                    even="22"/>
                                                    , false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("Expecting an id attribute"))
    }

    test (s"A POST on /a with a bad schema JSON should fail on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/json",""" { "foo" : "bar" } """,false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         403, List("Need a first name"))
    }
  }


  def testsWithPlainParamsDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should validate with goodXML that does not contain an_element (plain param) $wadlDesc with $configDesc") {

      val req = request("PUT", "/a", "application/xml",
        <some_xml att='1' xmlns='test.org'>
          <another_element>
          <yet_another_element />
          </another_element>
          </some_xml>
          , false,
        Map[String,List[String]]("a"->List("abba"),
          "b"->List("ababa"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-ATT-ATTRIB").toList == List("1"))
        assert(csReq.getHeaders("X-AN-ELEM").toList == List(""))
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("false"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("false"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a should validate with goodXML (wrong schema) on $wadlDesc with $configDesc") {

      val req = request("POST", "/a", "application/xml", goodXML, false,
        Map[String,List[String]]("a"->List("Bbba"),
          "b"->List("Dbaba"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("POST"))
        assert(csReq.getHeaders("X-X-URI").toList == List("true"))
        assert(csReq.getHeaders("X-X-ROOT").toList == List(""))
        assert(csReq.getHeaders("X-X-ROOT2").toList == List(""))
      }))


      validator.validate(req, response, chain)
    }

    test (s"A POST on /a with tst2:a and no @id attribute should validate with plain params enabled $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/xml",
        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
          stepType="ACCEPT"
          even="22"/>
          , false,
        Map[String,List[String]]("a"->List("abba"),
          "b"->List("ababa"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("POST"))
        assert(csReq.getHeaders("X-X-URI").toList == List("true"))
        assert(csReq.getHeaders("X-X-ROOT").toList == List("a"))
        assert(csReq.getHeaders("X-X-ROOT2").toList == List("a"))
      }))

      validator.validate(req, response, chain)
    }

    test (s"A POST on /a with a bad schema JSON should validate on $wadlDesc with $configDesc") {
      val req = request("POST", "/a", "application/json",""" { "foo" : "bar" } """,false,
        Map[String,List[String]]("a"->List("abba"),
          "b"->List("ababa"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("user")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("true"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(!csReq.getHeaders("X-X-METHOD").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT").hasMoreElements)
        assert(!csReq.getHeaders("X-X-ROOT2").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }
  }

  def testsWithRaxRolesEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should fail with goodXML with no roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         403, List("You are forbidden"))
     }

     test (s"A PUT on /a should fail with goodXML and an unknown role on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                   Map[String,List[String]]("a"->List("abba"),
                                                                            "b"->List("ababa"),
                                                                            "X-Auth"->List("foo!"),
                                                                            "X-Roles"->List("bizRole","admin"))), response, chain),
                         403, List("You are forbidden"))
     }


    test (s"A DELETE on /a/b should fail with multiple unknown roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("bizBuz", "Wooga"))), response, chain),
                         403, List("You are forbidden"))
    }


    test (s"A DELETE on /a/b should fail with no roles $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         403, List("You are forbidden"))
    }
  }


  def testsWithRaxRolesDisabled(validator : Validator, wadlDesc : String, configDesc : String) {

    test (s"A PUT on /a should validate with goodXML with no roles on $wadlDesc with $configDesc") {
      val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "c"->List("ababa"),
                          "X-Auth"->List("foo!")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-ATT-ATTRIB").toList == List("1"))
        assert(csReq.getHeaders("X-AN-ELEM").toList == List("an_element"))
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("false"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("false"))
      }))

      validator.validate(req, response, chain)

    }

    test (s"A PUT on /a should validate with goodXML and an unknown role on $wadlDesc with $configDesc") {
       val req = request("PUT", "/a", "application/xml",goodXML, false,
                          Map[String,List[String]]("a"->List("abba"),
                          "c"->List("ababa"),
                          "X-Auth"->List("foo!"),
                          "X-Roles"->List("adehount")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-ATT-ATTRIB").toList == List("1"))
        assert(csReq.getHeaders("X-AN-ELEM").toList == List("an_element"))
        assert(csReq.getHeaders("X-X-URI-LEVEL").toList == List("1"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-A").toList == List("true"))
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-BODY-EMPTY").toList == List("false"))
        assert(csReq.getHeaders("X-BODY-EMPTY2").toList == List("false"))
      }))

      validator.validate(req, response, chain)
    }


    test (s"A DELETE on /a/b should validate with multiple unknown roles on $wadlDesc with $configDesc") {
      val req = request("DELETE", "/a/b", null, "", false,
        Map[String,List[String]]("a"->List("Bbba"),
          "c"->List("Dbaba"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("asBEKUH", "13456")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("DELETE"))
        assert(csReq.getHeaders("X-X-AUTH").toList == List("foo!"))
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }


    test (s"A DELETE on /a/b should validate with no roles $wadlDesc with $configDesc") {
      val req = request("DELETE", "/a/b", null, "", false,
        Map[String,List[String]]("a"->List("Bbba"),
          "c"->List("Dbaba"),
          "X-Auth"->List("foo!")))

      req.setAttribute(ASSERT_FUNCTION, ((csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
        assert(csReq.getHeaders("X-HEADERS-START-WITH-B").toList == List("false"))
        assert(csReq.getHeaders("ALL-X-AUTH-HEADERS").toList == List("foo!"))
        assert(csReq.getHeaders("X-X-METHOD").toList == List("DELETE"))
        assert(csReq.getHeaders("X-X-AUTH").toList == List("foo!"))
        assert(!csReq.getHeaders("X-HEADERS-START-WITH-A").hasMoreElements)
        assert(!csReq.getHeaders("X-X-URI-LEVEL").hasMoreElements)
      }))

      validator.validate(req, response, chain)
    }
  }


  def testsWithRaxRolesMaskEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should validate with goodXML with no roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         405, List("PUT"))
     }

     test (s"A PUT on /a should validate with goodXML and an unknown role on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                   Map[String,List[String]]("a"->List("abba"),
                                                                            "b"->List("ababa"),
                                                                            "X-Auth"->List("foo!"),
                                                                            "X-Roles"->List("bizRole","admin"))), response, chain),
                         405, List("PUT"))
     }


    test (s"A DELETE on /a/b should validate with multiple unknown roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("bizBuz", "Wooga"))), response, chain),
                         404, List("Resource not found"))
    }


    test (s"A DELETE on /a/b should validate with no roles $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         404, List("Resource not found"))
    }
  }


  //
  // With captureHeaders disabled
  //

  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderDisabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreDisabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  When captureHeaders are enabled
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderEnabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsDisabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  When captureHeaders and Assert are enabled
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderAssertEnabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      sadWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsDisabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  When captureHeaders and param defaults are enabled
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderEnabledParamDefaultConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      happyWhenParamDefaultsEnabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsDisabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  When captureHeaders with plain params enabled
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderEnabledPlainParamConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled (validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  When captureHeaders with plain params enabled, raxroles
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderEnabledPlainRaxRoles) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled (validator, wadlDesc, configDesc)
      testsWithRaxRolesEnabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  When captureHeaders with plain params enabled, raxroles masked
  //
  for ((wadlDesc, wadl) <- captureHeaderWADLs) {
    for ((configDesc, config) <- captureHeaderEnabledPlainRaxRolesMask) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      happyWhenCaptureHeadersAreEnabled (validator, wadlDesc, configDesc)
      happyWhenAssertsAreDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled (validator, wadlDesc, configDesc)
      testsWithRaxRolesMaskEnabled(validator, wadlDesc, configDesc)
    }
  }

}
