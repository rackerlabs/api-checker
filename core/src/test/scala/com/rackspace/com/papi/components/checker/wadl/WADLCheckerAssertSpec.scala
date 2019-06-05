/***
 *   Copyright 2014 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.{LogAssertions, TestConfig}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

//
//  Tests to make sure that valid checker assertions actually fire
//

@RunWith(classOf[JUnitRunner])
class WADLCheckerAssertSpec extends BaseCheckerSpec with LogAssertions {

  val creatorString = {
    val title = getClass.getPackage.getImplementationTitle
    val version = getClass.getPackage.getImplementationVersion
    s"$title ($version)"
  }

  val validIn = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)

  val config = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c
  }

  val configWellFormed = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c
  }

  val configPlainParams = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c
  }

  val configHeaders = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.checkHeaders = true
    c
  }

  val configPreprocess = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.checkHeaders = true
    c.enablePreProcessExtension = true
    c
  }

  val configGrammar = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.checkHeaders = true
    c.enablePreProcessExtension = true
    c.checkXSDGrammar = true
    c.checkJSONGrammar = true
    c
  }

val configAdvance = {
    val c = TestConfig()
    c.validateChecker = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.checkElements = true
    c.checkHeaders = true
    c.enablePreProcessExtension = true
    c.checkXSDGrammar = true
    c.checkJSONGrammar = true
    c.preserveRequestBody = true
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.joinXPathChecks = true
    c.removeDups = true
    c
  }

  feature ("The WADLCheckerBuilder should reject documents which invalidate checker asserts") {

    info("As a developer")
    info("I want to make sure that checker documents are valid before they are loaded")
    info("so that I can catch errors early")


    scenario ("Checker with missing URL_FAIL step (@notMatch)") {
      Given("A checker with a missing URL_FAIL step (@notMatch)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "Checker error")
      assert(checkerLog, "d47e4")
      assert(checkerLog, "URL_FAIL")
      assert(checkerLog, "@notMatch")
    }


    scenario ("Checker with missing URL_FAIL step") {
      Given("A checker with a missing URL_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e6")
      assert(checkerLog, "descendant")
      assert(checkerLog, "URL_FAIL")
      assert(checkerLog, "URLXSD")
      assert(checkerLog, "URL")
    }

    scenario ("Checker with missing URL_FAIL step (on URLXSD)") {
      Given("A checker with a missing URL_FAIL step (on URLXSD)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T09:21:44.245-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test2.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e10u" notMatch="again" priority="10055"/>
  <step id="SA" type="ACCEPT" priority="100005"/>
  <step type="METHOD_FAIL" id="d55e8m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e10 d47e15 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 SE1"/>
  <step type="URLXSD" id="d47e7" match="xs:dateTime" label="time" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e10" match="other" next="d47e12 d47e11 d55e8m"/>
  <step type="METHOD" id="d47e11" match="GET" next="SA"/>
  <step type="URL" id="d47e12" match="(?s).*" label="stuff" next="d47e14 SE1 d55e10u"/>
  <step type="URL" id="d47e14" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e15" match="resource" next="d47e16 d47e17 d55e12m SE0"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="METHOD" id="d47e17" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "Checker error")
      assert(checkerLog, "d47e7")
      assert(checkerLog, "URL_FAIL")
      assert(checkerLog, "@notType")
    }

    scenario ("Checker with a mismatch URL_FAIL step (on URLXSD)") {
      Given("A checker with a mismatch URL_FAIL step (on URLXSD)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T09:21:44.245-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test2.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step xmlns:xs="http://fake/xs/namespace" type="URL_FAIL" id="d55e5u" notTypes="xs:dateTime" priority="10054"/>
  <step type="URL_FAIL" id="d55e10u" notMatch="again" priority="10055"/>
  <step id="SA" type="ACCEPT" priority="100005"/>
  <step type="METHOD_FAIL" id="d55e8m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e10 d47e15 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 SE1 d55e5u"/>
  <step type="URLXSD" id="d47e7" match="xs:dateTime" label="time" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e10" match="other" next="d47e12 d47e11 d55e8m"/>
  <step type="METHOD" id="d47e11" match="GET" next="SA"/>
  <step type="URL" id="d47e12" match="(?s).*" label="stuff" next="d47e14 SE1 d55e10u"/>
  <step type="URL" id="d47e14" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e15" match="resource" next="d47e16 d47e17 d55e12m SE0"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="METHOD" id="d47e17" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URLXSD")
      assert(checkerLog, "xs:dateTime")
    }

    scenario ("Checker with a mismatch URLXSD step") {
      Given("A checker with a mismatch URLXSD step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T09:21:44.245-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test2.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e5u" notTypes="xs:dateTime" priority="10054"/>
  <step type="URL_FAIL" id="d55e10u" notMatch="again" priority="10055"/>
  <step id="SA" type="ACCEPT" priority="100005"/>
  <step type="METHOD_FAIL" id="d55e8m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e10 d47e15 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 SE1 d55e5u"/>
  <step xmlns:xs="http://fake/xs/namespace" type="URLXSD" id="d47e7" match="xs:dateTime" label="time" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e10" match="other" next="d47e12 d47e11 d55e8m"/>
  <step type="METHOD" id="d47e11" match="GET" next="SA"/>
  <step type="URL" id="d47e12" match="(?s).*" label="stuff" next="d47e14 SE1 d55e10u"/>
  <step type="URL" id="d47e14" match="again" next="SE1 SE0"/>
  <step type="URL" id="d47e15" match="resource" next="d47e16 d47e17 d55e12m SE0"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="METHOD" id="d47e17" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URLXSD")
      assert(checkerLog, "xs:dateTime")
    }

    scenario ("Checker with a missing URL_FAIL step (mixed)") {
      Given("A checker with a missing URL_FAIL step (mixed)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:26:11.467-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test3.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e14 d47e23 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e10 SE1"/>
  <step type="URLXSD" id="d47e10" match="xs:dateTime" label="time" next="d47e12 SE1 d55e9u"/>
  <step type="URL" id="d47e7" match="stuff" next="d47e8 SE1 d55e6u"/>
  <step type="URL" id="d47e8" match="again" next="d47e9 d55e7m SE0"/>
  <step type="METHOD" id="d47e9" match="POST" next="SA"/>
  <step type="URL" id="d47e12" match="again" next="d47e13 d55e10m SE0"/>
  <step type="METHOD" id="d47e13" match="POST" next="SA"/>
  <step type="URL" id="d47e14" match="other" next="d47e16 d47e19 d47e15 d55e12m"/>
  <step type="METHOD" id="d47e15" match="GET" next="SA"/>
  <step type="URL" id="d47e16" match="foo" next="d47e17 SE1 d55e15u"/>
  <step type="URL" id="d47e17" match="again" next="d47e18 d55e16m SE0"/>
  <step type="METHOD" id="d47e18" match="POST" next="SA"/>
  <step type="URL" id="d47e19" match="(?s).*" label="stuff" next="d47e21 SE1 d55e18u"/>
  <step type="URL" id="d47e21" match="again" next="d47e22 d55e19m SE0"/>
  <step type="METHOD" id="d47e22" match="GET" next="SA"/>
  <step type="URL" id="d47e23" match="resource" next="d47e24 d47e25 d55e21m SE0"/>
  <step type="METHOD" id="d47e24" match="GET" next="SA"/>
  <step type="METHOD" id="d47e25" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e10")
      assert(checkerLog, "URL_FAIL")
      assert(checkerLog, "@notType")
      assert(checkerLog, "xs:dateTime")
    }

    scenario ("Checker with a mismatched URL_FAIL step (mixed, URL)") {
      Given("A checker with a mismatched URL_FAIL step (mixed, URL)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:26:11.467-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test3.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e5u" notMatch="stuf" notTypes="xs:dateTime" priority="10104"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e14 d47e23 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e10 SE1 d55e5u"/>
  <step type="URL" id="d47e7" match="stuff" next="d47e8 SE1 d55e6u"/>
  <step type="URL" id="d47e8" match="again" next="d47e9 d55e7m SE0"/>
  <step type="METHOD" id="d47e9" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e10" match="xs:dateTime" label="time" next="d47e12 SE1 d55e9u"/>
  <step type="URL" id="d47e12" match="again" next="d47e13 d55e10m SE0"/>
  <step type="METHOD" id="d47e13" match="POST" next="SA"/>
  <step type="URL" id="d47e14" match="other" next="d47e16 d47e19 d47e15 d55e12m"/>
  <step type="METHOD" id="d47e15" match="GET" next="SA"/>
  <step type="URL" id="d47e16" match="foo" next="d47e17 SE1 d55e15u"/>
  <step type="URL" id="d47e17" match="again" next="d47e18 d55e16m SE0"/>
  <step type="METHOD" id="d47e18" match="POST" next="SA"/>
  <step type="URL" id="d47e19" match="(?s).*" label="stuff" next="d47e21 SE1 d55e18u"/>
  <step type="URL" id="d47e21" match="again" next="d47e22 d55e19m SE0"/>
  <step type="METHOD" id="d47e22" match="GET" next="SA"/>
  <step type="URL" id="d47e23" match="resource" next="d47e24 d47e25 d55e21m SE0"/>
  <step type="METHOD" id="d47e24" match="GET" next="SA"/>
  <step type="METHOD" id="d47e25" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URL")
      assert(checkerLog, "stuf")
    }

    scenario ("Checker with a mismatched URL_FAIL step (mixed, URLXSD)") {
      Given("A checker with a mismatched URL_FAIL step (mixed, URLXSD)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:26:11.467-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test3.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e5u" notMatch="stuff" notTypes="xs:dateTim" priority="10104"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e14 d47e23 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e10 SE1 d55e5u"/>
  <step type="URL" id="d47e7" match="stuff" next="d47e8 SE1 d55e6u"/>
  <step type="URL" id="d47e8" match="again" next="d47e9 d55e7m SE0"/>
  <step type="METHOD" id="d47e9" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e10" match="xs:dateTime" label="time" next="d47e12 SE1 d55e9u"/>
  <step type="URL" id="d47e12" match="again" next="d47e13 d55e10m SE0"/>
  <step type="METHOD" id="d47e13" match="POST" next="SA"/>
  <step type="URL" id="d47e14" match="other" next="d47e16 d47e19 d47e15 d55e12m"/>
  <step type="METHOD" id="d47e15" match="GET" next="SA"/>
  <step type="URL" id="d47e16" match="foo" next="d47e17 SE1 d55e15u"/>
  <step type="URL" id="d47e17" match="again" next="d47e18 d55e16m SE0"/>
  <step type="METHOD" id="d47e18" match="POST" next="SA"/>
  <step type="URL" id="d47e19" match="(?s).*" label="stuff" next="d47e21 SE1 d55e18u"/>
  <step type="URL" id="d47e21" match="again" next="d47e22 d55e19m SE0"/>
  <step type="METHOD" id="d47e22" match="GET" next="SA"/>
  <step type="URL" id="d47e23" match="resource" next="d47e24 d47e25 d55e21m SE0"/>
  <step type="METHOD" id="d47e24" match="GET" next="SA"/>
  <step type="METHOD" id="d47e25" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URLXSD")
      assert(checkerLog, "xs:dateTim")
    }

    scenario ("Checker with a mismatched URL step (mixed)") {
      Given("A checker with a mismatched URL step (mixed)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:26:11.467-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test3.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e5u" notMatch="stuff" notTypes="xs:dateTime" priority="10104"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e14 d47e23 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e10 SE1 d55e5u"/>
  <step type="URL" id="d47e7" match="stuf" next="d47e8 SE1 d55e6u"/>
  <step type="URL" id="d47e8" match="again" next="d47e9 d55e7m SE0"/>
  <step type="METHOD" id="d47e9" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e10" match="xs:dateTime" label="time" next="d47e12 SE1 d55e9u"/>
  <step type="URL" id="d47e12" match="again" next="d47e13 d55e10m SE0"/>
  <step type="METHOD" id="d47e13" match="POST" next="SA"/>
  <step type="URL" id="d47e14" match="other" next="d47e16 d47e19 d47e15 d55e12m"/>
  <step type="METHOD" id="d47e15" match="GET" next="SA"/>
  <step type="URL" id="d47e16" match="foo" next="d47e17 SE1 d55e15u"/>
  <step type="URL" id="d47e17" match="again" next="d47e18 d55e16m SE0"/>
  <step type="METHOD" id="d47e18" match="POST" next="SA"/>
  <step type="URL" id="d47e19" match="(?s).*" label="stuff" next="d47e21 SE1 d55e18u"/>
  <step type="URL" id="d47e21" match="again" next="d47e22 d55e19m SE0"/>
  <step type="METHOD" id="d47e22" match="GET" next="SA"/>
  <step type="URL" id="d47e23" match="resource" next="d47e24 d47e25 d55e21m SE0"/>
  <step type="METHOD" id="d47e24" match="GET" next="SA"/>
  <step type="METHOD" id="d47e25" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URL")
      assert(checkerLog, "stuf")
    }

    scenario ("Checker with a mismatched URLXSD step (mixed)") {
      Given("A checker with a mismatched URLXSD step (mixed)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:26:11.467-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test3.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="URL_FAIL" id="d55e5u" notMatch="stuff" notTypes="xs:dateTime" priority="10104"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e14 d47e23 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e10 SE1 d55e5u"/>
  <step type="URL" id="d47e7" match="stuff" next="d47e8 SE1 d55e6u"/>
  <step type="URL" id="d47e8" match="again" next="d47e9 d55e7m SE0"/>
  <step type="METHOD" id="d47e9" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e10" match="xs:dateTim" label="time" next="d47e12 SE1 d55e9u"/>
  <step type="URL" id="d47e12" match="again" next="d47e13 d55e10m SE0"/>
  <step type="METHOD" id="d47e13" match="POST" next="SA"/>
  <step type="URL" id="d47e14" match="other" next="d47e16 d47e19 d47e15 d55e12m"/>
  <step type="METHOD" id="d47e15" match="GET" next="SA"/>
  <step type="URL" id="d47e16" match="foo" next="d47e17 SE1 d55e15u"/>
  <step type="URL" id="d47e17" match="again" next="d47e18 d55e16m SE0"/>
  <step type="METHOD" id="d47e18" match="POST" next="SA"/>
  <step type="URL" id="d47e19" match="(?s).*" label="stuff" next="d47e21 SE1 d55e18u"/>
  <step type="URL" id="d47e21" match="again" next="d47e22 d55e19m SE0"/>
  <step type="METHOD" id="d47e22" match="GET" next="SA"/>
  <step type="URL" id="d47e23" match="resource" next="d47e24 d47e25 d55e21m SE0"/>
  <step type="METHOD" id="d47e24" match="GET" next="SA"/>
  <step type="METHOD" id="d47e25" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e5u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "URLXSD")
      assert(checkerLog, "xs:dateTim")
    }

    scenario ("Checker with missing METHOD_FAIL step (at start)") {
      Given("A checker with a missing METHOD_FAIL step (at start)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "S0")
      assert(checkerLog, "proceeded")
      assert(checkerLog, "METHOD_FAIL")
    }

    scenario ("Checker with missing METHOD_FAIL step (@notMatch)") {
      Given("A checker with a missing METHOD_FAIL step (@notMatch)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "Checker error")
      assert(checkerLog, "d47e9")
      assert(checkerLog, "METHOD_FAIL")
      assert(checkerLog, "@notMatch")
    }


    scenario ("Checker with missing METHOD_FAIL step") {
      Given("A checker with a missing METHOD_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e5")
      assert(checkerLog, "proceeded")
      assert(checkerLog, "METHOD_FAIL")
    }

    scenario ("Checker with a bad type of METHOD_FAIL step") {
      Given("A checker with a bad type of METHOD_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 SE1 SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "Checker error")
      assert(checkerLog, "reference SE1")
      assert(checkerLog, "but also reference")
      assert(checkerLog, "METHOD")
    }

    scenario ("Checker with a bad METHOD_FAIL step") {
      Given("A checker with a bad METHOD_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|PUT" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e7m")
      assert(checkerLog, "sibling")
      assert(checkerLog, "PUT")
      assert(checkerLog, "METHOD")
    }


    scenario ("Checker with a mismatch METHOD_FAIL step") {
      Given("A checker with a mismatch METHOD_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="PUT" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e7m")
      assert(checkerLog, "sibling")
      assert(checkerLog, "POST")
      assert(checkerLog, "METHOD")
    }

    scenario ("Checker with a bad type of URL_FAIL step") {
      Given("A checker with a bad type of URL_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 SE0"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "Checker error")
      assert(checkerLog, "reference SE0")
      assert(checkerLog, "but also reference")
      assert(checkerLog, "URL")
    }

    scenario ("Checker with a bad URL_FAIL step") {
      Given("A checker with a bad URL_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|res" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="resource" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e4u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "res")
      assert(checkerLog, "URL")
    }


    scenario ("Checker with a mismatch URL_FAIL step") {
      Given("A checker with a mismatch URL_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                  xmlns:json="http://json-schema.org/schema#"
                  xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.rackspace.com/repose/wadl/checker
                                      file:///Users/jorgew/projects/api-checker/core/src/main/resources/xsd/checker.xsd">
    <meta>
        <built-by>jorgew</built-by>
        <created-by>{creatorString}</created-by>
        <created-on>2015-03-07T13:17:18.471-06:00</created-on>
        <config option="enableIgnoreJSONSchemaExtension" value="true"/>
        <config option="enableMessageExtension" value="true"/>
        <config option="joinXPathChecks" value="false"/>
        <config option="doXSDGrammarTransform" value="false"/>
        <config option="enablePreProcessExtension" value="true"/>
        <config option="removeDups" value="false"/>
        <config option="checkXSDGrammar" value="false"/>
        <config option="xpathVersion" value="1"/>
        <config option="checkPlainParams" value="false"/>
        <config option="checkWellFormed" value="false"/>
        <config option="enableIgnoreXSDExtension" value="true"/>
        <config option="checkJSONGrammar" value="false"/>
        <config option="checkElements" value="false"/>
        <config option="preserveRequestBody" value="false"/>
        <config option="checkHeaders" value="false"/>
        <config option="enableRaxRolesExtension" value="false"/>
        <config option="maskRaxRoles403" value="false"/>
    </meta>
    <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
    <step id="SA" type="ACCEPT" priority="100005"/>
    <step type="METHOD_FAIL" id="d55e5m" notMatch="GET" priority="20054"/>
    <step id="SE0" type="URL_FAIL" priority="10004"/>
    <step type="METHOD_FAIL" id="d55e7m" notMatch="GET|POST" priority="20104"/>
    <step id="SE1" type="METHOD_FAIL" priority="20003"/>
    <step type="URL_FAIL" id="d55e4u" notMatch="other|resource" priority="10103"/>
    <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
    <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
    <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
    <step type="URL" id="d47e5" match="to" next="d47e6 d47e8 SE1 d55e4u"/>
    <step type="URL" id="d47e6" match="other" next="d47e7 d55e5m SE0"/>
    <step type="METHOD" id="d47e7" match="GET" next="SA"/>
    <step type="URL" id="d47e8" match="res" next="d47e9 d47e10 d55e7m SE0"/>
    <step type="METHOD" id="d47e9" match="GET" next="SA"/>
    <step type="METHOD" id="d47e10" match="POST" next="SA"/>
    </checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d55e4u")
      assert(checkerLog, "sibling")
      assert(checkerLog, "res")
      assert(checkerLog, "URL")
    }

    scenario ("URL steps matching (?s).* should not require an URL_FAIL step") {
      Given("A checker with URL steps matching (?s).* and no sibling URL_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T14:47:23.43-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test4.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100007"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e19m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e18u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e24 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e20 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="SA"/>
  <step type="URL" id="d47e20" match="(?s).*" label="stuff" next="d47e22 SE1 d55e18u"/>
  <step type="URL" id="d47e22" match="again" next="d47e23 d55e19m SE0"/>
  <step type="METHOD" id="d47e23" match="GET" next="SA"/>
  <step type="URL" id="d47e24" match="resource" next="d47e25 d47e26 d55e21m SE0"/>
  <step type="METHOD" id="d47e25" match="GET" next="SA"/>
  <step type="METHOD" id="d47e26" match="POST" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        val checker = builder.build(in, config)
      }
      Then ("The checker not fail and no ERROR messages should be output")
      assertEmpty(checkerLog)
    }

    scenario ("Checker with a missing REQ_TYPE_FAIL step") {
      Given("A checker with a missing REQ_TYPE_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T15:13:18.196-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100008"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e20u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e20u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e21m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e23m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/json)(;.*)?" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e36")
      assert(checkerLog, "REQ_TYPE_FAIL")
      assert(checkerLog, "@notMatch")
    }

    scenario ("Checker with a mismatch REQ_TYPE_FAIL step") {
      Given("A checker with a mismatch REQ_TYPE_FAIL step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T15:13:18.196-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100008"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e20u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="REQ_TYPE_FAIL" id="d47e32rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/jsn)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e20u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e21m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e23m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38 d47e32rqt"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/json)(;.*)?" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e32rqt")
      assert(checkerLog, "sibling")
      assert(checkerLog, "(?i)(application/jsn)(;.*)?")
      assert(checkerLog, "REQ_TYPE")
    }

    scenario ("Checker with a mismatch REQ_TYPE step") {
      Given("A checker with a mismatch REQ_TYPE step")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-09T15:13:18.196-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100008"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e20u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="REQ_TYPE_FAIL" id="d47e32rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e20u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e21m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e23m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38 d47e32rqt"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="SA"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/jsn)(;.*)?" next="SA"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, config)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e32rqt")
      assert(checkerLog, "sibling")
      assert(checkerLog, "(?i)(application/json)(;.*)?")
      assert(checkerLog, "REQ_TYPE")
    }

    scenario ("Checker with a CONTENT_FAIL step in the wrong place") {
      Given("A checker with a CONTENT_FAIL step in the wrong place")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T10:06:35.287-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u d47e23WF"/>
  <step id="SA" type="ACCEPT" priority="100009"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="WELL_XML" id="d47e23W" priority="41008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e23WF" priority="40008"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e22u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="WELL_XML" id="d47e36W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e36WF" priority="40006"/>
  <step type="WELL_JSON" id="d47e38W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e38WF" priority="40006"/>
  <step type="REQ_TYPE_FAIL" id="d47e32rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e26m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="d47e23W d47e23WF"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e22u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e23m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e26m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38 d47e32rqt"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="d47e36W d47e36WF"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/json)(;.*)?" next="d47e38W d47e38WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configWellFormed)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e23WF")
      assert(checkerLog, "reference")
      assert(checkerLog, "WELL_XML or WELL_JSON or XSD or XPATH or XSL or HEADER or HEADERXSD or HEADER_ANY or HEADERXSD_ANY or HEADER_SINGLE or HEADERXSD_SINGLE or HEADER_ALL or JSON_SCHEMA")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (WELL_XML)") {
      Given("A checker with a missing CONTENT_FAIL step (WELL_XML)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T10:06:35.287-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100009"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="WELL_XML" id="d47e23W" priority="41008" next="SA"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e22u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="WELL_XML" id="d47e36W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e36WF" priority="40006"/>
  <step type="WELL_JSON" id="d47e38W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e38WF" priority="40006"/>
  <step type="REQ_TYPE_FAIL" id="d47e32rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e26m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="d47e23W"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e22u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e23m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e26m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38 d47e32rqt"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="d47e36W d47e36WF"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/json)(;.*)?" next="d47e38W d47e38WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configWellFormed)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e23W")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (WELL_JSON)") {
      Given("A checker with a missing CONTENT_FAIL step (WELL_JSON)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T10:06:35.287-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test5.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100009"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="WELL_XML" id="d47e23W" priority="41008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e23WF" priority="40008"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e23m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e22u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="WELL_XML" id="d47e36W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e36WF" priority="40006"/>
  <step type="WELL_JSON" id="d47e38W" priority="41006" next="SA"/>
  <step type="REQ_TYPE_FAIL" id="d47e32rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e26m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e30 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e26 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="d47e23W d47e23WF"/>
  <step type="URL" id="d47e26" match="(?s).*" label="stuff" next="d47e28 SE1 d55e22u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d55e23m SE0"/>
  <step type="METHOD" id="d47e29" match="GET" next="SA"/>
  <step type="URL" id="d47e30" match="resource" next="d47e31 d47e32 d55e26m SE0"/>
  <step type="METHOD" id="d47e31" match="GET" next="SA"/>
  <step type="METHOD" id="d47e32" match="POST" next="d47e36 d47e38 d47e32rqt"/>
  <step type="REQ_TYPE" id="d47e36" match="(?i)(application/xml)(;.*)?" next="d47e36W d47e36WF"/>
  <step type="REQ_TYPE" id="d47e38" match="(?i)(application/json)(;.*)?" next="d47e38W"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configWellFormed)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e38W")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (XPATH)") {
      Given("A checker with a missing CONTENT_FAIL step (XPATH)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T10:49:05.566-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test6.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100011"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="WELL_XML" id="d47e23W" priority="41008" next="d47e231XPTH d47e23WF"/>
  <step type="XPATH" id="d47e231XPTH" match="true()" priority="41009" next="d47e232XPTH d47e23WF"/>
  <step type="XPATH" id="d47e232XPTH" match="/" priority="41010" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e23WF" priority="40010"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e26m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e24u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="XPATH" id="d47e411XPTH" match="true()" priority="41007" next="SA"/>
  <step type="WELL_JSON" id="d47e46W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e46WF" priority="40006"/>
  <step type="REQ_TYPE_FAIL" id="d47e37rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e28m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e35 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e31 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="d47e23W d47e23WF"/>
  <step type="URL" id="d47e31" match="(?s).*" label="stuff" next="d47e33 SE1 d55e24u"/>
  <step type="URL" id="d47e33" match="again" next="d47e34 d55e26m SE0"/>
  <step type="METHOD" id="d47e34" match="GET" next="SA"/>
  <step type="URL" id="d47e35" match="resource" next="d47e36 d47e37 d55e28m SE0"/>
  <step type="METHOD" id="d47e36" match="GET" next="SA"/>
  <step type="METHOD" id="d47e37" match="POST" next="d47e41 d47e46 d47e37rqt"/>
  <step type="REQ_TYPE" id="d47e41" match="(?i)(application/xml)(;.*)?" next="d47e411XPTH"/>
  <step type="REQ_TYPE" id="d47e46" match="(?i)(application/json)(;.*)?" next="d47e46W d47e46WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configPlainParams)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e411XPTH")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a XPATH step with a bad parent") {
      Given("A checker with a XPATH step with a bad parent")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T10:49:05.566-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test6.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u d47e411XPTH d47e23WF"/>
  <step id="SA" type="ACCEPT" priority="100011"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10006"/>
  <step id="SE1" type="METHOD_FAIL" priority="20005"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step type="WELL_XML" id="d47e23W" priority="41008" next="d47e231XPTH d47e23WF"/>
  <step type="XPATH" id="d47e231XPTH" match="true()" priority="41009" next="d47e232XPTH d47e23WF"/>
  <step type="XPATH" id="d47e232XPTH" match="/" priority="41010" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e23WF" priority="40010"/>
  <step type="REQ_TYPE_FAIL" id="d47e19rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30007"/>
  <step type="METHOD_FAIL" id="d55e16m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e15u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e26m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e24u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step type="WELL_XML" id="d47e41W" priority="41006" next="d47e411XPTH d47e41WF"/>
  <step type="XPATH" id="d47e411XPTH" match="true()" priority="41007" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e41WF" priority="40007"/>
  <step type="WELL_JSON" id="d47e46W" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e46WF" priority="40006"/>
  <step type="REQ_TYPE_FAIL" id="d47e37rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30005"/>
  <step type="METHOD_FAIL" id="d55e28m" notMatch="GET|POST" priority="20104"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e35 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e17 d47e31 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="SA"/>
  <step type="URL" id="d47e17" match="foo" next="d47e18 SE1 d55e15u"/>
  <step type="URL" id="d47e18" match="again" next="d47e19 d55e16m SE0"/>
  <step type="METHOD" id="d47e19" match="POST" next="d47e23 d47e19rqt"/>
  <step type="REQ_TYPE" id="d47e23" match="(?i)(application/xml)(;.*)?" next="d47e23W d47e23WF"/>
  <step type="URL" id="d47e31" match="(?s).*" label="stuff" next="d47e33 SE1 d55e24u"/>
  <step type="URL" id="d47e33" match="again" next="d47e34 d55e26m SE0"/>
  <step type="METHOD" id="d47e34" match="GET" next="SA"/>
  <step type="URL" id="d47e35" match="resource" next="d47e36 d47e37 d55e28m SE0"/>
  <step type="METHOD" id="d47e36" match="GET" next="SA"/>
  <step type="METHOD" id="d47e37" match="POST" next="d47e41 d47e46 d47e37rqt"/>
  <step type="REQ_TYPE" id="d47e41" match="(?i)(application/xml)(;.*)?" next="d47e41W d47e41WF"/>
  <step type="REQ_TYPE" id="d47e46" match="(?i)(application/json)(;.*)?" next="d47e46W d47e46WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configPlainParams)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e411XPTH")
      assert(checkerLog, "parent")
      assert(checkerLog, "WELL_XML or XSL or XPATH")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (HEADER)") {
      Given("A checker with a missing CONTENT_FAIL step (HEADER)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T11:21:58.855-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test8.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100013"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e29" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e30 d55e21m SE0"/>
  <step type="WELL_XML" id="d47e34W" priority="41009" next="d47e340XPTH d47e34WF"/>
  <step type="XPATH" id="d47e340XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e341XPTH d47e34WF"/>
  <step type="XPATH" id="d47e341XPTH" match="true()" priority="41011" next="d47e342XPTH d47e34WF"/>
  <step type="XPATH" id="d47e342XPTH" match="/" priority="41012" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e34WF" priority="40012"/>
  <step type="REQ_TYPE_FAIL" id="d47e30rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e29HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e19u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e33m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e32u" notMatch="again" priority="10055"/>
  <step id="d47e20" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="SA"/>
  <step id="d47e22" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="SA"/>
  <step id="d47e24" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e20HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e47" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e48 d47e55 d55e36m SE0"/>
  <step id="d47e52" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e52HF" priority="40006"/>
  <step type="WELL_XML" id="d47e59W" priority="41007" next="d47e590XPTH d47e59WF"/>
  <step type="XPATH" id="d47e590XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e59WF" priority="40008"/>
  <step type="WELL_JSON" id="d47e61W" priority="41007" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e61WF" priority="40007"/>
  <step type="REQ_TYPE_FAIL" id="d47e55rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e36m" notMatch="GET|POST" priority="20105"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e46 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e27 d47e42 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="d47e20 d47e22 d47e24 d47e20HF"/>
  <step type="URL" id="d47e27" match="foo" next="d47e28 SE1 d55e19u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d47e29HF SE1 SE0"/>
  <step type="METHOD" id="d47e30" match="POST" next="d47e34 d47e30rqt"/>
  <step type="REQ_TYPE" id="d47e34" match="(?i)(application/xml)(;.*)?" next="d47e34W d47e34WF"/>
  <step type="URL" id="d47e42" match="(?s).*" label="stuff" next="d47e44 SE1 d55e32u"/>
  <step type="URL" id="d47e44" match="again" next="d47e45 d55e33m SE0"/>
  <step type="METHOD" id="d47e45" match="GET" next="SA"/>
  <step type="URL" id="d47e46" match="resource" next="d47e47 SE1"/>
  <step type="METHOD" id="d47e48" match="GET" next="d47e52 d47e52HF"/>
  <step type="METHOD" id="d47e55" match="POST" next="d47e59 d47e61 d47e55rqt"/>
  <step type="REQ_TYPE" id="d47e59" match="(?i)(application/xml)(;.*)?" next="d47e59W d47e59WF"/>
  <step type="REQ_TYPE" id="d47e61" match="(?i)(application/json)(;.*)?" next="d47e61W d47e61WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configHeaders)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e47")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (HEADERXSD)") {
      Given("A checker with a missing CONTENT_FAIL step (HEADERXSD)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T11:21:58.855-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test8.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100013"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e29" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e30 d55e21m SE0"/>
  <step type="WELL_XML" id="d47e34W" priority="41009" next="d47e340XPTH d47e34WF"/>
  <step type="XPATH" id="d47e340XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e341XPTH d47e34WF"/>
  <step type="XPATH" id="d47e341XPTH" match="true()" priority="41011" next="d47e342XPTH d47e34WF"/>
  <step type="XPATH" id="d47e342XPTH" match="/" priority="41012" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e34WF" priority="40012"/>
  <step type="REQ_TYPE_FAIL" id="d47e30rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="POST" priority="20057"/>
  <step type="URL_FAIL" id="d55e19u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e33m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e32u" notMatch="again" priority="10055"/>
  <step id="d47e20" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="SA"/>
  <step id="d47e22" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="SA"/>
  <step id="d47e24" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e20HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e47" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e48 d47e55 d55e36m SE0"/>
  <step id="d47e52" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e52HF" priority="40006"/>
  <step type="WELL_XML" id="d47e59W" priority="41007" next="d47e590XPTH d47e59WF"/>
  <step type="XPATH" id="d47e590XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e59WF" priority="40008"/>
  <step type="WELL_JSON" id="d47e61W" priority="41007" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e61WF" priority="40007"/>
  <step type="REQ_TYPE_FAIL" id="d47e55rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e36m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e47HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e46 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e27 d47e42 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="d47e20 d47e22 d47e24 d47e20HF"/>
  <step type="URL" id="d47e27" match="foo" next="d47e28 SE1 d55e19u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 SE1 SE0"/>
  <step type="METHOD" id="d47e30" match="POST" next="d47e34 d47e30rqt"/>
  <step type="REQ_TYPE" id="d47e34" match="(?i)(application/xml)(;.*)?" next="d47e34W d47e34WF"/>
  <step type="URL" id="d47e42" match="(?s).*" label="stuff" next="d47e44 SE1 d55e32u"/>
  <step type="URL" id="d47e44" match="again" next="d47e45 d55e33m SE0"/>
  <step type="METHOD" id="d47e45" match="GET" next="SA"/>
  <step type="URL" id="d47e46" match="resource" next="d47e47 d47e47HF SE1"/>
  <step type="METHOD" id="d47e48" match="GET" next="d47e52 d47e52HF"/>
  <step type="METHOD" id="d47e55" match="POST" next="d47e59 d47e61 d47e55rqt"/>
  <step type="REQ_TYPE" id="d47e59" match="(?i)(application/xml)(;.*)?" next="d47e59W d47e59WF"/>
  <step type="REQ_TYPE" id="d47e61" match="(?i)(application/json)(;.*)?" next="d47e61W d47e61WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configHeaders)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e29")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (HEADER_ANY)") {
      Given("A checker with a missing CONTENT_FAIL step (HEADER_ANY)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T11:21:58.855-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test8.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100013"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e29" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e30 d55e21m SE0"/>
  <step type="WELL_XML" id="d47e34W" priority="41009" next="d47e340XPTH d47e34WF"/>
  <step type="XPATH" id="d47e340XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e341XPTH d47e34WF"/>
  <step type="XPATH" id="d47e341XPTH" match="true()" priority="41011" next="d47e342XPTH d47e34WF"/>
  <step type="XPATH" id="d47e342XPTH" match="/" priority="41012" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e34WF" priority="40012"/>
  <step type="REQ_TYPE_FAIL" id="d47e30rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e21m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e29HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e19u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e33m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e32u" notMatch="again" priority="10055"/>
  <step id="d47e20" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="SA"/>
  <step id="d47e22" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="SA"/>
  <step id="d47e24" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="SA"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e47" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e48 d47e55 d55e36m SE0"/>
  <step id="d47e52" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e52HF" priority="40006"/>
  <step type="WELL_XML" id="d47e59W" priority="41007" next="d47e590XPTH d47e59WF"/>
  <step type="XPATH" id="d47e590XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e59WF" priority="40008"/>
  <step type="WELL_JSON" id="d47e61W" priority="41007" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e61WF" priority="40007"/>
  <step type="REQ_TYPE_FAIL" id="d47e55rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e36m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e47HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e46 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e27 d47e42 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="d47e20 d47e22 d47e24"/>
  <step type="URL" id="d47e27" match="foo" next="d47e28 SE1 d55e19u"/>
  <step type="URL" id="d47e28" match="again" next="d47e29 d47e29HF SE1 SE0"/>
  <step type="METHOD" id="d47e30" match="POST" next="d47e34 d47e30rqt"/>
  <step type="REQ_TYPE" id="d47e34" match="(?i)(application/xml)(;.*)?" next="d47e34W d47e34WF"/>
  <step type="URL" id="d47e42" match="(?s).*" label="stuff" next="d47e44 SE1 d55e32u"/>
  <step type="URL" id="d47e44" match="again" next="d47e45 d55e33m SE0"/>
  <step type="METHOD" id="d47e45" match="GET" next="SA"/>
  <step type="URL" id="d47e46" match="resource" next="d47e47 d47e47HF SE1"/>
  <step type="METHOD" id="d47e48" match="GET" next="d47e52 d47e52HF"/>
  <step type="METHOD" id="d47e55" match="POST" next="d47e59 d47e61 d47e55rqt"/>
  <step type="REQ_TYPE" id="d47e59" match="(?i)(application/xml)(;.*)?" next="d47e59W d47e59WF"/>
  <step type="REQ_TYPE" id="d47e61" match="(?i)(application/json)(;.*)?" next="d47e61W d47e61WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configHeaders)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e20")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (XSL)") {
      Given("A checker with a missing CONTENT_FAIL step (XSL)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T11:44:54.383-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test9.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e4 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100013"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="XSL" id="d47e761PPROC" version="2" priority="41009" next="SA">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e46" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e47 d55e37m SE0"/>
  <step type="WELL_XML" id="d47e51W" priority="41009" next="d47e510XPTH d47e51WF"/>
  <step type="XPATH" id="d47e510XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e511XPTH d47e51WF"/>
  <step type="XPATH" id="d47e511XPTH" match="true()" priority="41011" next="d47e512XPTH d47e51WF"/>
  <step type="XPATH" id="d47e512XPTH" match="/" priority="41012" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e51WF" priority="40012"/>
  <step type="REQ_TYPE_FAIL" id="d47e47rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e37m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e46HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e34u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e49m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e48u" notMatch="again" priority="10055"/>
  <step id="d47e20" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="d47e26 d47e16rqt"/>
  <step type="WELL_XML" id="d47e26W" priority="41007" next="d47e261PPROC"/>
  <step type="XSL" id="d47e261PPROC" version="2" priority="41008" next="SA">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="CONTENT_FAIL" id="d47e26WF" priority="40007"/>
  <step type="REQ_TYPE_FAIL" id="d47e16rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30006"/>
  <step id="d47e22" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="d47e26 d47e16rqt"/>
  <step id="d47e24" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="d47e26 d47e16rqt"/>
  <step type="CONTENT_FAIL" id="d47e20HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e64" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e65 d47e72 d55e52m SE0"/>
  <step id="d47e69" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e69HF" priority="40006"/>
  <step type="WELL_XML" id="d47e76W" priority="41007" next="d47e760XPTH"/>
  <step type="XPATH" id="d47e760XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="d47e761PPROC"/>
  <step type="WELL_JSON" id="d47e93W" priority="41007" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e93WF" priority="40007"/>
  <step type="REQ_TYPE_FAIL" id="d47e72rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e52m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e64HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e4" match="path" next="d47e5 SE1 d55e3u"/>
  <step type="URL" id="d47e5" match="to" next="d47e6 d47e15 d47e63 SE1 d55e4u"/>
  <step type="URL" id="d47e6" match="another" next="d47e7 d47e11 SE1"/>
  <step type="URL" id="d47e7" match="(?s).*" label="anything" next="d47e9 SE1 d55e6u"/>
  <step type="URL" id="d47e9" match="again" next="d47e10 d55e7m SE0"/>
  <step type="METHOD" id="d47e10" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e11" match="xs:dateTime" label="time" next="d47e13 SE1 d55e9u"/>
  <step type="URL" id="d47e13" match="again" next="d47e14 d55e10m SE0"/>
  <step type="METHOD" id="d47e14" match="POST" next="SA"/>
  <step type="URL" id="d47e15" match="other" next="d47e44 d47e59 d47e16 d55e12m"/>
  <step type="METHOD" id="d47e16" match="GET" next="d47e20 d47e22 d47e24 d47e20HF"/>
  <step type="REQ_TYPE" id="d47e26" match="(?i)(application/xml)(;.*)?" next="d47e26W d47e26WF"/>
  <step type="URL" id="d47e44" match="foo" next="d47e45 SE1 d55e34u"/>
  <step type="URL" id="d47e45" match="again" next="d47e46 d47e46HF SE1 SE0"/>
  <step type="METHOD" id="d47e47" match="POST" next="d47e51 d47e47rqt"/>
  <step type="REQ_TYPE" id="d47e51" match="(?i)(application/xml)(;.*)?" next="d47e51W d47e51WF"/>
  <step type="URL" id="d47e59" match="(?s).*" label="stuff" next="d47e61 SE1 d55e48u"/>
  <step type="URL" id="d47e61" match="again" next="d47e62 d55e49m SE0"/>
  <step type="METHOD" id="d47e62" match="GET" next="SA"/>
  <step type="URL" id="d47e63" match="resource" next="d47e64 d47e64HF SE1"/>
  <step type="METHOD" id="d47e65" match="GET" next="d47e69 d47e69HF"/>
  <step type="METHOD" id="d47e72" match="POST" next="d47e76 d47e93 d47e72rqt"/>
  <step type="REQ_TYPE" id="d47e76" match="(?i)(application/xml)(;.*)?" next="d47e76W"/>
  <step type="REQ_TYPE" id="d47e93" match="(?i)(application/json)(;.*)?" next="d47e93W d47e93WF"/>
</checker>
)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configPreprocess)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e76W")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (XSD)") {
      Given("A checker with a missing CONTENT_FAIL step (XSD)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T13:46:28.842-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test11.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ...
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" xmlns:xsd="http://www.w3.org/2001/XMLSchema" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="S0" type="START" next="d47e88 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100014"/>
  <step type="XSD" id="d47e135XSD" priority="45013" next="SA"/>
  <step type="XSD" id="d47e110XSD" priority="45009" next="SA"/>
  <step type="XSD" id="d47e160XSD" priority="45010" next="SA"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e130" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e131 d55e38m SE0"/>
  <step type="WELL_XML" id="d47e135W" priority="41009" next="d47e1350XPTH"/>
  <step type="XPATH" id="d47e1350XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e1351XPTH"/>
  <step type="XPATH" id="d47e1351XPTH" match="true()" priority="41011" next="d47e1352XPTH"/>
  <step type="XPATH" id="d47e1352XPTH" match="/" priority="41012" next="d47e135XSD"/>
  <step type="REQ_TYPE_FAIL" id="d47e131rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e38m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e130HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e36u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e51m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e50u" notMatch="again" priority="10055"/>
  <step id="d47e104" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="WELL_XML" id="d47e110W" priority="41007" next="d47e1101PPROC"/>
  <step type="XSL" id="d47e1101PPROC" version="2" priority="41008" next="d47e110XSD d47e110WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="CONTENT_FAIL" id="d47e110WF" priority="40009"/>
  <step type="REQ_TYPE_FAIL" id="d47e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30006"/>
  <step id="d47e106" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="d47e110 d47e100rqt"/>
  <step id="d47e108" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="CONTENT_FAIL" id="d47e104HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e148" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e149 d47e156 d55e54m SE0"/>
  <step id="d47e153" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e153HF" priority="40006"/>
  <step type="WELL_XML" id="d47e160W" priority="41007" next="d47e1600XPTH d47e160WF"/>
  <step type="XPATH" id="d47e1600XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="d47e1601PPROC"/>
  <step type="XSL" id="d47e1601PPROC" version="2" priority="41009" next="d47e160XSD d47e160WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="CONTENT_FAIL" id="d47e160WF" priority="40010"/>
  <step type="WELL_JSON" id="d47e177W" priority="41007" next="d47e177JSON d47e177WF"/>
  <step type="JSON_SCHEMA" id="d47e177JSON" priority="45008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e177WF" priority="40008"/>
  <step type="REQ_TYPE_FAIL" id="d47e156rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e54m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e148HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e88" match="path" next="d47e89 SE1 d55e3u"/>
  <step type="URL" id="d47e89" match="to" next="d47e90 d47e99 d47e147 SE1 d55e4u"/>
  <step type="URL" id="d47e90" match="another" next="d47e91 d47e95 SE1"/>
  <step type="URL" id="d47e91" match="(?s).*" label="anything" next="d47e93 SE1 d55e6u"/>
  <step type="URL" id="d47e93" match="again" next="d47e94 d55e7m SE0"/>
  <step type="METHOD" id="d47e94" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e95" match="xs:dateTime" label="time" next="d47e97 SE1 d55e9u"/>
  <step type="URL" id="d47e97" match="again" next="d47e98 d55e10m SE0"/>
  <step type="METHOD" id="d47e98" match="POST" next="SA"/>
  <step type="URL" id="d47e99" match="other" next="d47e128 d47e143 d47e100 d55e12m"/>
  <step type="METHOD" id="d47e100" match="GET" next="d47e104 d47e106 d47e108 d47e104HF"/>
  <step type="REQ_TYPE" id="d47e110" match="(?i)(application/xml)(;.*)?" next="d47e110W d47e110WF"/>
  <step type="URL" id="d47e128" match="foo" next="d47e129 SE1 d55e36u"/>
  <step type="URL" id="d47e129" match="again" next="d47e130 d47e130HF SE1 SE0"/>
  <step type="METHOD" id="d47e131" match="POST" next="d47e135 d47e131rqt"/>
  <step type="REQ_TYPE" id="d47e135" match="(?i)(application/xml)(;.*)?" next="d47e135W"/>
  <step type="URL" id="d47e143" match="(?s).*" label="stuff" next="d47e145 SE1 d55e50u"/>
  <step type="URL" id="d47e145" match="again" next="d47e146 d55e51m SE0"/>
  <step type="METHOD" id="d47e146" match="GET" next="SA"/>
  <step type="URL" id="d47e147" match="resource" next="d47e148 d47e148HF SE1"/>
  <step type="METHOD" id="d47e149" match="GET" next="d47e153 d47e153HF"/>
  <step type="METHOD" id="d47e156" match="POST" next="d47e160 d47e177 d47e156rqt"/>
  <step type="REQ_TYPE" id="d47e160" match="(?i)(application/xml)(;.*)?" next="d47e160W d47e160WF"/>
  <step type="REQ_TYPE" id="d47e177" match="(?i)(application/json)(;.*)?" next="d47e177W d47e177WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configGrammar)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e135W")
      assert(checkerLog, "CONTENT_FAIL")
    }

    scenario ("Checker with a missing CONTENT_FAIL step (JSON_SCHEMA)") {
      Given("A checker with a missing CONTENT_FAIL step (JSON_SCHEMA)")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T13:46:28.842-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test11.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ...
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" xmlns:xsd="http://www.w3.org/2001/XMLSchema" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="S0" type="START" next="d47e88 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100014"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e130" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e131 d55e38m SE0"/>
  <step type="WELL_XML" id="d47e135W" priority="41009" next="d47e1350XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1350XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e1351XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1351XPTH" match="true()" priority="41011" next="d47e1352XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1352XPTH" match="/" priority="41012" next="d47e135XSD d47e135WF"/>
  <step type="XSD" id="d47e135XSD" priority="45013" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e135WF" priority="40013"/>
  <step type="REQ_TYPE_FAIL" id="d47e131rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e38m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e130HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e36u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e51m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e50u" notMatch="again" priority="10055"/>
  <step id="d47e104" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="WELL_XML" id="d47e110W" priority="41007" next="d47e1101PPROC"/>
  <step type="XSL" id="d47e1101PPROC" version="2" priority="41008" next="d47e110XSD d47e110WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e110XSD" priority="45009" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e110WF" priority="40009"/>
  <step type="REQ_TYPE_FAIL" id="d47e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30006"/>
  <step id="d47e106" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="d47e110 d47e100rqt"/>
  <step id="d47e108" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="CONTENT_FAIL" id="d47e104HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e148" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e149 d47e156 d55e54m SE0"/>
  <step id="d47e153" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e153HF" priority="40006"/>
  <step type="WELL_XML" id="d47e160W" priority="41007" next="d47e1600XPTH d47e160WF"/>
  <step type="XPATH" id="d47e1600XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="d47e1601PPROC"/>
  <step type="XSL" id="d47e1601PPROC" version="2" priority="41009" next="d47e160XSD d47e160WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e160XSD" priority="45010" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e160WF" priority="40010"/>
  <step type="WELL_JSON" id="d47e177W" priority="41007" next="d47e177JSON"/>
  <step type="JSON_SCHEMA" id="d47e177JSON" priority="45008" next="SA"/>
  <step type="REQ_TYPE_FAIL" id="d47e156rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e54m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e148HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e88" match="path" next="d47e89 SE1 d55e3u"/>
  <step type="URL" id="d47e89" match="to" next="d47e90 d47e99 d47e147 SE1 d55e4u"/>
  <step type="URL" id="d47e90" match="another" next="d47e91 d47e95 SE1"/>
  <step type="URL" id="d47e91" match="(?s).*" label="anything" next="d47e93 SE1 d55e6u"/>
  <step type="URL" id="d47e93" match="again" next="d47e94 d55e7m SE0"/>
  <step type="METHOD" id="d47e94" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e95" match="xs:dateTime" label="time" next="d47e97 SE1 d55e9u"/>
  <step type="URL" id="d47e97" match="again" next="d47e98 d55e10m SE0"/>
  <step type="METHOD" id="d47e98" match="POST" next="SA"/>
  <step type="URL" id="d47e99" match="other" next="d47e128 d47e143 d47e100 d55e12m"/>
  <step type="METHOD" id="d47e100" match="GET" next="d47e104 d47e106 d47e108 d47e104HF"/>
  <step type="REQ_TYPE" id="d47e110" match="(?i)(application/xml)(;.*)?" next="d47e110W d47e110WF"/>
  <step type="URL" id="d47e128" match="foo" next="d47e129 SE1 d55e36u"/>
  <step type="URL" id="d47e129" match="again" next="d47e130 d47e130HF SE1 SE0"/>
  <step type="METHOD" id="d47e131" match="POST" next="d47e135 d47e131rqt"/>
  <step type="REQ_TYPE" id="d47e135" match="(?i)(application/xml)(;.*)?" next="d47e135W d47e135WF"/>
  <step type="URL" id="d47e143" match="(?s).*" label="stuff" next="d47e145 SE1 d55e50u"/>
  <step type="URL" id="d47e145" match="again" next="d47e146 d55e51m SE0"/>
  <step type="METHOD" id="d47e146" match="GET" next="SA"/>
  <step type="URL" id="d47e147" match="resource" next="d47e148 d47e148HF SE1"/>
  <step type="METHOD" id="d47e149" match="GET" next="d47e153 d47e153HF"/>
  <step type="METHOD" id="d47e156" match="POST" next="d47e160 d47e177 d47e156rqt"/>
  <step type="REQ_TYPE" id="d47e160" match="(?i)(application/xml)(;.*)?" next="d47e160W d47e160WF"/>
  <step type="REQ_TYPE" id="d47e177" match="(?i)(application/json)(;.*)?" next="d47e177W"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configGrammar)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e177W")
      assert(checkerLog, "CONTENT_FAIL")
    }


    scenario ("Checker with an XSD Step in the wrong spot") {
      Given("A checker with an XSD Step in the wrong spot")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T13:46:28.842-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test11.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ..
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" xmlns:xsd="http://www.w3.org/2001/XMLSchema" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="S0" type="START" next="d47e88 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100014"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e130" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e131 d55e38m SE0"/>
  <step type="WELL_XML" id="d47e135W" priority="41009" next="d47e1350XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1350XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e1351XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1351XPTH" match="true()" priority="41011" next="d47e1352XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1352XPTH" match="/" priority="41012" next="d47e135XSD d47e135WF"/>
  <step type="XSD" id="d47e135XSD" priority="45013" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e135WF" priority="40013"/>
  <step type="REQ_TYPE_FAIL" id="d47e131rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e38m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e130HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e36u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e51m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e50u" notMatch="again" priority="10055"/>
  <step id="d47e104" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="WELL_XML" id="d47e110W" priority="41007" next="d47e1101PPROC"/>
  <step type="XSL" id="d47e1101PPROC" version="2" priority="41008" next="d47e110XSD d47e110WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e110XSD" priority="45009" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e110WF" priority="40009"/>
  <step type="REQ_TYPE_FAIL" id="d47e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30006"/>
  <step id="d47e106" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="d47e110 d47e100rqt"/>
  <step id="d47e108" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="CONTENT_FAIL" id="d47e104HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e148" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e149 d47e156 d55e54m SE0"/>
  <step id="d47e153" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e153HF" priority="40006"/>
  <step type="WELL_XML" id="d47e160W" priority="41007" next="d47e1600XPTH d47e160WF"/>
  <step type="XPATH" id="d47e1600XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="d47e1601PPROC"/>
  <step type="XSL" id="d47e1601PPROC" version="2" priority="41009" next="d47e160XSD d47e160WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e160XSD" priority="45010" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e160WF" priority="40010"/>
  <step type="WELL_JSON" id="d47e177W" priority="41007" next="d47e177JSON d47e177WF"/>
  <step type="JSON_SCHEMA" id="d47e177JSON" priority="45008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e177WF" priority="40008"/>
  <step type="REQ_TYPE_FAIL" id="d47e156rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e54m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e148HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e88" match="path" next="d47e89 SE1 d55e3u"/>
  <step type="URL" id="d47e89" match="to" next="d47e90 d47e99 d47e147 SE1 d55e4u"/>
  <step type="URL" id="d47e90" match="another" next="d47e91 d47e95 SE1"/>
  <step type="URL" id="d47e91" match="(?s).*" label="anything" next="d47e93 SE1 d55e6u"/>
  <step type="URL" id="d47e93" match="again" next="d47e94 d55e7m SE0"/>
  <step type="METHOD" id="d47e94" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e95" match="xs:dateTime" label="time" next="d47e97 SE1 d55e9u"/>
  <step type="URL" id="d47e97" match="again" next="d47e98 d55e10m SE0"/>
  <step type="METHOD" id="d47e98" match="POST" next="SA"/>
  <step type="URL" id="d47e99" match="other" next="d47e128 d47e143 d47e100 d55e12m"/>
  <step type="METHOD" id="d47e100" match="GET" next="d47e104 d47e106 d47e108 d47e104HF"/>
  <step type="REQ_TYPE" id="d47e110" match="(?i)(application/xml)(;.*)?" next="d47e135XSD d47e110W d47e110WF"/>
  <step type="URL" id="d47e128" match="foo" next="d47e129 SE1 d55e36u"/>
  <step type="URL" id="d47e129" match="again" next="d47e130 d47e130HF SE1 SE0"/>
  <step type="METHOD" id="d47e131" match="POST" next="d47e135 d47e131rqt"/>
  <step type="REQ_TYPE" id="d47e135" match="(?i)(application/xml)(;.*)?" next="d47e135W d47e135WF"/>
  <step type="URL" id="d47e143" match="(?s).*" label="stuff" next="d47e145 SE1 d55e50u"/>
  <step type="URL" id="d47e145" match="again" next="d47e146 d55e51m SE0"/>
  <step type="METHOD" id="d47e146" match="GET" next="SA"/>
  <step type="URL" id="d47e147" match="resource" next="d47e148 d47e148HF SE1"/>
  <step type="METHOD" id="d47e149" match="GET" next="d47e153 d47e153HF"/>
  <step type="METHOD" id="d47e156" match="POST" next="d47e160 d47e177 d47e156rqt"/>
  <step type="REQ_TYPE" id="d47e160" match="(?i)(application/xml)(;.*)?" next="d47e160W d47e160WF"/>
  <step type="REQ_TYPE" id="d47e177" match="(?i)(application/json)(;.*)?" next="d47e177W d47e177WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configGrammar)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e135XSD")
      assert(checkerLog, "parent")
      assert(checkerLog, "WELL_XML or XSL or XPATH")
    }

    scenario ("Checker with a JSON_SCHEMA step in the wrong spot") {
      Given("A checker with a JSON_SCHEMA step in the wrong spot")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d47e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-10T13:46:28.842-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test11.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ...
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" xmlns:xsd="http://www.w3.org/2001/XMLSchema" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="S0" type="START" next="d47e88 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100014"/>
  <step type="METHOD_FAIL" id="d55e7m" notMatch="POST" priority="20056"/>
  <step id="SE0" type="URL_FAIL" priority="10007"/>
  <step id="SE1" type="METHOD_FAIL" priority="20006"/>
  <step type="URL_FAIL" id="d55e6u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e10m" notMatch="POST" priority="20056"/>
  <step type="URL_FAIL" id="d55e9u" notMatch="again" priority="10055"/>
  <step id="d47e130" name="X-DATE" type="HEADERXSD" match="xs:dateTime" priority="41006" next="d47e131 d55e38m SE0"/>
  <step type="WELL_XML" id="d47e135W" priority="41009" next="d47e1350XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1350XPTH" match="/xs:elementToo" message="Expecting the root element to be: xs:elementToo" priority="41010" next="d47e1351XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1351XPTH" match="true()" priority="41011" next="d47e1352XPTH d47e135WF"/>
  <step type="XPATH" id="d47e1352XPTH" match="/" priority="41012" next="d47e135XSD d47e135WF"/>
  <step type="XSD" id="d47e135XSD" priority="45013" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e135WF" priority="40013"/>
  <step type="REQ_TYPE_FAIL" id="d47e131rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step type="METHOD_FAIL" id="d55e38m" notMatch="POST" priority="20057"/>
  <step type="CONTENT_FAIL" id="d47e130HF" priority="40006"/>
  <step type="URL_FAIL" id="d55e36u" notMatch="again" priority="10055"/>
  <step type="METHOD_FAIL" id="d55e51m" notMatch="GET" priority="20056"/>
  <step type="URL_FAIL" id="d55e50u" notMatch="again" priority="10055"/>
  <step id="d47e104" name="X-ROLE" type="HEADER_ANY" match="ROLE1" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="WELL_XML" id="d47e110W" priority="41007" next="d47e1101PPROC"/>
  <step type="XSL" id="d47e1101PPROC" version="2" priority="41008" next="d47e110XSD d47e110WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e110XSD" priority="45009" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e110WF" priority="40009"/>
  <step type="REQ_TYPE_FAIL" id="d47e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30006"/>
  <step id="d47e106" name="X-ROLE" type="HEADER_ANY" match="ROLE2" priority="41005" next="d47e110 d47e100rqt"/>
  <step id="d47e108" name="X-ROLE" type="HEADER_ANY" match="ROLE3" priority="41005" next="d47e110 d47e100rqt"/>
  <step type="CONTENT_FAIL" id="d47e104HF" priority="40005"/>
  <step type="METHOD_FAIL" id="d55e12m" notMatch="GET" priority="20054"/>
  <step id="d47e148" name="X-AUTH-TOKEN" type="HEADER" match="(?s).*" priority="41004" next="d47e149 d47e156 d55e54m SE0"/>
  <step id="d47e153" name="X-TIME" type="HEADERXSD" match="xs:time" priority="41006" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e153HF" priority="40006"/>
  <step type="WELL_XML" id="d47e160W" priority="41007" next="d47e1600XPTH d47e160WF"/>
  <step type="XPATH" id="d47e1600XPTH" match="/xs:element" message="Expecting the root element to be: xs:element" priority="41008" next="d47e1601PPROC"/>
  <step type="XSL" id="d47e1601PPROC" version="2" priority="41009" next="d47e160XSD d47e160WF">
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="XSD" id="d47e160XSD" priority="45010" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e160WF" priority="40010"/>
  <step type="WELL_JSON" id="d47e177W" priority="41007" next="d47e177JSON d47e177WF"/>
  <step type="JSON_SCHEMA" id="d47e177JSON" priority="45008" next="SA"/>
  <step type="CONTENT_FAIL" id="d47e177WF" priority="40008"/>
  <step type="REQ_TYPE_FAIL" id="d47e156rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step type="METHOD_FAIL" id="d55e54m" notMatch="GET|POST" priority="20105"/>
  <step type="CONTENT_FAIL" id="d47e148HF" priority="40004"/>
  <step type="URL_FAIL" id="d55e4u" notMatch="another|other|resource" priority="10153"/>
  <step type="URL_FAIL" id="d55e3u" notMatch="to" priority="10052"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="path" priority="10051"/>
  <step type="URL" id="d47e88" match="path" next="d47e89 SE1 d55e3u"/>
  <step type="URL" id="d47e89" match="to" next="d47e90 d47e99 d47e147 SE1 d55e4u"/>
  <step type="URL" id="d47e90" match="another" next="d47e91 d47e95 SE1"/>
  <step type="URL" id="d47e91" match="(?s).*" label="anything" next="d47e93 SE1 d55e6u"/>
  <step type="URL" id="d47e93" match="again" next="d47e94 d55e7m SE0"/>
  <step type="METHOD" id="d47e94" match="POST" next="SA"/>
  <step type="URLXSD" id="d47e95" match="xs:dateTime" label="time" next="d47e97 SE1 d55e9u"/>
  <step type="URL" id="d47e97" match="again" next="d47e98 d55e10m SE0"/>
  <step type="METHOD" id="d47e98" match="POST" next="SA"/>
  <step type="URL" id="d47e99" match="other" next="d47e128 d47e143 d47e100 d55e12m"/>
  <step type="METHOD" id="d47e100" match="GET" next="d47e104 d47e106 d47e108 d47e104HF"/>
  <step type="REQ_TYPE" id="d47e110" match="(?i)(application/xml)(;.*)?" next="d47e110W d47e110WF"/>
  <step type="URL" id="d47e128" match="foo" next="d47e129 SE1 d55e36u"/>
  <step type="URL" id="d47e129" match="again" next="d47e130 d47e130HF SE1 SE0"/>
  <step type="METHOD" id="d47e131" match="POST" next="d47e135 d47e131rqt"/>
  <step type="REQ_TYPE" id="d47e135" match="(?i)(application/xml)(;.*)?" next="d47e135W d47e135WF"/>
  <step type="URL" id="d47e143" match="(?s).*" label="stuff" next="d47e145 SE1 d55e50u"/>
  <step type="URL" id="d47e145" match="again" next="d47e146 d55e51m SE0"/>
  <step type="METHOD" id="d47e146" match="GET" next="SA"/>
  <step type="URL" id="d47e147" match="resource" next="d47e148 d47e148HF SE1"/>
  <step type="METHOD" id="d47e149" match="GET" next="d47e153 d47e153HF"/>
  <step type="METHOD" id="d47e156" match="POST" next="d47e160 d47e177 d47e156rqt"/>
  <step type="REQ_TYPE" id="d47e160" match="(?i)(application/xml)(;.*)?" next="d47e160W d47e160WF"/>
  <step type="REQ_TYPE" id="d47e177" match="(?i)(application/json)(;.*)?" next="d47e177W d47e177WF d47e177JSON"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configGrammar)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "d47e177JSON")
      assert(checkerLog, "parent")
      assert(checkerLog, "WELL_JSON")
    }

    scenario ("Checker with advance features, rax:roleMask, removeDups, XPath join should validate without issues") {
      Given("A checker with advance features, rax:roleMask, removeDups, XPath join should validate without issues")
      val in = ("in.checker", <checker xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d48e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-11T16:56:43.283-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test12.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="true"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="true"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="true"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="true"/>
    <config option="maskRaxRoles403" value="true"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ...
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="d48e88_AnotherFoo" type="URL" match="path" next="d48e89_AnotherFoo d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_AnotherFoo" type="URL" match="to" next="d48e90_AnotherFoo d48e99_AnotherFoo d48e141_AnotherFoo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e90_AnotherFoo" type="URL" match="another" next="d48e91_AnotherFoo d48e95_AnotherFoo d48e90UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e91_AnotherFoo" type="URL" match="(?s).*" label="anything" next="d48e93_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e93_AnotherFoo" type="URL" match="again" next="d48e94_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e94_AnotherFoo" type="METHOD" match="POST" next="SA d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e95_AnotherFoo" type="URLXSD" match="xs:dateTime" label="time" next="d48e93_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99_AnotherFoo" type="URL" match="other" next="d48e122_AnotherFoo d48e137_AnotherFoo d48e100_AnotherFoo d48e99UF_AnotherFoo d48e99MF_AnotherFoo"/>
  <step id="d48e122_AnotherFoo" type="URL" match="foo" next="d48e123_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e123_AnotherFoo" type="URL" match="again" next="d48e124_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e124_AnotherFoo" type="METHOD" match="POST" next="d48e129 d48e100rqt"/>
  <step id="d48e137_AnotherFoo" type="URL" match="(?s).*" label="stuff" next="d48e139_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e139_AnotherFoo" type="URL" match="again" next="d48e140_AnotherFoo d48e93UF_AnotherFoo d48e99MF_AnotherFoo"/>
  <step id="d48e140_AnotherFoo" type="METHOD" match="GET" next="SA d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e100_AnotherFoo" type="METHOD" match="GET" next="d48e100rqt d48e104 d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e141_AnotherFoo" type="URL" match="resource" next="d48e148_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e148_AnotherFoo" type="METHOD" match="POST" next="d48e148rqt d48e152 d48e169 d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e88_foo" type="URL" match="path" next="d48e89_foo d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_foo" type="URL" match="to" next="d48e90_AnotherFoo d48e99_foo d48e141_foo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99_foo" type="URL" match="other" next="d48e137_AnotherFoo d48e100_AnotherFoo d48e99UF_foo d48e99MF_AnotherFoo"/>
  <step id="d48e141_foo" type="URL" match="resource" next="d48e142_foo d48e148_AnotherFoo d48e93UF_AnotherFoo d48e141MF_foo"/>
  <step id="d48e142_foo" type="METHOD" match="GET" next="SA"/>
  <step id="S0" type="START" next="d48e88_ S0_AnotherFoo S0_foo S0MF_AnotherFoo S0UF_AnotherFoo"/>
  <step id="S0_AnotherFoo" type="HEADER_ANY" name="X-ROLES" match="AnotherFoo" priority="5" next="d48e88_AnotherFoo S0UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="SA" type="ACCEPT" priority="100012"/>
  <step id="d48e93UF_AnotherFoo" type="URL_FAIL" priority="10008"/>
  <step id="S0MF_AnotherFoo" type="METHOD_FAIL" priority="20008"/>
  <step id="d48e93MF_AnotherFoo" type="METHOD_FAIL" notMatch="POST" priority="20057"/>
  <step id="d48e91UF_AnotherFoo" type="URL_FAIL" notMatch="again" priority="10056"/>
  <step id="d48e90UF_AnotherFoo" type="URL_FAIL" notMatch="(?s).*" notTypes="xs:dateTime" priority="10105"/>
  <step id="d89e0" type="WELL_XML" priority="41009" next="d97e0 d48e104WF"/>
  <step id="d97e0" type="XSL" version="1" priority="41010" next="d48e104XSD d48e104WF">
    <xsl:transform xmlns:check="http://www.rackspace.com/repose/wadl/checker" version="1.0" check:mergable="true">
      <xsl:template match="/">
        <xsl:choose>
          <xsl:when test="/xs:elementToo"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting the root element to be: xs:elementToo</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="true()"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting true()</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="/"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting /</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:copy>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:template>
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:transform>
  </step>
  <step type="XSD" id="d48e104XSD" priority="45011" next="SA"/>
  <step type="CONTENT_FAIL" id="d48e104WF" priority="40011"/>
  <step type="REQ_TYPE_FAIL" id="d48e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step id="d48e99MF_AnotherFoo" type="METHOD_FAIL" notMatch="GET" priority="20057"/>
  <step type="WELL_XML" id="d48e104W" priority="41007" next="d48e1041PPROC"/>
  <step type="XSL" id="d48e1041PPROC" version="2" priority="41008" next="d48e104XSD d48e104WF">
    <xsl:stylesheet xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step id="d48e99UF_AnotherFoo" type="URL_FAIL" notMatch="(?s).*|foo" priority="10105"/>
  <step type="REQ_TYPE_FAIL" id="d48e148rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step id="d89e2" type="WELL_XML" priority="41007" next="d89e2NS d48e104WF"/>
  <step id="d89e2NS" type="XSL" version="1" priority="41008" next="d48e1521PPROC">
    <xsl:transform xmlns:check="http://www.rackspace.com/repose/wadl/checker" version="1.0" check:mergable="true">
      <xsl:template match="/">
        <xsl:choose>
          <xsl:when test="/xs:element"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting the root element to be: xs:element</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:copy>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:template>
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:transform>
  </step>
  <step type="XSL" id="d48e1521PPROC" version="2" priority="41009" next="d48e104XSD d48e104WF">
    <xsl:stylesheet xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="WELL_JSON" id="d48e169W" priority="41007" next="d48e169JSON d48e104WF"/>
  <step type="JSON_SCHEMA" id="d48e169JSON" priority="45008" next="SA"/>
  <step id="d48e89UF_AnotherFoo" type="URL_FAIL" notMatch="another|other|resource" priority="10154"/>
  <step id="d48e88UF_AnotherFoo" type="URL_FAIL" notMatch="to" priority="10053"/>
  <step id="S0UF_AnotherFoo" type="URL_FAIL" notMatch="path" priority="10052"/>
  <step id="S0_foo" type="HEADER_ANY" name="X-ROLES" match="foo" priority="5" next="d48e88_foo S0UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99UF_foo" type="URL_FAIL" notMatch="(?s).*" priority="10055"/>
  <step id="d48e141MF_foo" type="METHOD_FAIL" notMatch="GET|POST" priority="20105"/>
  <step id="d48e88_" type="URL" match="path" next="d48e89_ d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_" type="URL" match="to" next="d48e90_AnotherFoo d48e99_foo d48e141_AnotherFoo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step type="REQ_TYPE" id="d48e104" match="(?i)(application/xml)(;.*)?" next="d48e104W d48e104WF"/>
  <step type="REQ_TYPE" id="d48e129" match="(?i)(application/xml)(;.*)?" next="d89e0 d48e104WF"/>
  <step type="REQ_TYPE" id="d48e152" match="(?i)(application/xml)(;.*)?" next="d89e2 d48e104WF"/>
  <step type="REQ_TYPE" id="d48e169" match="(?i)(application/json)(;.*)?" next="d48e169W d48e104WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        val checker = builder.build(in, configAdvance)
      }
      Then ("The checker should be loaded with no errors")
      assertEmpty (checkerLog)
    }

    scenario ("Checker with advance features and rox:rolesMask header check of the wrong type") {
      Given("A checker with advance features and rox:rolesMask header check of the wrong type")
      val in = ("in.checker", <checker xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                                       xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                       xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                                       xmlns:d48e1="http://wadl.dev.java.net/2009/02"
                                       xmlns:rax="http://docs.rackspace.com/api">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-11T16:56:43.283-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test12.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="true"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="true"/>
    <config option="checkXSDGrammar" value="true"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="true"/>
    <config option="checkWellFormed" value="true"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="true"/>
    <config option="checkElements" value="true"/>
    <config option="preserveRequestBody" value="true"/>
    <config option="checkHeaders" value="true"/>
    <config option="enableRaxRolesExtension" value="true"/>
    <config option="maskRaxRoles403" value="true"/>
  </meta>
  <grammar type="SCHEMA_JSON">
                ...
        </grammar>
  <grammar ns="http://www.rackspace.com/repose/wadl/checker/step/test">
    <schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.rackspace.com/repose/wadl/checker/step/test">
      <element name="e" type="tst:SampleElement"/>
      <element name="a" type="tst:SampleAttribute"/>
      <complexType name="SampleElement">
        <sequence>
          <element name="id" type="tst:UUID"/>
          <element default="START" minOccurs="0" name="stepType" type="tst:StepType"/>
          <element default="50" minOccurs="0" name="even" type="tst:EvenInt100"/>
        </sequence>
      </complexType>
      <complexType name="SampleAttribute">
        <attribute name="id" type="tst:UUID" use="required"/>
        <attribute default="START" name="stepType" type="tst:StepType" use="optional"/>
        <attribute default="50" name="even" type="tst:EvenInt100" use="optional"/>
      </complexType>
      <!-- A simple enumeration -->
      <simpleType name="StepType">
        <restriction base="xsd:string">
          <enumeration value="START"/>
          <enumeration value="URL_FAIL"/>
          <enumeration value="METHOD_FAIL"/>
          <enumeration value="ACCEPT"/>
          <enumeration value="URL"/>
          <enumeration value="METHOD"/>
          <enumeration value="URLXSD"/>
        </restriction>
      </simpleType>
      <!-- A pattern -->
      <simpleType name="UUID">
        <restriction base="xsd:string">
          <length fixed="true" value="36"/>
          <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
        </restriction>
      </simpleType>
      <!-- XSD 1.1 assert -->
      <simpleType name="EvenInt100">
        <restriction base="xsd:integer">
          <minInclusive value="0"/>
          <maxInclusive value="100"/>
          <assertion test="$value mod 2 = 0"/>
        </restriction>
      </simpleType>
    </schema>
  </grammar>
  <step id="d48e88_AnotherFoo" type="URL" match="path" next="d48e89_AnotherFoo d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_AnotherFoo" type="URL" match="to" next="d48e90_AnotherFoo d48e99_AnotherFoo d48e141_AnotherFoo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e90_AnotherFoo" type="URL" match="another" next="d48e91_AnotherFoo d48e95_AnotherFoo d48e90UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e91_AnotherFoo" type="URL" match="(?s).*" label="anything" next="d48e93_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e93_AnotherFoo" type="URL" match="again" next="d48e94_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e94_AnotherFoo" type="METHOD" match="POST" next="SA d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e95_AnotherFoo" type="URLXSD" match="xs:dateTime" label="time" next="d48e93_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99_AnotherFoo" type="URL" match="other" next="d48e122_AnotherFoo d48e137_AnotherFoo d48e100_AnotherFoo d48e99UF_AnotherFoo d48e99MF_AnotherFoo"/>
  <step id="d48e122_AnotherFoo" type="URL" match="foo" next="d48e123_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e123_AnotherFoo" type="URL" match="again" next="d48e124_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e124_AnotherFoo" type="METHOD" match="POST" next="d48e129 d48e100rqt"/>
  <step id="d48e137_AnotherFoo" type="URL" match="(?s).*" label="stuff" next="d48e139_AnotherFoo d48e91UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e139_AnotherFoo" type="URL" match="again" next="d48e140_AnotherFoo d48e93UF_AnotherFoo d48e99MF_AnotherFoo"/>
  <step id="d48e140_AnotherFoo" type="METHOD" match="GET" next="SA d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e100_AnotherFoo" type="METHOD" match="GET" next="d48e100rqt d48e104 d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e141_AnotherFoo" type="URL" match="resource" next="d48e148_AnotherFoo d48e93UF_AnotherFoo d48e93MF_AnotherFoo"/>
  <step id="d48e148_AnotherFoo" type="METHOD" match="POST" next="d48e148rqt d48e152 d48e169 d48e93UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e88_foo" type="URL" match="path" next="d48e89_foo d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_foo" type="URL" match="to" next="d48e90_AnotherFoo d48e99_foo d48e141_foo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99_foo" type="URL" match="other" next="d48e137_AnotherFoo d48e100_AnotherFoo d48e99UF_foo d48e99MF_AnotherFoo"/>
  <step id="d48e141_foo" type="URL" match="resource" next="d48e142_foo d48e148_AnotherFoo d48e93UF_AnotherFoo d48e141MF_foo"/>
  <step id="d48e142_foo" type="METHOD" match="GET" next="SA"/>
  <step id="S0" type="START" next="d48e88_ S0_AnotherFoo S0_foo S0MF_AnotherFoo S0UF_AnotherFoo"/>
  <step id="S0_AnotherFoo" type="HEADER" name="X-ROLES" match="AnotherFoo" priority="5" next="d48e88_AnotherFoo S0UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="SA" type="ACCEPT" priority="100012"/>
  <step id="d48e93UF_AnotherFoo" type="URL_FAIL" priority="10008"/>
  <step id="S0MF_AnotherFoo" type="METHOD_FAIL" priority="20008"/>
  <step id="d48e93MF_AnotherFoo" type="METHOD_FAIL" notMatch="POST" priority="20057"/>
  <step id="d48e91UF_AnotherFoo" type="URL_FAIL" notMatch="again" priority="10056"/>
  <step id="d48e90UF_AnotherFoo" type="URL_FAIL" notMatch="(?s).*" notTypes="xs:dateTime" priority="10105"/>
  <step id="d89e0" type="WELL_XML" priority="41009" next="d97e0 d48e104WF"/>
  <step id="d97e0" type="XSL" version="1" priority="41010" next="d48e104XSD d48e104WF">
    <xsl:transform xmlns:check="http://www.rackspace.com/repose/wadl/checker" version="1.0" check:mergable="true">
      <xsl:template match="/">
        <xsl:choose>
          <xsl:when test="/xs:elementToo"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting the root element to be: xs:elementToo</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="true()"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting true()</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="/"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting /</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:copy>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:template>
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:transform>
  </step>
  <step type="XSD" id="d48e104XSD" priority="45011" next="SA"/>
  <step type="CONTENT_FAIL" id="d48e104WF" priority="40011"/>
  <step type="REQ_TYPE_FAIL" id="d48e100rqt" notMatch="(?i)(application/xml)(;.*)?" priority="30008"/>
  <step id="d48e99MF_AnotherFoo" type="METHOD_FAIL" notMatch="GET" priority="20057"/>
  <step type="WELL_XML" id="d48e104W" priority="41007" next="d48e1041PPROC"/>
  <step type="XSL" id="d48e1041PPROC" version="2" priority="41008" next="d48e104XSD d48e104WF">
    <xsl:stylesheet xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step id="d48e99UF_AnotherFoo" type="URL_FAIL" notMatch="(?s).*|foo" priority="10105"/>
  <step type="REQ_TYPE_FAIL" id="d48e148rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?" priority="30006"/>
  <step id="d89e2" type="WELL_XML" priority="41007" next="d89e2NS d48e104WF"/>
  <step id="d89e2NS" type="XSL" version="1" priority="41008" next="d48e1521PPROC">
    <xsl:transform xmlns:check="http://www.rackspace.com/repose/wadl/checker" version="1.0" check:mergable="true">
      <xsl:template match="/">
        <xsl:choose>
          <xsl:when test="/xs:element"/>
          <xsl:otherwise>
            <xsl:message terminate="yes">Expecting the root element to be: xs:element</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:copy>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:template>
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:transform>
  </step>
  <step type="XSL" id="d48e1521PPROC" version="2" priority="41009" next="d48e104XSD d48e104WF">
    <xsl:stylesheet xmlns="http://wadl.dev.java.net/2009/02" version="2.0">
      <xsl:template match="node() | @*">
        <xsl:copy>
          <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      </xsl:template>
    </xsl:stylesheet>
  </step>
  <step type="WELL_JSON" id="d48e169W" priority="41007" next="d48e169JSON d48e104WF"/>
  <step type="JSON_SCHEMA" id="d48e169JSON" priority="45008" next="SA"/>
  <step id="d48e89UF_AnotherFoo" type="URL_FAIL" notMatch="another|other|resource" priority="10154"/>
  <step id="d48e88UF_AnotherFoo" type="URL_FAIL" notMatch="to" priority="10053"/>
  <step id="S0UF_AnotherFoo" type="URL_FAIL" notMatch="path" priority="10052"/>
  <step id="S0_foo" type="HEADER_ANY" name="X-ROLES" match="foo" priority="5" next="d48e88_foo S0UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e99UF_foo" type="URL_FAIL" notMatch="(?s).*" priority="10055"/>
  <step id="d48e141MF_foo" type="METHOD_FAIL" notMatch="GET|POST" priority="20105"/>
  <step id="d48e88_" type="URL" match="path" next="d48e89_ d48e88UF_AnotherFoo S0MF_AnotherFoo"/>
  <step id="d48e89_" type="URL" match="to" next="d48e90_AnotherFoo d48e99_foo d48e141_AnotherFoo d48e89UF_AnotherFoo S0MF_AnotherFoo"/>
  <step type="REQ_TYPE" id="d48e104" match="(?i)(application/xml)(;.*)?" next="d48e104W d48e104WF"/>
  <step type="REQ_TYPE" id="d48e129" match="(?i)(application/xml)(;.*)?" next="d89e0 d48e104WF"/>
  <step type="REQ_TYPE" id="d48e152" match="(?i)(application/xml)(;.*)?" next="d89e2 d48e104WF"/>
  <step type="REQ_TYPE" id="d48e169" match="(?i)(application/json)(;.*)?" next="d48e169W d48e104WF"/>
</checker>)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(in, configAdvance)
        }
      }
      Then ("The checker should be rejected with an appropriate message")
      assert(checkerLog, "S0_AnotherFoo")
      assert(checkerLog, "CONTENT_FAIL")
    }


    scenario ("Checker with funky regex symbols should validate without issues") {
      Given("A checker with funky regex symbols")
      val in = ("in.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker"
                                       xmlns:json="http://json-schema.org/schema#"
                                       xmlns:util="http://www.rackspace.com/repose/wadl/checker/util">
  <meta>
    <built-by>jorgew</built-by>
    <created-by>{creatorString}</created-by>
    <created-on>2015-03-12T09:07:16.134-05:00</created-on>
    <created-from>file:/Users/jorgew/projects/api-checker/test13.wadl.xml</created-from>
    <config option="enableIgnoreJSONSchemaExtension" value="true"/>
    <config option="enableMessageExtension" value="true"/>
    <config option="joinXPathChecks" value="false"/>
    <config option="doXSDGrammarTransform" value="false"/>
    <config option="enablePreProcessExtension" value="true"/>
    <config option="removeDups" value="false"/>
    <config option="checkXSDGrammar" value="false"/>
    <config option="xpathVersion" value="1"/>
    <config option="checkPlainParams" value="false"/>
    <config option="checkWellFormed" value="false"/>
    <config option="enableIgnoreXSDExtension" value="true"/>
    <config option="checkJSONGrammar" value="false"/>
    <config option="checkElements" value="false"/>
    <config option="preserveRequestBody" value="false"/>
    <config option="checkHeaders" value="false"/>
    <config option="enableRaxRolesExtension" value="false"/>
    <config option="maskRaxRoles403" value="false"/>
  </meta>
  <step id="S0" type="START" next="d47e6 d47e8 SE1 d55e2u"/>
  <step id="SA" type="ACCEPT" priority="100003"/>
  <step type="METHOD_FAIL" id="d55e3m" notMatch="\.\-" priority="20052"/>
  <step id="SE0" type="URL_FAIL" priority="10002"/>
  <step type="METHOD_FAIL" id="d55e5m" notMatch="\-GET\.\.IT\-" priority="20052"/>
  <step id="SE1" type="METHOD_FAIL" priority="20001"/>
  <step type="URL_FAIL" id="d55e2u" notMatch="\\\^\-\.\$\{\}\*\+\|\#\(\)\[\]|\^ABC\[D\]EFG\#" priority="10151"/>
  <step type="URL" id="d47e6" match="\\\^\-\.\$\{\}\*\+\|\#\(\)\[\]" next="d47e7 d55e3m SE0"/>
  <step type="METHOD" id="d47e7" match="\.\-" next="SA"/>
  <step type="URL" id="d47e8" match="\^ABC\[D\]EFG\#" next="d47e9 d55e5m SE0"/>
  <step type="METHOD" id="d47e9" match="\-GET\.\.IT\-" next="SA"/>
</checker>
)
      When("the document is loaded")
      val checkerLog = log (Level.ERROR) {
        val checker = builder.build(in, config)
      }
      Then ("The checker should be loaded with no errors")
      assertEmpty (checkerLog)
    }

  }

}
