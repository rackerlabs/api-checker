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

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

//
//  Test optimizations in complex setups.
//

@RunWith(classOf[JUnitRunner])
class WADLCheckerOptSpec extends BaseCheckerSpec {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("xsl","http://www.w3.org/1999/XSL/Transform")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst","http://www.rackspace.com/repose/wadl/checker/step/test")
  register ("w_ns16","http://docs.rackspace.com/usage/nova/ips")
  register ("w_ns17", "http://docs.rackspace.com/event/nova/host")
  register ("w_ns18", "http://docs.rackspace.com/event/RHEL")
  register ("atom", "http://www.w3.org/2005/Atom")
  register ("foo", "http://www.rackspace.com/foo/bar")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    //
    //  WADL with resources that have shared XPath checks.
    //
    val sharedXPathWADL =
<application xmlns="http://wadl.dev.java.net/2009/02"
xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:rax="http://docs.rackspace.com/api"
xmlns:w_ns16="http://docs.rackspace.com/usage/nova/ips"
xmlns:w_ns17="http://docs.rackspace.com/event/nova/host"
xmlns:w_ns18="http://docs.rackspace.com/event/RHEL"
xmlns:atom="http://www.w3.org/2005/Atom">
  <resources base="http://localhost">
    <resource path="servers/entries" type="#CloudServers #RHEL" />
    <resource path="nova/entries" type="#CloudServersOpenStack #RHEL" />
  </resources>
  <resource_type id="RHEL">
    <method id="addRHELEntry" name="POST">
      <request>
        <representation mediaType="application/atom+xml" element="atom:entry">
          <param name="usage" style="plain" required="true" path="/atom:entry/w_ns18:usage" />
          <param name="cross_check" style="plain" required="true" path="/atom:entry/@only_usage" />
        </representation>
      </request>
      <response status="201">
        <representation mediaType="application/atom+xml" />
      </response>
      <response status="400 401 409 500 503">
        <representation mediaType="application/xml" />
      </response>
    </method>
  </resource_type>
  <resource_type id="CloudServersOpenStack">
    <method id="addCloudServersOpenStackEntry" name="POST">
      <request>
        <representation mediaType="application/atom+xml" element="atom:entry">
          <param name="usage" style="plain" required="true" path="/atom:entry/w_ns16:usage" />
          <param name="up" style="plain" required="true" path="/atom:entry/w_ns16:usage/w_ns16:up" />
          <param name="down" style="plain" required="true" path="/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down" />
          <param name="cross_check" style="plain" required="true"
          path="/atom:entry/@only_usage_up_down" />
        </representation>
      </request>
      <response status="201">
        <representation mediaType="application/atom+xml" />
      </response>
      <response status="400 401 409 500 503">
        <representation mediaType="application/xml" />
      </response>
    </method>
  </resource_type>
  <resource_type id="CloudServers">
    <method id="addCloudServersEntry" name="POST">
      <request>
        <representation mediaType="application/atom+xml" element="atom:entry">
          <param name="usage" style="plain" required="true" path="/atom:entry/w_ns17:usage" />
          <param name="up" style="plain" required="true" path="/atom:entry/w_ns17:usage/w_ns17:up" />
          <param name="down" style="plain" required="true" path="/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down" />
          <param name="cross_check" style="plain" required="true"
          path="/atom:entry/@only_usage_up_down" />
        </representation>
      </request>
      <response status="201">
        <representation mediaType="application/atom+xml" />
      </response>
      <response status="400 401 409 500 503">
        <representation mediaType="application/xml" />
      </response>
    </method>
  </resource_type>
</application>

    scenario ("The sharedXPathWADL is processed checking wellformness, XSD, elemnts, and plain parameters, but without optimizations") {
      Given("the sharedXPathWADL")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(false, false, true, true, true, 1, true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("servers"), URL("entries"), Method("POST"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns16:usage"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("servers"), URL("entries"), Method("POST"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)


      assert(checker,Start, URL("nova"), URL("entries"), Method("POST"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns17:usage"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("nova"), URL("entries"), Method("POST"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 5")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 13")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage_up_down']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns18:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage']) = 1")
    }

    scenario ("The sharedXPathWADL is processed checking wellformness, XSD, elemnts, and plain parameters, with remove dups optimization") {
      Given("the sharedXPathWADL")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns16:usage"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)


      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns17:usage"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XPath("/atom:entry"),
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 11")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage_up_down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns18:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage']) = 1")
    }

    scenario ("The sharedXPathWADL is processed checking wellformness, XSD, elemnts, and plain parameters, with remove dups and joinpath optimizations") {
      Given("the sharedXPathWADL")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), XSL,
             XPath("/atom:entry/w_ns16:usage"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), XSL,
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)


      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), XSL,
             XPath("/atom:entry/w_ns17:usage"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), XSL,
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 9")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage_up_down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns18:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/atom:entry']) = 2")
    }

    scenario ("The sharedXPathWADL is processed checking wellformness, XSD, elemnts, and plain parameters, with remove dups and joinpath optimizations (preserve request body)") {
      Given("the sharedXPathWADL")
      When("The WADL is transalted")
      val config = TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true)
      config.preserveRequestBody = true
      val checker = builder.build(sharedXPathWADL, config)

      Then ("The following paths should hold")

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XSL,
             XPath("/atom:entry/w_ns16:usage"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up"),
             XPath("/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XSL,
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)

      assert(checker,Start, URL("servers"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML,
             ContentFail)

      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XSL,
             XPath("/atom:entry/w_ns17:usage"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up"),
             XPath("/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down"),
             XPath("/atom:entry/@only_usage_up_down"), Accept)

      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML, XSL,
             XPath("/atom:entry/w_ns18:usage"),
             XPath("/atom:entry/@only_usage"), Accept)

      assert(checker,Start, URL("nova"), URL("entries"),
             Method("POST"),
             ReqType("(application/atom\\+xml)(;.*)?"), WellXML,
             ContentFail)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 9")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage_up_down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns18:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/atom:entry']) = 2")
    }

    val sharedXPathWADL2 =
