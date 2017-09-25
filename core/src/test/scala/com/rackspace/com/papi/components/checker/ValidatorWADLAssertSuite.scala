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
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

import scala.xml.Elem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

@RunWith(classOf[JUnitRunner])
class ValidatorWADLAssertSuite extends BaseValidatorSuite {
  ///
  ///  Configs
  ///
  val baseConfig = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.enableAssertExtension = false
    c
  }

  val baseWithAssert = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c
  }

  val baseWithAssertRemoveDups = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c
  }


  val baseWithAssertRemoveDupsMethodLabels = {
    val c = TestConfig()
    c.xpathVersion = 31
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.preserveMethodLabels = true
    c
  }


  val baseWithAssertParamDefaults = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = true
    c.checkHeaders = true
    c.enableAssertExtension = true
    c
  }

  val baseWithAssertParamDefaultsRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = true
    c.checkHeaders = true
    c.enableAssertExtension = true
    c
  }


  val baseWithAssertParamDefaultsRemoveDupsMethodLabels = {
    val c = TestConfig()
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = true
    c.checkHeaders = true
    c.enableAssertExtension = true
    c.preserveMethodLabels = true
    c
  }


  val baseAssertWithPlainParams = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c
  }

  val baseAssertWithJoinXPaths = {
    val c = TestConfig()
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c
  }

  val baseAssertWithJoinXPathsMethodLabels = {
    val c = TestConfig()
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.preserveMethodLabels = true
    c
  }

  val baseAssertWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c
  }

  val baseAssertWithJoinXPathsAndRemoveDupsMethodLabels = {
    val c = TestConfig()
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.preserveMethodLabels = true
    c
  }


//  RaxRoles Configs

  val baseWithPlainParamsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c
  }


  val baseWithRemoveDupsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c
  }

  val baseWithRemoveDupsRaxRolesMethodLabels = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c.preserveMethodLabels = true
    c
  }


  val baseWithJoinXPathsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
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
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRolesMethodLabels = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c.preserveMethodLabels = true
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
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
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
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c
  }

  val baseWithRemoveDupsRaxRolesMaskMethodLabels = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c.preserveMethodLabels = true
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
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
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
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRolesMaskMethodLabels = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = true
    c.checkElements = true
    c.preserveMethodLabels = true
    c
  }


val WADL_withAsserts = <application xmlns="http://wadl.dev.java.net/2009/02"
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
                        <rax:assert test="tst:some_xml/@att='1'" message="expect att to = 1"/>
                        <rax:assert test="$body/tst:some_xml/tst:an_element" message="expect an_element"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                        <rax:assert test="false()" message="YAML makes us fail!" code="500"/>
                    </representation>
                    <!-- assertion should be placed in all representations -->
                    <representation>
                        <rax:assert test="not(empty($body))" message="There should be a body"/>
                        <rax:assert test="not(empty($_))" message="There should be a $_"/>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst2:a">
                        <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="Expecting an id attribute"/>
                        <!-- This assertion applies only to POST /a when the representation is XML -->
                        <rax:assert test="$req:method='POST' and $req:uri='/a' and /tst2:a" message="This assertion should never fire!!" code="500"/>
                        <rax:assert test="$req:method='POST' and $req:uri='/a' and $_/tst2:a" message="This assertion should never fire!!" code="500"/>
                    </representation>
                    <representation mediaType="application/json">
                        <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="Need a first name" rax:code="403"/>
                    </representation>
                </request>
            </method>
            <resource path="b">
                <method name="GET"/>
                <method name="DELETE" rax:roles="admin Administrator">
                  <request>
                    <param name="X-AUTH" style="header" type="xs:string" default="foo!" required="true" repeating="true"/>
                    <!-- Should be treated as a request assertion -->
                    <representation>
                        <rax:assert test="$req:method='DELETE'"/>
                        <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                    </representation>
                  </request>
                </method>
                <method name="POST">
                    <request>
                        <representation mediaType="application/xml">
                        </representation>
                        <representation mediaType="application/json">
                            <!-- This assertion applies only to POST /a/b if the representation is JSON -->
                            <rax:assert test="$req:uri='/a/b' and not(empty($body))" message="The request path should be /a/b and there should be a JSON body" code="400"/>
                            <rax:assert test="$_?stuff?string = 'A String'" message="Expect JSON to have 'A String'"/>
                        </representation>
                        <!-- This assertion applies only to POST /a/b request regardless of the representation-->
                        <rax:assert test="$req:uri='/a/b'" message="The request path should be /a/b" code="400"/>
                    </request>
                </method>
            </resource>
            <resource path="z">
              <method name="PATCH">
                <request>
                    <representation mediaType="application/xml" element="tst2:a">
                        <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="Expecting an id attribute"/>
                        <rax:assert test="$req:method='PATCH' and $req:uri='/a' and /tst2:a" message="This assertion should never fire!!" code="500"/>
                    </representation>
                </request>
              </method>
              <method name="PATCH" rax:roles="#all">
                <request>
                    <representation mediaType="application/json" rax:roles="#all">
                        <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="Need a first name" rax:code="403"/>
                    </representation>
                </request>
              </method>
            </resource>
            <!-- This assertion applies to all requests in the resource /a -->
            <rax:assert test="$req:uri='/a'" message="The request path should be /a" code="400"/>
            <rax:assert test="$req:uriLevel = 1" message="Bad URL Level this shouldn't happen" code="500"/>
            <rax:assert test="some $h in $req:headerNames satisfies starts-with($h, 'a')" message="There should be a header that starts with a"/>
            <!-- This assertion applies to all requests in the resource /a AND all subresources of a /a/b for example-->
            <rax:assert test="some $h in $req:headerNames satisfies starts-with($h,'b')" message="There should be a header that starts with b" code="400" applyToChildren="true"/>
        </resource>
        <!-- This assertion applies to all requests in the WADL -->
        <rax:assert test="'foo!' = req:headers('X-AUTH', true())" message="The X-AUTH header should always be specified and it should be foo!" code="400" />
    </resources>
   </application>


