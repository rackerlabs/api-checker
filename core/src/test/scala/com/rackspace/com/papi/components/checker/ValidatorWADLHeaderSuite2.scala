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

  val checkHeadersDisabledConfig : CaseConfig = ("check headers disabled", TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, false))
  val checkHeadersDisabledConfigDups : CaseConfig = ("check headers disabled and remove dups enabled", TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, false))
  val checkHeadersConfig : CaseConfig = ("check headers enabled", TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true))
  val checkHeadersConfigDups : CaseConfig = ("check headers and remove dups enabled", TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true))
  val checkHeadersDefaultsConfig : CaseConfig = ("check headers and param defaults enabled", { val t = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", false, true); t.setParamDefaults = true; t })
  val checkHeadersDefaultsConfigDups : CaseConfig = ("check headers, param defaults,  and remove dups enabled", { val t = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true); t.setParamDefaults = true; t})

  val resourceHeadersWADL : TestWADL = ("WADL with mixed same name headers at the resource level",
                                            <application xmlns="http://wadl.dev.java.net/2009/02"
                                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
                                          <resources base="https://test.api.openstack.com">
                                      <resource path="/a/b">
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" default="foo!"/>
                                        <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true"/>
                                        <param name="X-TEST" style="header" type="xsd:dateTime" required="true"/>
                                        <param name="X-TESTO" style="header" type="xsd:int" required="true" default="42"/>
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
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" default="foo!"/>
                                             <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true"/>
                                             <param name="X-TEST" style="header" type="xsd:dateTime" required="true"/>
                                             <param name="X-TESTO" style="header" type="xsd:int" required="true" default="42"/>
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
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
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
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
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
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
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
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=baz, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("baz", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
    }

    test(s"$desc : should not allow POST /a/b if X-TESTO=23, bing") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/xml", goodXML_XSD1, false,
                                                    Map("X-TEST"->List("foo!"), "X-TESTO"->List("23", "bing!"))), response, chain), 400,
                       List("X-TEST","header"))
    }

  }



  val mixedHeadersDisabledCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedHeadersDisabledCase)

  val mixedHeadersCase : TestCase = (resourceHeadersWADL,
                                     List(checkHeadersConfig, checkHeadersConfigDups),
                                     List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderNegHeaderTests))

  run(mixedHeadersCase)

  val mixedHeadersDefaultCase : TestCase = (resourceHeadersWADL,
                                            List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                            List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDefaultHeaderTests))

  run(mixedHeadersDefaultCase)

  val mixedReqHeadersDisabledCase : TestCase = (requestHeadersWADL,
                                                List(checkHeadersDisabledConfig, checkHeadersDisabledConfigDups),
                                                List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedHeaderDisabledHeaderTests))

  run(mixedReqHeadersDisabledCase)


  val mixedReqHeadersCase : TestCase = (requestHeadersWADL,
                                        List(checkHeadersConfig, checkHeadersConfigDups),
                                        List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderNegHeaderTests))

  run(mixedReqHeadersCase)

  val mixedReqHeadersDefaultCase : TestCase = (requestHeadersWADL,
                                               List(checkHeadersDefaultsConfig, checkHeadersDefaultsConfigDups),
                                               List(mixedHeaderHappyPathTests, mixedHeaderHappyNegTests, mixedReqHeaderDefaultHeaderTests))

  run(mixedReqHeadersDefaultCase)
}
