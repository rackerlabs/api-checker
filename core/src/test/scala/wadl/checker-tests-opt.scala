
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
      given("the sharedXPathWADL")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(false, false, true, true, true, 1, true))
      then ("The following paths should hold")

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

      and ("The Following counts should hold")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 6")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 16")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns17:usage/w_ns17:up/w_ns17:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage_up_down']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns16:usage/w_ns16:up/w_ns16:down']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/w_ns18:usage']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/atom:entry/@only_usage']) = 2")
    }

    scenario ("The sharedXPathWADL is processed checking wellformness, XSD, elemnts, and plain parameters, with remove dups optimization") {
      given("the sharedXPathWADL")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true))
      then ("The following paths should hold")

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

      and ("The Following counts should hold")
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
      given("the sharedXPathWADL")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
      then ("The following paths should hold")

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

      and ("The Following counts should hold")
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
      given("the sharedXPathWADL2")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(false, false, true, true, true, 1, true))
      then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"))

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"), Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      and ("The Following counts should hold")
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
      given("the sharedXPathWADL2")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true))
      then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"))

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      and ("The Following counts should hold")
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
      given("the sharedXPathWADL2")
      when("The WADL is transalted")
      val checker = builder.build(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
      then ("The following paths should hold")

      assert(checker,Start, URL("y"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), XSL)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:bar"),
             XPath("/foo:bar/@junk"), Accept)

      assert(checker,Start, URL("x"),  Method("POST"),
             ReqType("(application/xml)(;.*)?"), WellXML, XPath("/foo:foo"),
             XPath("/foo:foo/@junk"), Accept)

      and ("The Following counts should hold")
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
  }
}