<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:foo="http://www.rackspace.com/foo/bar"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:atom="http://www.w3.org/2005/Atom">
    <resources base="http://localhost/">
        <resource path="y" type="#FOO"/>
        <resource path="x" type="#FOO #BAR"/>
    </resources>
    <resource_type id="FOO">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:bar">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:bar/@junk"/>
                </representation>
            </request>
        </method>
    </resource_type>
    <resource_type id="BAR">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:foo">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:foo/@junk"/>
                </representation>
            </request>
        </method>
    </resource_type>
</application>

    scenario ("The sharedXPathWADL2 is processed checking wellformness, elemnts, and plain parameters, but without optimizations") {
      Given("the sharedXPathWADL2")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(false, false, true, true, true, 1, true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"))

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 6")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
    }

    scenario ("The sharedXPathWADL2 is processed checking wellformness, elemnts, and plain parameters, with remove dups optimization") {
      Given("the sharedXPathWADL2")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"))

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
    }

    scenario ("The sharedXPathWADL2 is processed checking wellformness, elemnts, and plain parameters, with remove dups and joinpath optimizations") {
      Given("the sharedXPathWADL2")
      When("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), XSL)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar/@junk']) = 1")
    }

    scenario ("The sharedXPathWADL2 is processed checking wellformness, elemnts, and plain parameters, with remove dups and joinpath optimizations (preserve request body)") {
      Given("the sharedXPathWADL2")
      When("The WADL is transalted")
      val config = TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true)
      config.preserveRequestBody = true
      val checker = builder.build(sharedXPathWADL2, config)
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XSL)

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar/@junk']) = 1")
    }

    val sharedXPathWADL3 =
<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:foo="http://www.rackspace.com/foo/bar"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:check="http://www.rackspace.com/repose/wadl/checker"
             xmlns:atom="http://www.w3.org/2005/Atom">
    <resources base="http://localhost/">
        <resource path="y" type="#FOO"/>
        <resource path="x" type="#FOO #BAR"/>
    </resources>
    <resource_type id="FOO">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:bar">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:bar/@junk"/>
               <rax:preprocess>
                  <xsl:transform check:mergable="true" version="2.0">
                     <xsl:output method="xml" encoding="UTF-8"/>
                     <xsl:variable name="entry" select="/atom:entry"/>
                     <xsl:variable name="event" select="$entry/atom:content/event:event"/>
                     <xsl:template match="/">
                        <xsl:choose>
                           <xsl:when xmlns:p="http://docs.rackspace.com/event/identity/user"
                                     test="$event/p:product">
                              <xsl:variable name="product" select="$event/p:product"/>
                              <xsl:choose>
                                 <xsl:when test="$product[@version = '2']">
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type = 'UPDATE') then $product/@updatedAttributes else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is UPDATE, the updatedAttributes attribute is required.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type != 'UPDATE') then not($product/@updatedAttributes) else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is other than UPDATE, the updatedAttributes attribute should not be used.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </xsl:when>
                              </xsl:choose>
                           </xsl:when>
                        </xsl:choose>
                        <xsl:copy>
                           <xsl:apply-templates/>
                        </xsl:copy>
                     </xsl:template>
                  </xsl:transform>
               </rax:preprocess>
                </representation>
            </request>
        </method>
    </resource_type>
    <resource_type id="BAR">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:foo">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:foo/@junk"/>
               <rax:preprocess>
                  <xsl:stylesheet version="2.0">
                     <xsl:output method="xml" encoding="UTF-8"/>
                     <xsl:variable name="entry" select="/atom:entry"/>
                     <xsl:variable name="event" select="$entry/atom:content/event:event"/>
                     <xsl:template match="/">
                        <xsl:choose>
                           <xsl:when xmlns:p="http://docs.rackspace.com/event/identity/user"
                                     test="$event/p:product">
                              <xsl:variable name="product" select="$event/p:product"/>
                              <xsl:choose>
                                 <xsl:when test="$product[@version = '2']">
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type = 'UPDATE') then $product/@updatedAttributes else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is UPDATE, the updatedAttributes attribute is required.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type != 'UPDATE') then not($product/@updatedAttributes) else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is other than UPDATE, the updatedAttributes attribute should not be used.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </xsl:when>
                              </xsl:choose>
                           </xsl:when>
                        </xsl:choose>
                        <xsl:copy>
                           <xsl:apply-templates/>
                        </xsl:copy>
                     </xsl:template>
                  </xsl:stylesheet>
               </rax:preprocess>
                </representation>
            </request>
        </method>
    </resource_type>