val WADL_withAsserts2 = <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:req="http://www.rackspace.com/repose/wadl/checker/request"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:tst="test.org"
             xmlns:tst2="http://www.rackspace.com/repose/wadl/checker/step/test"
              >
    <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="user">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <param name="test" style="plain" path="tst:some_xml/tst:an_element/tst:another_element" required="true"/>
                        <rax:assert test="tst:some_xml/@att='1'" message="expect att to = 1"/>
                        <rax:assert test="$req:body/tst:some_xml/tst:an_element" message="expect an_element"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                        <rax:assert test="false()" message="YAML makes us fail!" code="500"/>
                    </representation>
                    <!-- assertion should be placed in all representations -->
                    <representation>
                        <rax:assert test="not(empty($req:body))" message="There should be a body"/>
                        <rax:assert test="not(empty($req:_))" message="There should be a $req:_"/>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst2:a">
                        <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="Expecting an id attribute"/>
                        <!-- This assertion applies only to POST /a when the representation is XML -->
                        <rax:assert test="$req:method='POST' and $req:uri='/a' and /tst2:a" message="This assertion should never fire!!" code="500"/>
                        <rax:assert test="$req:method='POST' and $req:uri='/a' and $req:_/tst2:a" message="This assertion should never fire!!" code="500"/>
                    </representation>
                    <representation mediaType="application/json">
                        <param name="test3" style="plain" path="$ _?firstName" required="true" rax:message="Need a first name" rax:code="403"/>
                    </representation>
                </request>
            </method>
            <resource path="b">
                <method name="GET"/>
                <method name="DELETE" rax:roles="admin Administrator">
                  <request>
                    <param name="X-AUTH" style="header" type="xs:string" default="foo!" required="true" repeating="true"/>
                    <!-- Should be treated as a request assertion -->
                    <representation>
                        <rax:assert test="$req:method='DELETE'"/>
                        <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                    </representation>
                  </request>
                </method>
                <method name="POST">
                    <request>
                        <representation mediaType="application/xml">
                        </representation>
                        <representation mediaType="application/json">
                            <!-- This assertion applies only to POST /a/b if the representation is JSON -->
                            <rax:assert test="$req:uri='/a/b' and not(empty($req:body))" message="The request path should be /a/b and there should be a JSON body" code="400"/>
                            <rax:assert test="$req:_?stuff?string = 'A String'" message="Expect JSON to have 'A String'"/>
                        </representation>
                        <!-- This assertion applies only to POST /a/b request regardless of the representation-->
                        <rax:assert test="$req:uri='/a/b'" message="The request path should be /a/b" code="400"/>
                    </request>
                </method>
            </resource>
            <resource path="z">
              <method name="PATCH">
                <request>
                    <representation mediaType="application/xml" element="tst2:a">
                        <param name="test2" style="plain" path="tst2:a/@id" required="true" rax:message="Expecting an id attribute"/>
                        <rax:assert test="$req:method='PATCH' and $req:uri='/a' and /tst2:a" message="This assertion should never fire!!" code="500"/>
                    </representation>
                </request>
              </method>
              <method name="PATCH" rax:roles="#all">
                <request>
                    <representation mediaType="application/json" rax:roles="#all">
                        <param name="test3" style="plain" path="$_?firstName" required="true" rax:message="Need a first name" rax:code="403"/>
                    </representation>
                </request>
              </method>
            </resource>
            <!-- This assertion applies to all requests in the resource /a -->
            <rax:assert test="$req:uri='/a'" message="The request path should be /a" code="400"/>
            <rax:assert test="$req:uriLevel = 1" message="Bad URL Level this shouldn't happen" code="500"/>
            <rax:assert test="some $h in $req:headerNames satisfies starts-with($h, 'a')" message="There should be a header that starts with a"/>
            <!-- This assertion applies to all requests in the resource /a AND all subresources of a /a/b for example-->
            <rax:assert test="some $h in $req:headerNames satisfies starts-with($h,'b')" message="There should be a header that starts with b" code="400" applyToChildren="true"/>
        </resource>
        <!-- This assertion applies to all requests in the WADL -->
        <rax:assert test="'foo!' = req:headers('X-AUTH', true())" message="The X-AUTH header should always be specified and it should be foo!" code="400" />
    </resources>
   </application>


  //
  // Config combinations
  //
  val assertDisabledConfigs = Map[String, Config]("base config with asserts disabled"->baseConfig)

  val assertEnabledConfigs  = Map[String, Config]("Config with asserts enabled"->baseWithAssert,
    "Config with asserts and remove dups"->baseWithAssertRemoveDups,
    "Config with asserts and remove dups preserve method labels "->baseWithAssertRemoveDupsMethodLabels
  )

  val assertEnabledParamDefaultConfigs = Map[String, Config]("Config with asserts enabled, param defaults"->baseWithAssertParamDefaults,
    "Config with asserts enabled, param defaults and remove dups"->baseWithAssertParamDefaultsRemoveDups,
    "Config with asserts enabled, param defaults and remove dups preserve method labels"->baseWithAssertParamDefaultsRemoveDupsMethodLabels
  )

  val assertEnabledPlainParamConfigs = Map[String, Config]("Config with asserts enabled, plain params"->baseAssertWithPlainParams,
    "Config with asserts enabled, plain params join XPath"->baseAssertWithJoinXPaths,
    "Config with asserts enabled, plain params join XPath, remove dups"->baseAssertWithJoinXPathsAndRemoveDups,
    "Config with asserts enabled, plain params join XPath preserve method labels"->baseAssertWithJoinXPathsMethodLabels,
    "Config with asserts enabled, plain params join XPath, remove dups preserve method labels"->baseAssertWithJoinXPathsAndRemoveDupsMethodLabels
  )

  val assertEnabledPlainRaxRoles = Map[String, Config]("Config with asserts enabled, plain params, rax roles"->baseWithPlainParamsRaxRoles,
    "Config with asserts enabled, plain params, rax roles, remove dups"->baseWithRemoveDupsRaxRoles,
    "Config with asserts enabled, plain params, rax roles, remove dups method labels"->baseWithRemoveDupsRaxRolesMethodLabels,
    "Config with asserts enabled, plain params, rax roles, join xpath"->baseWithJoinXPathsRaxRoles,
    "Config with asserts enabled, plain params, rax roles, remove dups, join xpath"->baseWithJoinXPathsAndRemoveDupsRaxRoles,
    "Config with asserts enabled, plain params, rax roles, remove dups, join xpath, preserve method labels"->baseWithJoinXPathsAndRemoveDupsRaxRolesMethodLabels)

  val assertEnabledPlainRaxRolesMask = Map[String, Config]("Config with asserts enabled, plain params, rax roles masked"->baseWithPlainParamsRaxRolesMask,
    "Config with asserts enabled, plain params, rax roles masked, remove dups"->baseWithRemoveDupsRaxRolesMask,
    "Config with asserts enabled, plain params, rax roles masked, remove dups, preserve method labels"->baseWithRemoveDupsRaxRolesMaskMethodLabels,
    "Config with asserts enabled, plain params, rax roles masked, join xpath"->baseWithJoinXPathsRaxRolesMask,
    "Config with asserts enabled, plain params, rax roles masked, remove dups, join xpath, preserve method labels"->baseWithJoinXPathsAndRemoveDupsRaxRolesMaskMethodLabels)


  //
  // WADL combinations
  //
  val assertWADLs = Map[String, Elem]("WADL with $body assertions"->WADL_withAsserts,
                                      "WADL with $req:body assertions"->WADL_withAsserts2)


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


  def happyWhenAssertionsAreDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should validate with goodXML with an @att=2 on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",
                                 <some_xml att='2' xmlns='test.org'>
                                   <an_element>
                                     <another_element />
                                   </an_element>
                                 </some_xml>
                                 , false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }


     test (s"A PUT on /a should validate with goodXML but only X-Roles headers on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                 Map[String,List[String]]("X-Roles"->List("user"))), response, chain)
     }



     test (s"A PUT on /a should validate with goodXML that does not contain an_element $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",
                                 <some_xml att='1' xmlns='test.org'>
                                   <another_element>
                                     <yet_another_element />
                                   </another_element>
                                 </some_xml>
                                 , false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }


    test (s"A PUT on /a should validate with YAML  $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "text/x-yaml","""---
- name: Hello World!
                                 """, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A PUT on /a should validate with empty JSON body $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json", "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A PUT on /a should validate with good JSON body and only X-Roles headers $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("X-Roles"->List("user"))), response, chain)
    }


    test (s"A POST on /a should validate with goodXML (wrong schema) on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml",goodXML, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
     }



    test (s"A POST on /a should validate with goodJSON (wrong schema) on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json","""  { "foo" : "bar" } """, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A DELETE on /a/b should validate on  with only X-ROLES headers $wadlDesc with $configDesc") {
      validator.validate(request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("X-Roles"->List("user","admin"))), response, chain)
    }

    test (s"A POST on /a/b should validate with empty JSON body only X-ROLES headers $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json", null.asInstanceOf[String], false,
                                 Map[String,List[String]]("X-Roles"->List("user"))), response, chain)
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

  def sadWhenAssertionsAreEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should not validate with goodXML with an @att=2 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",
                                                    <some_xml att='2' xmlns='test.org'>
                                                    <an_element>
                                                    <another_element />
                                                    </an_element>
                                                    </some_xml>
                                                    , false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("expect att to = 1"))
     }

    test (s"A PUT on /a should not validate with goodXML but no headers on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         400, List("There should be a header that starts with a"))
     }

    test (s"A PUT on /a should not validate with goodXML but no b header on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("There should be a header that starts with b"))
    }

    test (s"A PUT on /a should not validate with goodXML but no X-AUTH header on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("abba"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("X-Auth","foo!"))
    }

    test (s"A PUT on /a should not validate with YAML  $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "text/x-yaml","""---
- name: Hello World!
                                 """, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         500, List("YAML makes us fail!"))
    }

    test (s"A PUT on /a should not validate with empty JSON body $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/json", "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("No content"))
    }

    test (s"A PUT on /a should not validate with good JSON body and no headers $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         400, List("There should be a header that starts with b"))
    }

    test (s"A PUT on /a should not validate with good JSON body and b header but no auth header $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json", goodJSON, false,
                                 Map[String,List[String]]("b"->List("abba"),
                                                          "X-Roles"->List("user"))), response, chain),
                         400, List("X-AUTH","foo!"))
    }

    test (s"A PUT on /a should not validate with good XML body and no headers on application/xml $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML, false,
                                 Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         400, List("There should be a header that starts with b"))
    }

    test (s"A PUT on /a should not validate with good XML body and b header but no auth header $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML, false,
                                 Map[String,List[String]]("b"->List("abba"),
                                                          "X-Roles"->List("user"))), response, chain),
                         400, List("X-AUTH","foo!"))
    }

    test (s"A DELETE on /a/b should not validate on with 'foo!' is not the first value $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("x-auth"->List("bar","foo!"),
                                                                             "X-Roles"->List("admin"))),
                                            response, chain),
                         400, List("req:header('X-AUTH') = 'foo!'"))
    }

    test (s"A POST on /a/b should not validate with empty JSON body no headers $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json", null.asInstanceOf[String], false,
                                                    Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
                         400, List("No content"))
    }

    test (s"A POST on /a/b should not validate with JSON with bad schema on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json", goodJSON_Schema1, false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         400, List("Expect JSON to have 'A String'"))
    }
  }

  def happyWithParamDefaultsEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A DELETE on /a/b should validate on when no X-Auth is specified $wadlDesc with $configDesc") {
      validator.validate(request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Roles"->List("user"))), response, chain)
    }
  }

  def sadWithParamDefaultsDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A DELETE on /a/b should not validate on  with incorrect Auth header $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("X-Auth"->List("Bust"),
                                                                             "X-Roles"->List("admin"))), response, chain),
                         400, List("req:header('X-AUTH') = 'foo!'"))
    }
  }

  def testsWithPlainParamsDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a should not validate with goodXML that does not contain an_element $wadlDesc with $configDesc") {
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
                         400, List("expect an_element"))
    }

    test (s"A POST on /a should not validate with goodXML (wrong schema) on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("user"))), response, chain),
                         500, List("This assertion should never fire!!"))
    }

    test (s"A POST on /a with tst2:a and no @id attribute should succeed on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml",
                                 <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                 stepType="ACCEPT"
                                 even="22"/>
                                 , false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
    }

    test (s"A POST on /a with a bad schema JSON should succeed on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json",""" { "foo" : "bar" } """,false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("user"))), response, chain)
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

  def testsWithRaxRolesDisabled(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should validate with goodXML with no roles on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"))), response, chain)
     }

     test (s"A PUT on /a should validate with goodXML and an unknown role on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                 Map[String,List[String]]("a"->List("abba"),
                                                          "b"->List("ababa"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("bizRole","admin"))), response, chain)
     }


    test (s"A DELETE on /a/b should validate with multiple unknown roles on $wadlDesc with $configDesc") {
      validator.validate(request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"),
                                                          "X-Roles"->List("bizBuz", "Wooga"))), response, chain)
    }


    test (s"A DELETE on /a/b should validate with no roles $wadlDesc with $configDesc") {
      validator.validate(request("DELETE", "/a/b", null, "", false,
                                 Map[String,List[String]]("a"->List("Bbba"),
                                                          "b"->List("Dbaba"),
                                                          "X-Auth"->List("foo!"))), response, chain)
    }
  }

  def testsWithRaxRolesEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
     test (s"A PUT on /a should validate with goodXML with no roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                    Map[String,List[String]]("a"->List("abba"),
                                                                             "b"->List("ababa"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         403, List("You are forbidden"))
     }

     test (s"A PUT on /a should validate with goodXML and an unknown role on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml",goodXML, false,
                                                   Map[String,List[String]]("a"->List("abba"),
                                                                            "b"->List("ababa"),
                                                                            "X-Auth"->List("foo!"),
                                                                            "X-Roles"->List("bizRole","admin"))), response, chain),
                         403, List("You are forbidden"))
     }


    test (s"A DELETE on /a/b should validate with multiple unknown roles on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"),
                                                                             "X-Roles"->List("bizBuz", "Wooga"))), response, chain),
                         403, List("You are forbidden"))
    }


    test (s"A DELETE on /a/b should validate with no roles $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("DELETE", "/a/b", null, "", false,
                                                    Map[String,List[String]]("a"->List("Bbba"),
                                                                             "b"->List("Dbaba"),
                                                                             "X-Auth"->List("foo!"))), response, chain),
                         403, List("You are forbidden"))
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
  // With assertions disabled
  //

  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertDisabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happyWhenAssertionsAreDisabled(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
    }
  }

  //
  //  With asserts enabled
  //
  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertEnabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      sadWhenAssertionsAreEnabled(validator, wadlDesc, configDesc)
      sadWithParamDefaultsDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsDisabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  // With param defaults and assert enabled configs
  //
  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertEnabledParamDefaultConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      sadWhenAssertionsAreEnabled(validator, wadlDesc, configDesc)
      happyWithParamDefaultsEnabled (validator, wadlDesc, configDesc)
      testsWithPlainParamsDisabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With plain params and asserts enabled
  //
  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertEnabledPlainParamConfigs) {
      val validator = Validator(wadl, config)
      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      sadWhenAssertionsAreEnabled(validator, wadlDesc, configDesc)
      sadWithParamDefaultsDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With plain params and rax:roles and asserts enabled
  //
  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertEnabledPlainRaxRoles) {
      val validator = Validator(wadl, config)
      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      sadWhenAssertionsAreEnabled(validator, wadlDesc, configDesc)
      sadWithParamDefaultsDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesEnabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  With plain params and rax:roles Masked and asserts enabled
  //
  for ((wadlDesc, wadl) <- assertWADLs) {
    for ((configDesc, config) <- assertEnabledPlainRaxRolesMask) {
      val validator = Validator(wadl, config)
      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths (validator, wadlDesc, configDesc)
      sadWhenAssertionsAreEnabled(validator, wadlDesc, configDesc)
      sadWithParamDefaultsDisabled(validator, wadlDesc, configDesc)
      testsWithPlainParamsEnabled(validator, wadlDesc, configDesc)
      testsWithRaxRolesMaskEnabled(validator, wadlDesc, configDesc)
    }
  }


}
