/***
 *   Copyright 2015 Rackspace US, Inc.
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

import scala.xml._

@RunWith(classOf[JUnitRunner])
class ValidatorWADLHeaderSuite2 extends BaseValidatorSuite {
  type TestWADL    = (String, NodeSeq) /* Descrption, WADL */
  type CaseConfig  = (String, Config)  /* Description, TestConfig */
  type ConfigList  = List[CaseConfig]  /* A list of test configs */
  type Suite        = (String, Validator) => Unit  /* A Function that runs tests, given a validator and a description */
  type SuiteList    = List[Suite]        /* A list of tests */

  type TestCase = (TestWADL, ConfigList, SuiteList)

  def run(t : TestCase) : Unit = {
    val testWADL   : TestWADL = t._1
    val configList : ConfigList = t._2
    val suiteList : SuiteList = t._3

    configList.foreach ( c => {
      val validator = Validator((localWADLURI, testWADL._2), c._2)
      suiteList.foreach (t => {
        t(testWADL._1+" : "+c._1, validator)
      })
    })
  }

  val checkHeadersDisabledConfig : CaseConfig = ("check headers disabled", TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, false, false, false))
  val checkHeadersDisabledConfigDups : CaseConfig = ("check headers disabled and remove dups enabled", TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, false, false, false))
  val checkHeadersConfig : CaseConfig = ("check headers enabled", TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, false))
  val checkHeadersConfigDups : CaseConfig = ("check headers and remove dups enabled", TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, false))
  val checkHeadersConfigDisableAnyMatch  : CaseConfig = ("check headers enabled, anymatch extension disabled", {
    val tc = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, false)
    tc.enableAnyMatchExtension = false
    tc
  })
  val checkHeadersConfigDupsDisableAnyMatch : CaseConfig = ("check headers and remove dups enabled, anymatch disabled", {
    val tc = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, false)
    tc.enableAnyMatchExtension = false
    tc
  })

  val checkHeadersDefaultsConfig : CaseConfig = ("check headers and param defaults enabled", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, false);
    t.setParamDefaults = true;
    t
  })
  val checkHeadersDefaultsConfigDups : CaseConfig = ("check headers, param defaults,  and remove dups enabled", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, false);
    t.setParamDefaults = true;
    t
  })

  val checkHeadersDefaultsConfigDisableAnyMatch : CaseConfig = ("check headers and param defaults enabled disabled anyMatch", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, false);
    t.setParamDefaults = true;
    t.enableAnyMatchExtension = false
    t
  })
  val checkHeadersDefaultsConfigDupsDisableAnyMatch : CaseConfig = ("check headers, param defaults,  and remove dups enabled, disabled anyMatch", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, false);
    t.setParamDefaults = true;
    t.enableAnyMatchExtension = false
    t
  })

  val checkHeadersDisabledMsgConfig : CaseConfig = ("check headers disabled, message extension enabled",
                                                    TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, false, false, true))

  val checkHeadersDisabledMsgConfigDups : CaseConfig = ("check headers disabled and remove dups enabled and message extension enabled",
                                                        TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, false, false, true))


  val checkHeadersDisabledMsgConfigDisableAnyMatch : CaseConfig = ("check headers disabled, message extension enabled disable any match", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, false, false, true)
    t.enableAnyMatchExtension = false
    t
  })

  val checkHeadersDisabledMsgConfigDupsDisableAnyMatch : CaseConfig = ("check headers disabled and remove dups enabled and message extension enabled disable any match", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, false, false, true)
    t.enableAnyMatchExtension = false
    t
  })


  val checkHeadersMsgConfig : CaseConfig = ("check headers enabled and message extension enabled",
                                            TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, true))

  val checkHeadersMsgConfigDups : CaseConfig = ("check headers and remove dups enabled and message extension enabled",
                                                TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, true))


  val checkHeadersMsgConfigDisableAnyMatch : CaseConfig = ("check headers enabled and message extension enabled disable any match", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, true)
    t.enableAnyMatchExtension = false
    t
  })

  val checkHeadersMsgConfigDupsDisableAnyMatch : CaseConfig = ("check headers and remove dups enabled and message extension enabled disable any match", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, true)
    t.enableAnyMatchExtension = false
    t
  })


  val checkHeadersMsgDefaultsConfig : CaseConfig = ("check headers and param defaults enabled and message extension enabled", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, true);
    t.setParamDefaults = true;
    t
  })

  val checkHeadersMsgDefaultsConfigDups : CaseConfig = ("check headers, param defaults,  and remove dups enabled and message extension enabled", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, true);
    t.setParamDefaults = true;
    t
  })


  val checkHeadersMsgDefaultsConfigDisableAnyMatch : CaseConfig = ("check headers and param defaults enabled and message extension enabled disable any match", {
    val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true, false, true);
    t.setParamDefaults = true;
    t.enableAnyMatchExtension = false
    t
  })

  val checkHeadersMsgDefaultsConfigDupsDisableAnyMatch : CaseConfig = ("check headers, param defaults,  and remove dups enabled and message extension enabled disable any match", {
    val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, false, true);
    t.setParamDefaults = true;
    t.enableAnyMatchExtension = false
    t
  })

  val resourceHeadersWADL : TestWADL = ("WADL with mixed same name headers at the resource level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)

  val resourceHeadersMixedCaseWADL : TestWADL = ("WADL with mixed same name headers (mixed case) at the resource level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-test" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-test" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                        <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)


  val resourceHeadersAllMatchWADL : TestWADL = ("WADL with mixed same name headers at the resource level anyMatch=false",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="false"/>
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="false"/>
                                        <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!"/>
                                        <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="0"/>
                                        <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)


  val resourceHeadersNoRepeatWADL : TestWADL = ("WADL with mixed same name non-repeating headers at the resource level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="false" default="foo!" rax:code="401" rax:message="No!"/>
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" rax:code="401" rax:message="No!"/>
                                        <param name="X-TEST" style="header" type="xsd:dateTime" required="true" rax:code="401" rax:message="No!"/>
                                        <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="false" rax:code="401" rax:message="No!"/>
                                        <param name="X-TESTO" style="header" type="xsd:int" required="true" default="42" rax:message="Bad Testo"/>
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)


  val requestHeadersWADL : TestWADL = ("WADL with mixed same name headers at the request level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                             <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)

  val requestHeadersMixedCaseWADL : TestWADL = ("WADL with mixed same name headers (mixed case) at the request level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-test" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-test" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                             <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)

  val requestHeadersAllMatchWADL : TestWADL = ("WADL with mixed same name headers at the request level anyMatch = false",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true" default="foo!" rax:code="401" rax:message="No!" rax:anyMatch="false"/>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="false"/>
                                             <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="0"/>
                                             <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="0"/>
                                             <param name="X-TESTO" style="header" type="xsd:int" required="true" repeating="true" default="42" rax:message="Bad Testo"/>
                                             <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)


  val requestHeadersNoRepeatWADL : TestWADL = ("WADL with mixed same name no repeat headers at the request level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:rax="http://docs.rackspace.com/api"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <method name="PUT">
                                          <request>
                                            <representation mediaType="application/xml" element="tst:a"/>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="POST">
                                          <request>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" default="foo!" rax:code="401" rax:message="No!"/>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="false" rax:code="401" rax:message="No!"/>
                                             <param name="X-TEST" style="header" type="xsd:dateTime" required="true" repeating="0" rax:code="401" rax:message="No!"/>
                                             <param name="X-TEST" style="header" type="xsd:date" required="true" repeating="true" rax:code="401" rax:message="No!" rax:anyMatch="true"/>
                                             <param name="X-TESTO" style="header" type="xsd:int" required="true" default="42" rax:message="Bad Testo"/>
                                             <representation mediaType="application/xml" element="tst:e"/>
                                          </request>
                                        </method>
                                      </resource>
                                      <resource path="/c">
                                        <method name="POST">
                                          <request>
                                            <representation mediaType="application/json"/>
                                          </request>
                                        </method>
                                        <method name="GET"/>
                                       </resource>
                                      </resources>
                                      </application>)

  def mixedHeaderHappyPathTests (desc : String, validator : Validator) : Unit = {
    test(s"$desc : should allow GET on /c ") {
      validator.validate(request("GET","/c"), response, chain)
    }

    test(s"$desc : should allow json post on /c, with no headers") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow json post on /c, with weird X-TEST header") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map("X-TEST"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26 and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2001-10-26"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo!, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!", "baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar!, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!", "baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing", "2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo! and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML (header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!;q=1.0"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML (multiple header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!;q=1.0;rockit=true"), "X-TESTO"->List("23"))), response, chain)
    }



    test(s"$desc : should allow POST on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=foo!, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!", "baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=bar!, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("bar!", "baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z, baz, bing and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("baz", "bing", "2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

  }


  def mixedHeaderHappyPathTestsAllMatch (desc : String, validator : Validator) : Unit = {
    test(s"$desc : should allow GET on /c ") {
      validator.validate(request("GET","/c"), response, chain)
    }

    test(s"$desc : should allow json post on /c, with no headers") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow json post on /c, with weird X-TEST header") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map("X-TEST"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26 and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2001-10-26"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo!, 2001-10-26,  2001-10-26T19:32:52Z, bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!", "2001-10-26", "2001-10-26T19:32:52Z", "bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo!, 2001-10-26,  2001-10-26T19:32:52Z, bar! and X-TESTO=23 and goodXML (with header params)") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!;q=1.0", "2001-10-26", "2001-10-26T19:32:52Z;q=0.5", "bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo!, 2001-10-26,  2001-10-26T19:32:52Z, bar! and X-TESTO=23 and goodXML (with mulitiple header params)") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!;q=1.0;rockit=true", "2001-10-26;q=1", "2001-10-26T19:32:52Z;q=0.5;rockit=true", "bar!"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT on /a/b with X-TEST=bar!, 2001-10-26,  2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!", "2001-10-26", "2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z, foo!, bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!", "bar!", "2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo! and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML (header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!; q=1.0"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML (multiple header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!;q=1.0 ; rockit=true"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=foo!, bar!, 1975-07-08 and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!", "bar!", "1975-07-08"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=bar!, 1975-07-08, foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("bar!", "1975-07-08", "foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z, 1975-07-08, foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("1975-07-08", "foo!", "2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

  }


  def mixedHeaderHappyPathNoRepeatTests (desc : String, validator : Validator) : Unit = {
    test(s"$desc : should allow GET on /c ") {
      validator.validate(request("GET","/c"), response, chain)
    }

    test(s"$desc : should allow json post on /c, with no headers") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow json post on /c, with weird X-TEST header") {
      validator.validate(request("POST", "/c", "application/json", goodJSON, false, Map("X-TEST"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2011-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("2011-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=34 and goodXML") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("34"))), response, chain)
    }


    test(s"$desc : should allow PUT on /a/b with X-TEST=bar! and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT on /a/b with X-TEST=2001-10-26 and X-TESTO=23 and goodJSON") {
      validator.validate(request("PUT", "/a/b", "application/json", goodJSON, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=bar! and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("bar!"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML (header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z;q=1.0"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=2001-10-26T19:32:52Z and X-TESTO=23 and goodXML (multiple header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("2001-10-26T19:32:52Z;q=1.0;rockit=true"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST on /a/b with X-TEST=foo! and X-TESTO=32 and goodXML") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("32"))), response, chain)
    }
  }


  def mixedHeaderHappyNegTests (desc : String, validator : Validator) : Unit = {
    test(s"$desc : should not allow put on /c") {
      assertResultFailed(validator.validate(request("PUT", "/c", "application/json", goodJSON, false, Map[String,List[String]]()), response, chain),
                         405)
    }

    test(s"$desc : should not allow GET on /a") {
      assertResultFailed(validator.validate(request("GET", "/a"), response, chain), 405)
    }

    test(s"$desc : should not allow bad xml on PUT /a/b") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("xpecting","tst:a"))
    }

    test(s"$desc : should not allow bad xml on POST /a/b") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("xpecting","tst:e"))
    }

  }


  def mixedHeaderDisabledHeaderTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST=foo") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST=baz, bing") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST=baz, bing (header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("baz;q=1.0", "bing;q=0.5"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST=baz, bing (multiple header param)") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("baz;q=1.0;rockit=true", "bing;q=0.5;rockit=false"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow POST /a/b if X-TESTO=foo") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO=baz, bing") {
      validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }
  }

  def mixedHeaderNegHeaderTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should not allow PUT /a/b with no headers") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

  }


  def mixedHeaderNegHeaderTestsAll (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should not allow PUT /a/b with no headers") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","bing!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","9999-99-99"))
    }

  }


  def mixedHeaderMsgNegHeaderTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should not allow PUT /a/b with no headers") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

  }