</application>

    scenario ("The sharedXPathWADL3 is processed checking wellformness, elemnts, and plain parameters, preprocess extension, but without optimizations") {
      Given("the sharedXPathWADL3")
      When("The WADL is transalted")
      val config = TestConfig(removeDups = false, saxoneeValidation = false, wellFormed = true,
                              checkXSDGrammar = false, checkElements = true, xpathVersion = 2,
                              checkPlainParams = true, doXSDGrammarTransform = false,
                              enablePreProcessExtension = true, xslEngine = "XalanC",
                              joinXPathChecks = false, checkHeaders = false,
                              enableIgnoreXSDExtension = false, enableMessageExtension = false,
                              checkJSONGrammar = false, enableIgnoreJSONSchemaExtension = false,
                              enableRaxRolesExtension = false, preserveRequestBody = false)
      val checker = builder.build(sharedXPathWADL3, config)

      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), XSL, Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 6")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
    }

    scenario ("The sharedXPathWADL3 is processed checking wellformness, elemnts, and plain parameters, with remove dups optimization") {
      Given("the sharedXPathWADL3")
      When("The WADL is transalted")

      val config = TestConfig(removeDups = true, saxoneeValidation = false, wellFormed = true,
                              checkXSDGrammar = false, checkElements = true, xpathVersion = 2,
                              checkPlainParams = true, doXSDGrammarTransform = false,
                              enablePreProcessExtension = true, xslEngine = "XalanC",
                              joinXPathChecks = false, checkHeaders = false,
                              enableIgnoreXSDExtension = false, enableMessageExtension = false,
                              checkJSONGrammar = false, enableIgnoreJSONSchemaExtension = false,
                              enableRaxRolesExtension = false, preserveRequestBody = false)

      val checker = builder.build(sharedXPathWADL3, config)

      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), XSL, Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 6")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
    }

    scenario ("The sharedXPathWADL3 is processed checking wellformness, elemnts, and plain parameters, with remove dups and joinpath optimizations") {
      Given("the sharedXPathWADL3")
      When("The WADL is transalted")
      val config = TestConfig(removeDups = true, saxoneeValidation = false, wellFormed = true,
                              checkXSDGrammar = false, checkElements = true, xpathVersion = 2,
                              checkPlainParams = true, doXSDGrammarTransform = false,
                              enablePreProcessExtension = true, xslEngine = "XalanC",
                              joinXPathChecks = true, checkHeaders = false,
                              enableIgnoreXSDExtension = false, enableMessageExtension = false,
                              checkJSONGrammar = false, enableIgnoreJSONSchemaExtension = false,
                              enableRaxRolesExtension = false, preserveRequestBody = false)

      val checker = builder.build(sharedXPathWADL3, config)
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), XSL, Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']/xsl:transform/@chk:mergable) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']/xsl:stylesheet) = 1")
    }

    scenario ("The sharedXPathWADL3 is processed checking wellformness, elemnts, and plain parameters, with remove dups and joinpath optimizations (preserve request body)") {
      Given("the sharedXPathWADL3")
      When("The WADL is transalted")
      val config = TestConfig(removeDups = true, saxoneeValidation = false, wellFormed = true,
                              checkXSDGrammar = false, checkElements = true, xpathVersion = 2,
                              checkPlainParams = true, doXSDGrammarTransform = false,
                              enablePreProcessExtension = true, xslEngine = "XalanC",
                              joinXPathChecks = true, checkHeaders = false,
                              enableIgnoreXSDExtension = false, enableMessageExtension = false,
                              checkJSONGrammar = false, enableIgnoreJSONSchemaExtension = false,
                              enableRaxRolesExtension = false, preserveRequestBody = true)

      val checker = builder.build(sharedXPathWADL3, config)
      Then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XSL, Accept)

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), XSL, Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), XSL, Accept)

      And ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/foo:foo/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']//xsl:when[@test='/foo:bar/@junk']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']/xsl:transform/@chk:mergable) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']/xsl:stylesheet) = 1")
    }
  }
}