def mixedHeaderMsgNegHeaderTestsAll (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should not allow PUT /a/b with no headers") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


  }



  def mixedHeaderNegHeaderNoRepeatTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should not allow PUT /a/b with no headers") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","Header","1 and only 1"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","Header", "1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","Header", "1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

  }

  def mixedHeaderDefaultHeaderTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

  }

  def mixedHeaderDefaultHeaderTestsAll (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","bing!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","9999-99-99"))
    }


  }


  def mixedHeaderMsgDefaultHeaderTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

  }


  def mixedHeaderMsgDefaultHeaderTestsAll (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

  }



  def mixedHeaderDefaultHeaderNoRepeatTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","Header", "1 and only 1"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow PUT /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }


    test(s"$desc : should not allow PUT /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain), 400,
                       List("X-TESTO","Header", "1 and only 1"))
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","Header", "1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

  }

  def mixedReqHeaderNegHeaderTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

  }


  def mixedReqHeaderNegHeaderTestsAll (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

     test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","bing!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","9999-99-99"))
    }

  }


  def mixedReqHeaderMsgNegHeaderTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

  }


  def mixedReqHeaderMsgNegHeaderTestsAll (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 401,
                                            List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


  }


  def mixedReqHeaderNegHeaderNoRepeatTests (desc : String, validator : Validator) : Unit = {


    test(s"$desc : should allow PUT /a/b with no headers") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map[String,List[String]]()), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST is not set") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should not allow POST /a/b with no headers") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map[String,List[String]]()), response, chain), 400,
                                            List("X-TEST","header"))
    }


    test(s"$desc : should not allow POST /a/b if X-TESTO is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST is not set") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","date"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header", "1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

  }


  def mixedReqHeaderDefaultHeaderTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

  }

  def mixedReqHeaderDefaultHeaderTestsAll (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","bing!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header","9999-99-99"))
    }


  }




  def mixedReqHeaderMsgDefaultHeaderTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

  }


  def mixedReqHeaderMsgDefaultHeaderTestsAll (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("Bad Testo"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, bing!") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","bing!"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }


    test(s"$desc : should not allow POST /a/b if X-TEST=foo!, 1975-07-08, 9999-99-99") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!","1975-07-08","9999-99-99"), "X-TESTO"->List("23"))), response, chain), 401,
                       List("No!"))
    }

  }


  def mixedReqHeaderDefaultHeaderNoRepeatTests (desc : String, validator : Validator) : Unit = {

    test(s"$desc : should allow PUT /a/b with no headers, default X-TEST and X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO is not set, default value for X-TESTO should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == Nil)
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : allow PUT /a/b if X-TEST is not set, default vaule of X-TEST should not be set") {
      val req = request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                        Map("X-TESTO"->List("23")))
      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == Nil)
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TEST=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain)
    }


    test(s"$desc : should allow PUT /a/b if X-TESTO=foo") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=baz, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing"))), response, chain)
    }

    test(s"$desc : should allow PUT /a/b if X-TESTO=23, bing") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodXML_XSD2, false,
                                 Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing"))), response, chain)
    }

    test(s"$desc : should allow POST /a/b with no headers, default X-TEST and X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map[String,List[String]]())

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })
      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TESTO is not set, default value for X-TESTO should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TEST"->List("bar!")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("bar!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("42"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should allow POST /a/b if X-TEST is not set, default value for X-TEST should be set") {
      val req = request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                        Map("X-TESTO"->List("23")))

      req.setAttribute(ASSERT_FUNCTION, (csReq : CheckerServletRequest, csResp : CheckerServletResponse, res : Result) => {
        assert (csReq.getHeaders("X-TEST").toList == List("foo!"))
        assert (csReq.getHeaders("X-TESTO").toList == List("23"))
      })

      validator.validate(req, response, chain)
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TEST=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("baz", "bing"), "X-TESTO"->List("23"))), response, chain), 400,
                       List("X-TEST","date"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=foo") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("foo"))), response, chain), 400,
                       List("X-TESTO","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header", "1 and only 1"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TESTO","Header","1 and only 1"))
    }

  }

  val mixedHeadersDisabledCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersDisabledCase)


  val mixedHeadersDisabledMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                     List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersDisabledMixedCaseCase)



  val mixedHeadersMsgDisabledCase : TestCase = (resourceHeadersWADL,
                                                List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersMsgDisabledCase)


  val mixedHeadersMsgDisabledMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                                List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersMsgDisabledMixedCaseCase)



  val mixedHeadersAllMatchDisabledCase : TestCase = (resourceHeadersAllMatchWADL,
                                                     List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersAllMatchDisabledCase)


  val mixedHeadersAllMatchMsgDisabledCase : TestCase = (resourceHeadersAllMatchWADL,
                                                        List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersAllMatchMsgDisabledCase)




  val mixedHeadersDisabledNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                     List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersDisabledNoRepeatCase)


  val mixedHeadersMsgDisabledNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                                        List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersMsgDisabledNoRepeatCase)



  val mixedHeadersCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersConfig, checkHeadersConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTests))

  run(mixedHeadersCase)


  val mixedHeadersMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                     List(checkHeadersConfig, checkHeadersConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTests))

  run(mixedHeadersMixedCaseCase)



  val mixedHeadersAllCase : TestCase = (resourceHeadersAllMatchWADL,
                                     List(checkHeadersConfig, checkHeadersConfigDups),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTestsAll))

  run(mixedHeadersAllCase)


  val mixedHeadersDisableAnyCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersConfigDisableAnyMatch, checkHeadersConfigDupsDisableAnyMatch),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTestsAll))

  run(mixedHeadersDisableAnyCase)


  val mixedHeadersDisableAnyMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                     List(checkHeadersConfigDisableAnyMatch, checkHeadersConfigDupsDisableAnyMatch),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTestsAll))

  run(mixedHeadersDisableAnyMixedCaseCase)




  val mixedHeadersMsgCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTests))

  run(mixedHeadersMsgCase)


  val mixedHeadersMsgMixedCase : TestCase = (resourceHeadersMixedCaseWADL,
                                     List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTests))

  run(mixedHeadersMsgMixedCase)



  val mixedHeadersMsgAllCase : TestCase = (resourceHeadersAllMatchWADL,
                                     List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTestsAll))

  run(mixedHeadersMsgAllCase)

  val mixedHeadersMsgDisableAnyCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersMsgConfigDisableAnyMatch, checkHeadersMsgConfigDupsDisableAnyMatch),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTestsAll))

  run(mixedHeadersMsgDisableAnyCase)


  val mixedHeadersMsgDisableAnyMixedCase : TestCase = (resourceHeadersMixedCaseWADL,
                                     List(checkHeadersMsgConfigDisableAnyMatch, checkHeadersMsgConfigDupsDisableAnyMatch),
                                     List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTestsAll))

  run(mixedHeadersMsgDisableAnyMixedCase)





  val mixedHeadersNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                     List(checkHeadersConfig, checkHeadersConfigDups),
                                     List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedHeaderNegHeaderNoRepeatTests))

  run(mixedHeadersNoRepeatCase)


  val mixedHeadersMsgNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                     List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                     List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedHeaderMsgNegHeaderTests))

  run(mixedHeadersMsgNoRepeatCase)



  val mixedHeadersDefaultCase : TestCase = (resourceHeadersWADL,
                                            List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                            List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTests))

  run(mixedHeadersDefaultCase)


  val mixedHeadersDefaultMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                            List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                            List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTests))

  run(mixedHeadersDefaultMixedCaseCase)



  val mixedHeadersDefaultAllCase : TestCase = (resourceHeadersAllMatchWADL,
                                            List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                            List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTestsAll))

  run(mixedHeadersDefaultAllCase)


  val mixedHeadersDefaultDisableAnyCase : TestCase = (resourceHeadersWADL,
                                            List(checkHeadersDefaultsConfigDisableAnyMatch, checkHeadersDefaultsConfigDupsDisableAnyMatch),
                                            List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTestsAll))

  run(mixedHeadersDefaultDisableAnyCase)


  val mixedHeadersDefaultDisableAnyMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                            List(checkHeadersDefaultsConfigDisableAnyMatch, checkHeadersDefaultsConfigDupsDisableAnyMatch),
                                            List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTestsAll))

  run(mixedHeadersDefaultDisableAnyMixedCaseCase)




  val mixedHeadersMsgDefaultCase : TestCase = (resourceHeadersWADL,
                                               List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTests))

  run(mixedHeadersMsgDefaultCase)


  val mixedHeadersMsgDefaultMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                               List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTests))

  run(mixedHeadersMsgDefaultMixedCaseCase)



  val mixedHeadersMsgDefaultAllCase : TestCase = (resourceHeadersAllMatchWADL,
                                               List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTestsAll))

  run(mixedHeadersMsgDefaultAllCase)


  val mixedHeadersMsgDefaultDisabledAnyCase : TestCase = (resourceHeadersWADL,
                                                          List(checkHeadersMsgDefaultsConfigDisableAnyMatch, checkHeadersMsgDefaultsConfigDupsDisableAnyMatch),
                                                          List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTestsAll))

  run(mixedHeadersMsgDefaultDisabledAnyCase)


  val mixedHeadersMsgDefaultDisabledAnyMixedCaseCase : TestCase = (resourceHeadersMixedCaseWADL,
                                                          List(checkHeadersMsgDefaultsConfigDisableAnyMatch, checkHeadersMsgDefaultsConfigDupsDisableAnyMatch),
                                                          List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTestsAll))

  run(mixedHeadersMsgDefaultDisabledAnyMixedCaseCase)



  val mixedHeadersDefaultNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                            List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                            List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderNoRepeatTests))

  run(mixedHeadersDefaultNoRepeatCase)


  val mixedHeadersMsgDefaultNoRepeatCase : TestCase = (resourceHeadersNoRepeatWADL,
                                                       List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                                       List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedHeaderMsgDefaultHeaderTests))

  run(mixedHeadersMsgDefaultNoRepeatCase)



  val mixedReqHeadersDisabledCase : TestCase = (requestHeadersWADL,
                                                List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersDisabledCase)


  val mixedReqHeadersDisabledMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                                List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersDisabledMixedCaseCase)




  val mixedReqHeadersMsgDisabledCase : TestCase = (requestHeadersWADL,
                                                   List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                   List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersMsgDisabledCase)


  val mixedReqHeadersMsgDisabledMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                                   List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                   List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersMsgDisabledMixedCaseCase)



  val mixedReqHeadersAllMatchDisabledCase : TestCase = (requestHeadersAllMatchWADL,
                                                        List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersAllMatchDisabledCase)



  val mixedReqHeadersAllMatchMsgDisabledCase : TestCase = (requestHeadersAllMatchWADL,
                                                           List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                           List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersAllMatchMsgDisabledCase)



  val mixedReqHeadersDisabledNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                                List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersDisabledNoRepeatCase)


  val mixedReqHeadersMsgDisabledNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                                           List(checkHeadersDisabledMsgConfig, checkHeadersDisabledMsgConfigDups),
                                                           List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersMsgDisabledNoRepeatCase)



  val mixedReqHeadersCase : TestCase = (requestHeadersWADL,
                                        List(checkHeadersConfig, checkHeadersConfigDups),
                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderTests))

  run(mixedReqHeadersCase)


  val mixedReqHeadersMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                        List(checkHeadersConfig, checkHeadersConfigDups),
                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderTests))

  run(mixedReqHeadersMixedCaseCase)


  val mixedReqHeadersAllCase : TestCase = (requestHeadersAllMatchWADL,
                                        List(checkHeadersConfig, checkHeadersConfigDups),
                                        List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests,mixedReqHeaderNegHeaderTestsAll))

  run(mixedReqHeadersAllCase)


  val mixedReqHeadersDisableAnyCase : TestCase = (requestHeadersWADL,
                                        List(checkHeadersConfigDisableAnyMatch, checkHeadersConfigDupsDisableAnyMatch),
                                        List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderTestsAll))

  run(mixedReqHeadersDisableAnyCase)


  val mixedReqHeadersDisableAnyMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                        List(checkHeadersConfigDisableAnyMatch, checkHeadersConfigDupsDisableAnyMatch),
                                        List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderTestsAll))

  run(mixedReqHeadersDisableAnyMixedCaseCase)



  val mixedReqHeadersMsgCase : TestCase = (requestHeadersWADL,
                                           List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                           List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTests))

  run(mixedReqHeadersMsgCase)


  val mixedReqHeadersMsgMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                           List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                           List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTests))

  run(mixedReqHeadersMsgMixedCaseCase)



  val mixedReqHeadersMsgAllCase : TestCase = (requestHeadersAllMatchWADL,
                                           List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                           List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTestsAll))

  run(mixedReqHeadersMsgAllCase)

  val mixedReqHeadersMsgDisableAnyCase : TestCase = (requestHeadersWADL,
                                           List(checkHeadersMsgConfigDisableAnyMatch, checkHeadersMsgConfigDupsDisableAnyMatch),
                                           List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTestsAll))

  run(mixedReqHeadersMsgDisableAnyCase)


  val mixedReqHeadersMsgDisableAnyMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                           List(checkHeadersMsgConfigDisableAnyMatch, checkHeadersMsgConfigDupsDisableAnyMatch),
                                           List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTestsAll))

  run(mixedReqHeadersMsgDisableAnyMixedCaseCase)




  val mixedReqHeadersNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                        List(checkHeadersConfig, checkHeadersConfigDups),
                                        List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderNoRepeatTests))

  run(mixedReqHeadersNoRepeatCase)


  val mixedReqHeadersMsgNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                                   List(checkHeadersMsgConfig, checkHeadersMsgConfigDups),
                                                   List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgNegHeaderTests))

  run(mixedReqHeadersMsgNoRepeatCase)



  val mixedReqHeadersDefaultCase : TestCase = (requestHeadersWADL,
                                               List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTests))

  run(mixedReqHeadersDefaultCase)


  val mixedReqHeadersDefaultMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                               List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTests))

  run(mixedReqHeadersDefaultMixedCaseCase)



  val mixedReqHeadersDefaultAllCase : TestCase = (requestHeadersAllMatchWADL,
                                               List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTestsAll))

  run(mixedReqHeadersDefaultAllCase)


  val mixedReqHeadersDefaultAnyCase : TestCase = (requestHeadersWADL,
                                               List(checkHeadersDefaultsConfigDisableAnyMatch, checkHeadersDefaultsConfigDupsDisableAnyMatch),
                                               List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTestsAll))

  run(mixedReqHeadersDefaultAnyCase)



  val mixedReqHeadersDefaultAnyMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                               List(checkHeadersDefaultsConfigDisableAnyMatch, checkHeadersDefaultsConfigDupsDisableAnyMatch),
                                               List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTestsAll))

  run(mixedReqHeadersDefaultAnyMixedCaseCase)


  val mixedReqHeadersMsgDefaultCase : TestCase = (requestHeadersWADL,
                                                  List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                                  List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTests))

  run(mixedReqHeadersMsgDefaultCase)


  val mixedReqHeadersMsgDefaultMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                                  List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                                  List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTests))

  run(mixedReqHeadersMsgDefaultMixedCaseCase)


  val mixedReqHeadersMsgDefaultAllCase : TestCase = (requestHeadersAllMatchWADL,
                                                  List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                                  List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTestsAll))

  run(mixedReqHeadersMsgDefaultAllCase)


  val mixedReqHeadersMsgDefaultAnyCase : TestCase = (requestHeadersWADL,
                                                  List(checkHeadersMsgDefaultsConfigDisableAnyMatch, checkHeadersMsgDefaultsConfigDupsDisableAnyMatch),
                                                  List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTestsAll))

  run(mixedReqHeadersMsgDefaultAnyCase)



  val mixedReqHeadersMsgDefaultAnyMixedCaseCase : TestCase = (requestHeadersMixedCaseWADL,
                                                  List(checkHeadersMsgDefaultsConfigDisableAnyMatch, checkHeadersMsgDefaultsConfigDupsDisableAnyMatch),
                                                  List(mixedHeaderHappyPathTestsAllMatch, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTestsAll))

  run(mixedReqHeadersMsgDefaultAnyMixedCaseCase)



  val mixedReqHeadersDefaultNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                                       List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                                       List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderNoRepeatTests))

  run(mixedReqHeadersDefaultNoRepeatCase)

  val mixedReqHeadersMsgDefaultNoRepeatCase : TestCase = (requestHeadersNoRepeatWADL,
                                                          List(checkHeadersMsgDefaultsConfig, checkHeadersMsgDefaultsConfigDups),
                                                          List(mixedHeaderHappyPathNoRepeatTests, mixedHeaderHappyNegTests, mixedReqHeaderMsgDefaultHeaderTests))

  run(mixedReqHeadersMsgDefaultNoRepeatCase)
}
