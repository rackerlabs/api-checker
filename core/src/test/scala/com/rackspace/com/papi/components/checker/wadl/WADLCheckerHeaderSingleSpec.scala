/***
 *   Copyright 2016 Rackspace US, Inc.
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
import org.scalatest.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerHeaderSingleSpec extends BaseCheckerSpec with LogAssertions {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("xsl","http://www.w3.org/1999/XSL/Transform")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst","http://www.rackspace.com/repose/wadl/checker/step/test")


  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header nodes they are used in the next
    //  couple of tests.
    //
    def reqTypeAndHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header nodes.  They are used in the next couple of tests.
    //
    def wellFormedAndHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header nodes. They are used in the next couple of tests.
    //
    def xsdAndHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required non-repeat headers must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required non-repeat headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" repeating="false"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, non-repeating required headers must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, non-repeating required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" repeating="false"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, non-repeating required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled X-ROLES header should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, non-repeating required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true" default="c:creator"/>
      <param name="X-TEST" style="header" type="xsd:string" required="true" repeating="false" default="FOO"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true"  default="BAR"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    //
    //  Like reqTypeAndHeaderAssertions except we also check default values.
    //
    def reqTypeAndHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"), HeaderSingle("X-TEST", ".*"),
              HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  Like reqTypeAndHeaderAssertions, but assumes that duplicates
    //  have been removed
    //
    def reqTypeAndHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    //
    //  Like reqTypeAndHeaderDufsOnAssertion but with default headers enabled
    //

    def reqTypeAndHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    //
    //  Like wellFormedAndHeaderAssertions except we also check default values.
    //
    def wellFormedAndHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    //  Like wellFormedAndHeaderAssertions, but assumes that remove dups
    //  optimization is on, and duplicates have been removed
    //
    def wellFormedAndHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }


    //
    //  Like wellFormedAndHeaderDupsOnAssertions but with default headers enabled
    //

    def wellFormedAndHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    //  Like xsdAndHeaderAssertions except we also check default values.
    //

    def xsdAndHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    // Like xsdAndHeaderAssertions, but it's assumed that remove dups opt is on
    //
    def xsdAndHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    //  Like xsdAndHeaderDupsOnAnssertions but with default headers enabled
    //

    def xsdAndHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxCodeHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The header states should have the appropriate header codes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    // Like raxCodeHeaderAssertions but with default headers enabled
    //

    def raxCodeHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The header states should have the appropriate header codes")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403),Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    def raxMessageHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The header states should have the appropriate header codes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    // Like raxMessageHeaderAssertions but with default headers enabled
    //

    def raxMessageHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The header states should have the appropriate header codes")
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","foo", "X-FOO,foo,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "X-TEST, bad"),
              HeaderSingle("X-TEST2", ".*", "X-TEST2, bad"), HeaderSingle("X-FOO","bar", "X-FOO,bar,bad"),
              Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    def raxSameCodeHeaderDupsAssertion(checker : NodeSeq) : Unit = {
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar", 401), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    // Like raxSameCodeHeaderDupsAssertion but with default headers enabled
    //

    def raxSameCodeHeaderDupsAssertionWithDefaults(checker : NodeSeq) : Unit = {
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar", 401), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 401),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    def raxSameMessageHeaderDupsAssertion(checker : NodeSeq) : Unit = {
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar", "No!"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    //  Like raxSameMessageHeaderDupsAssertion but with default headers enabled
    //

    def raxSameMessageHeaderDupsAssertionWithDefaults(checker : NodeSeq) : Unit = {
      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar", "No!"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"), HeaderSingle("X-TEST2", ".*", "No!"),
              HeaderSingle("X-FOO","foo|bar"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and XSD header nodes they are used in the
    //  next couple of tests.
    //
    def reqTypeAndXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and XSD header nodes.  They are used in the next
    //  couple of tests.
    //
    def wellFormedAndXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // XSD header nodes. They are used in the next couple of tests.
    //
    def xsdAndXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests.
    //
    def reqTypeAndHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests.
    //
    def wellFormedAndHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests.
    //
    def xsdAndHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests.
    //
    def reqTypeAndHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqTypeFail)
    }

    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests.
    //
    def wellFormedAndHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests.
    //
    def xsdAndHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true"  default="FOO"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true"  default="BAR"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="foo" default="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled X-ROLES header should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <param name="X-TEST" style="header" type="xsd:string" required="true"  default="FOO"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true"  default="BAR"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="foo" default="foo"/>
      <param name="X-FOO" style="header" type="xsd:string" required="true"  fixed="bar"/>
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled x-RoLeS header should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="x-RoLeS" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <param name="X-TEST" style="header" type="xsd:string" required="true"  default="FOO"/>
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled X-ROLES header should be ignored (method)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled X-ROLES header should be ignored (mixed)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers must be ignored if feature is off, if raxroles is enabled X-ROLES header in response should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      <response>
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      </response>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val cfg = TestConfig(false, false, true, true, true, 1,
                           true, true, true, "XalanC",
                           false, true)
      cfg.enableRaxRolesExtension  = true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, cfg)
        reqTypeAndHeaderAssertions(checker)
        wellFormedAndHeaderAssertions(checker)
        xsdAndHeaderAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("No warning messages should be provided")
      assertEmpty(checkerLog)
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, errors to default headers must be ignored if feature is off (fixed mismatch)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="bar" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, errors to default headers must be ignored if feature is off (multiple defaults)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" default="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked, default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndHeaderAssertionsWithDefaults(checker)
      wellFormedAndHeaderAssertionsWithDefaults(checker)
      xsdAndHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked, default headers should be set, if rax roles is enabled X-ROLES header should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked, default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <param name="X-ROLES" style="header" type="xsd:string" required="true" 
      default="c:creator"/>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      config.enableRaxRolesExtension=true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, config)
        reqTypeAndHeaderAssertionsWithDefaults(checker)
        wellFormedAndHeaderAssertionsWithDefaults(checker)
        xsdAndHeaderAssertionsWithDefaults(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }


    scenario("If a default fixed header and default values are set, an error should occur") {
      Given ("A WADL where default parameters do not match fixed, and defaults are set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="bar" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      When ("the WADL is translated")
      Then ("A WADLException should be thrown")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build (inWADL, config)
        }
      }
      And ("There is a proper message detailing the error.")
      assert(checkerLog, "header param X-FOO")
      assert(checkerLog, "@default value \"bar\"")
      assert(checkerLog, "does not match @fixed value \"foo\"")
    }

    scenario("If multiple defaults are set for the same header value and defaults are enabled, an error should occur") {
      Given ("A WADL where there are multiple defualts set for for the same header, and defaults are set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" default="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      When ("the WADL is translated")
      Then ("A WADLException should be thrown")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build (inWADL, config)
        }
      }
      And ("There is a proper message detailing the error.")
      assert (checkerLog,"Multiple headers X-FOO have multiple @default vaules.")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (rax:code)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (rax:message)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="X-TEST, bad" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="X-TEST2, bad" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="X-FOO,foo,bad" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (rax:code, rax:message)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (rax:code, rax:message) default values should be ignored if the feature is truned off") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (rax:code, rax:message) default values should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked default values should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndHeaderAssertionsWithDefaults(checker)
      wellFormedAndHeaderAssertionsWithDefaults(checker)
      xsdAndHeaderAssertionsWithDefaults(checker)
      raxMessageHeaderAssertionsWithDefaults(checker)
      raxCodeHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on), if raxroles is enabled then X-ROLES header should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true"
      default="c:creator" />
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <param name="X-ROLES" style="header" type="xsd:string" required="true"
      default="c:creator" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.enableRaxRolesExtension=true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, config)
        reqTypeAndHeaderDupsOnAssertions(checker)
        wellFormedAndHeaderDupsOnAssertions(checker)
        xsdAndHeaderDupsOnAssertions(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on), default headers should be ignored if feature is off") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on), default headers should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked, default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndHeaderDupsOnAssertionsWithDefaults(checker)
      wellFormedAndHeaderDupsOnAssertionsWithDefaults(checker)
      xsdAndHeaderDupsOnAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on), default headers should be set, if raxroles is set X-ROLES header params should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked, default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-ROLES" style="header" type="xsd:string" required="true"
      default="c:creator" />
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <param name="X-ROLES" style="header" type="xsd:string" required="true"
      default="c:creator" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      config.enableRaxRolesExtension=true
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, config)
        reqTypeAndHeaderDupsOnAssertionsWithDefaults(checker)
        wellFormedAndHeaderDupsOnAssertionsWithDefaults(checker)
        xsdAndHeaderDupsOnAssertionsWithDefaults(checker)
        And("The following assertions should also hold:")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
        assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
      }
      And ("An appropriate warning message should be provided")
      assert(checkerLog, "you are not allowed to specify an X-ROLES header request parameter")
      assert(checkerLog, "The X-ROLES header parameter will be ignored")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code (same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="401" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="401" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      raxSameCodeHeaderDupsAssertion(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:message (same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No!" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      raxSameMessageHeaderDupsAssertion(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      raxSameMessageHeaderDupsAssertion(checker)
      raxSameCodeHeaderDupsAssertion(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (same)) default headers should be ignored if the feature is off") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderDupsOnAssertions(checker)
      wellFormedAndHeaderDupsOnAssertions(checker)
      xsdAndHeaderDupsOnAssertions(checker)
      raxSameMessageHeaderDupsAssertion(checker)
      raxSameCodeHeaderDupsAssertion(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (same)) default headers should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No!" rax:code="401" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No!" rax:code="401" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndHeaderDupsOnAssertionsWithDefaults(checker)
      wellFormedAndHeaderDupsOnAssertionsWithDefaults(checker)
      xsdAndHeaderDupsOnAssertionsWithDefaults(checker)
      raxSameMessageHeaderDupsAssertionWithDefaults(checker)
      raxSameCodeHeaderDupsAssertionWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code (different))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:message (different))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:message="X-TEST, bad" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="X-TEST2, bad" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="X-FOO,foo,bad" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (different))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (different)) default headers should be ignored if the feature is not set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      raxMessageHeaderAssertions(checker)
      raxCodeHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, required headers must be checked (remove dups on, rax:code, rax:message (different)) default headers should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, required headers must be checked default headers should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="X-TEST, bad" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="X-TEST2, bad" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" rax:message="X-FOO,foo,bad" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" rax:message="X-FOO,bar,bad" required="true" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config =TestConfig(true, false, true, true, true, 1,
                             true, true, true, "XalanC",
                             false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndHeaderAssertionsWithDefaults(checker)
      wellFormedAndHeaderAssertionsWithDefaults(checker)
      xsdAndHeaderAssertionsWithDefaults(checker)
      raxMessageHeaderAssertionsWithDefaults(checker)
      raxCodeHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-TEST3" style="header" type="xsd:string" required="false" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <param name="X-FOO" style="header" type="xsd:string" required="false" fixed="bar" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderAssertions(checker)
      wellFormedAndHeaderAssertions(checker)
      xsdAndHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndXSDHeaderAssertions(checker)
      wellFormedAndXSDHeaderAssertions(checker)
      xsdAndXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndXSDHeaderAssertions(checker)
      wellFormedAndXSDHeaderAssertions(checker)
      xsdAndXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header and header must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header and header must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderXSDHeaderAssertions(checker)
      wellFormedAndHeaderXSDHeaderAssertions(checker)
      xsdAndHeaderXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 6")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header and header must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header and header must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderXSDHeaderAssertions(checker)
      wellFormedAndHeaderXSDHeaderAssertions(checker)
      xsdAndHeaderXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 6")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderXSDHeader2Assertions(checker)
      wellFormedAndHeaderXSDHeader2Assertions(checker)
      xsdAndHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 8")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers, non req headers should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers, nonrequired headers should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndHeaderXSDHeader2Assertions(checker)
      wellFormedAndHeaderXSDHeader2Assertions(checker)
      xsdAndHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 8")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers, opt on") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD header and header must be checked, multiple similar Headers, opt on")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="PUT">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      true, true))
      reqTypeAndHeaderXSDHeader2Assertions(checker)
      wellFormedAndHeaderXSDHeader2Assertions(checker)
      xsdAndHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header nodes on PUT request they are used
    //  in the next couple of tests.
    //
    def reqTypeAndReqHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    def reqTypeAndReqHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  Like reqTypeAndReqHeaderAssertions, but we assume remove dups optimization
    //
    def reqTypeAndReqHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    def reqTypeAndReqHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),  SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header nodes.  They are used in the next couple of tests.
    //
    def wellFormedAndReqHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    def wellFormedAndReqHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    //  Like wellFormedAndReqHeaderAssertions, but we assume remove dups on optimization
    //
    def wellFormedAndReqHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }


    def wellFormedAndReqHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }


    //
    // The following assertions are used to test XSD, ContentError, and
    // header nodes. They are used in the next couple of tests.
    //
    def xsdAndReqHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    def xsdAndReqHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","bar"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    // Like xsdAndReqHeaderAssertions, but we assume remove dups optimization
    //
    def xsdAndReqHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def xsdAndReqHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*"), HeaderSingle("X-TEST2", ".*"), HeaderSingle("X-FOO","foo|bar"),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxCodeReqHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain header assertions with correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxCodeReqHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain header assertions with correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","foo", 402), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401), HeaderSingle("X-TEST2", ".*", 404),
              HeaderSingle("X-FOO","bar", 403), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxCodeReqHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain header assertions with the correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401),
              HeaderSingle("X-TEST2", ".*", 401), HeaderSingle("X-FOO","foo|bar", 401),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", 401),
              HeaderSingle("X-TEST2", ".*", 401), HeaderSingle("X-FOO","foo|bar", 401),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxCodeReqHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain header assertions with the correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401),
              HeaderSingle("X-TEST2", ".*", 401), HeaderSingle("X-FOO","foo|bar", 401),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", 401),
              HeaderSingle("X-TEST2", ".*", 401), HeaderSingle("X-FOO","foo|bar", 401),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxMessageReqHeaderDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain header assertions with the correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No!"),
              HeaderSingle("X-TEST2", ".*", "No!"), HeaderSingle("X-FOO","foo|bar", "No!"),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No!"),
              HeaderSingle("X-TEST2", ".*", "No!"), HeaderSingle("X-FOO","foo|bar", "No!"),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxMessageReqHeaderDupsOnAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain header assertions with the correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"),
              HeaderSingle("X-TEST2", ".*", "No!"), HeaderSingle("X-FOO","foo|bar", "No!"),
              ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No!"),
              HeaderSingle("X-TEST2", ".*", "No!"), HeaderSingle("X-FOO","foo|bar", "No!"),
              ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxMessageReqHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain header assertions with correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","foo", "No2"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","bar", "No3"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","foo", "No2"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","bar", "No3"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def raxMessageReqHeaderAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain header assertions with correct error code")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","foo", "No2"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","bar", "No3"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","foo", "No2"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST", "FOO"), SetHeader("X-TEST2","BAR"), SetHeader("X-FOO","foo"),
              HeaderSingle("X-TEST", ".*", "No1"), HeaderSingle("X-TEST2", ".*", "No4"),
              HeaderSingle("X-FOO","bar", "No3"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and XSD header nodes they are used in the
    //  next couple of tests.
    //
    def reqTypeAndReqXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and XSD header nodes.  They are used in the next
    //  couple of tests.
    //
    def wellFormedAndReqXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // XSD header nodes. They are used in the next couple of tests.
    //
    def xsdAndReqXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }


    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests.
    //
    def reqTypeAndReqHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }


    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests.
    //
    def wellFormedAndReqHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests.
    //
    def xsdAndReqHeaderXSDHeaderAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests.
    //
    def reqTypeAndReqHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqTypeFail)
    }

    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests.
    //
    def wellFormedAndReqHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests.
    //
    def xsdAndReqHeaderXSDHeader2Assertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests.
    //
    def reqTypeAndReqHeaderXSDHeader2MixAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqTypeFail)
    }

    def reqTypeAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqTypeFail)
    }



    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests.
    //
    def wellFormedAndReqHeaderXSDHeader2MixAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }


    def wellFormedAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }



    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests.
    //
    def xsdAndReqHeaderXSDHeader2MixAssertions(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    def xsdAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), SetHeader("X-TEST-INT", "99"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("c"), SetHeader("X-TEST-INT", "999"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), SetHeader("X-TEST-OTHER", "2015-11-28"),
              HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked default values should be ignored if the feature is not set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked default values should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked default values should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderAssertionsWithDefaults(checker)
      xsdAndReqHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }



    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (method ref)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method href="#headerMethod"/>
      <method href="#postOnAB"/>
      </resource>
      <resource path="/c">
      <method href="#postOnC"/>
      <method href="#getOnC"/>
      </resource>
      </resources>
      <method id="headerMethod" name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method id="postOnAB" name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      <method id="postOnC" name="POST">
      <request>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method id="getOnC" name="GET"/>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (method ref) with default values set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked with default values set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method href="#headerMethod"/>
      <method href="#postOnAB"/>
      </resource>
      <resource path="/c">
      <method href="#postOnC"/>
      <method href="#getOnC"/>
      </resource>
      </resources>
      <method id="headerMethod" name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method id="postOnAB" name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      <method id="postOnC" name="POST">
      <request>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method id="getOnC" name="GET"/>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderAssertionsWithDefaults(checker)
      xsdAndReqHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (rax:code)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxCodeReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (rax:message)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No1" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No4" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No2" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxMessageReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (rax:code, rax:message)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No1" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="No4" required="true" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" rax:message="No2" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxCodeReqHeaderAssertions(checker)
      raxMessageReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (rax:code, rax:message) with defaults set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked with defaults set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No1" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="No4" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="402" rax:message="No2" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string"  rax:code="403" rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderAssertionsWithDefaults(checker)
      xsdAndReqHeaderAssertionsWithDefaults(checker)
      raxCodeReqHeaderAssertionsWithDefaults(checker)
      raxMessageReqHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderDupsOnAssertions(checker)
      wellFormedAndReqHeaderDupsOnAssertions(checker)
      xsdAndReqHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code(same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderDupsOnAssertions(checker)
      wellFormedAndReqHeaderDupsOnAssertions(checker)
      xsdAndReqHeaderDupsOnAssertions(checker)
      raxCodeReqHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:message(same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No!" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="No!" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="No!" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderDupsOnAssertions(checker)
      wellFormedAndReqHeaderDupsOnAssertions(checker)
      xsdAndReqHeaderDupsOnAssertions(checker)
      raxMessageReqHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code, rax:message(same))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderDupsOnAssertions(checker)
      wellFormedAndReqHeaderDupsOnAssertions(checker)
      xsdAndReqHeaderDupsOnAssertions(checker)
      raxCodeReqHeaderDupsOnAssertions(checker)
      raxMessageReqHeaderDupsOnAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code, rax:message(same)) defaults should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked and defaults should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderDupsOnAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderDupsOnAssertionsWithDefaults(checker)
      xsdAndReqHeaderDupsOnAssertionsWithDefaults(checker)
      raxCodeReqHeaderDupsOnAssertionsWithDefaults(checker)
      raxMessageReqHeaderDupsOnAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code(diff))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxCodeReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:message(diff))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:message="No1" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:message="No4" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="No2" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxMessageReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code, rax:message(diff))") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No1" required="true" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="No4" required="true" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" rax:message="No2" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      raxCodeReqHeaderAssertions(checker)
      raxMessageReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked (dups on, rax:code, rax:message(diff)) with default values set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT that must be checked with default values set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:rax="http://docs.rackspace.com/api"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No1" required="true" default="FOO" />
      <param name="X-TEST2" style="header" type="xsd:string" rax:code="404" rax:message="No4" required="true" default="BAR" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="402" rax:message="No2" required="true" fixed="foo" default="foo" />
      <param name="X-FOO" style="header" type="xsd:string" rax:code="403" rax:message="No3" required="true" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(true, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderAssertionsWithDefaults(checker)
      xsdAndReqHeaderAssertionsWithDefaults(checker)
      raxCodeReqHeaderAssertionsWithDefaults(checker)
      raxMessageReqHeaderAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required header on a PUT that must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required header on a PUT must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <param name="X-TEST2" style="header" type="xsd:string" required="true" />
      <param name="X-TEST3" style="header" type="xsd:string" required="false" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-FOO" style="header" type="xsd:string" required="true" fixed="bar" />
      <param name="X-FOO" style="header" type="xsd:string" required="false" fixed="bar" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderAssertions(checker)
      wellFormedAndReqHeaderAssertions(checker)
      xsdAndReqHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 7")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqXSDHeaderAssertions(checker)
      wellFormedAndReqXSDHeaderAssertions(checker)
      xsdAndReqXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqXSDHeaderAssertions(checker)
      wellFormedAndReqXSDHeaderAssertions(checker)
      xsdAndReqXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and header must be checked") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and header must be checked")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeaderAssertions(checker)
      wellFormedAndReqHeaderXSDHeaderAssertions(checker)
      xsdAndReqHeaderXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 6")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required XSD request header and request header must be checked, non-req should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required XSD request header and request header must be checked, non-req should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
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
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeaderAssertions(checker)
      wellFormedAndReqHeaderXSDHeaderAssertions(checker)
      xsdAndReqHeaderXSDHeaderAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 6")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2Assertions(checker)
      wellFormedAndReqHeaderXSDHeader2Assertions(checker)
      xsdAndReqHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 8")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, non req headers should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, nonrequired headers should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2Assertions(checker)
      wellFormedAndReqHeaderXSDHeader2Assertions(checker)
      xsdAndReqHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 8")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, opt on") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, opt on")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      true, true))
      reqTypeAndReqHeaderXSDHeader2Assertions(checker)
      wellFormedAndReqHeaderXSDHeader2Assertions(checker)
      xsdAndReqHeaderXSDHeader2Assertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, mixed, multiple similar Headers") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertions(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertions(checker)
      xsdAndReqHeaderXSDHeader2MixAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 9")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, mixed, multiple similar Headers default values should be ignored if feature is not set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" default="99" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" default="999" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" default="2015-11-28" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertions(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertions(checker)
      xsdAndReqHeaderXSDHeader2MixAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 9")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, mixed, multiple similar Headers default values should be set") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers default values should be set")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" default="99" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" default="999" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" default="2015-11-28" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val config = TestConfig(false, false, true, true, true, 1,
                              true, true, true, "XalanC",
                              false, true)
      config.setParamDefaults=true
      val checker = builder.build (inWADL, config)
      reqTypeAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker)
      xsdAndReqHeaderXSDHeader2MixAssertionsWithDefaults(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 9")
    }


    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, mixed, non req headers should be ignored") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, nonrequired headers should be ignored")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" />
      <representation mediaType="application/xml"/>
      <representation mediaType="application/json"/>
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertions(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertions(checker)
      xsdAndReqHeaderXSDHeader2MixAssertions(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 3")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 9")
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests. Some Header nodes do not have a ReqType.
    //
    def reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
    }

    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests. Some Header nodes do not have a ReqType.
    //
    def wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests. Some Header nodes do not have a ReqType.
    //
    def xsdAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", ".*"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, mixed, non req headers should be ignored, checks should occur even if no represetation type is specified.") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, nonrequired headers should be ignored. No representation types are sepecified.")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" />
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker)
      xsdAndReqHeaderXSDHeader2MixAssertionsNoReqType(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, and header and xsd header nodes they are used
    //  in the next couple of tests. Some Header nodes do not have a ReqType.
    //  Other headers are of differnt types but have the same name.
    //
    def reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
    }


    //
    //  Like reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName but remove dups enabled
    //
    def reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo|bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
    }


    //
    //  The following assertions are used to test WellFormXML,
    //  ContentError, and header and xsd header nodes.  They are used in
    //  the next couple of tests. Some Header nodes do not have a ReqType.
    //  Other headers are of differnt types but have the same name.
    //
    def wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }

    //
    //  Like wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName but with remove dups enabled
    //

    def wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo|bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderXSDSingle("X-TEST-OTHER", "xsd:date"), Accept)
      assert (checker, Start, URL("c"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Method("POST"), HeaderSingle("X-TEST-OTHER", "22"), Accept)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo|bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
    }

    //
    // The following assertions are used to test XSD, ContentError, and
    // header and xsd header nodes. They are used in the next couple of
    // tests. Some Header nodes do not have a ReqType.
    //  Other headers are of differnt types but have the same name.
    //
    def xsdAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    //
    // Like xsdAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups but with remove dups enabled
    //

    def xsdAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker : NodeSeq) : Unit = {
      And("The machine should cantain paths to XSD types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderSingle("X-TEST", "foo|bar"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), HeaderXSDSingle("X-TEST", "xsd:int"), HeaderXSDSingle("X-TEST-INT", "xsd:int"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XSD, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, mixed (with same name), non req headers should be ignored, checks should occur even if no represetation type is specified.") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers (with same name), nonrequired headers should be ignored. No representation types are sepecified.")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-TEST" style="header" type="xsd:string" required="true" fixed="bar" />
      <param name="X-TEST" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:int" required="true" fixed="22" />
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker)
      xsdAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameName(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 5")
    }

    scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers, mixed (with same name), non req headers should be ignored, checks should occur even if no represetation type is specified. (dups on)") {
      Given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, a required request XSD header and request header must be checked, multiple similar Headers (with same name), nonrequired headers should be ignored. No representation types are sepecified. (dups on)")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <grammars>
      <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
      </grammars>
      <resources base="https://test.api.openstack.com">
      <resource path="/a/b">
      <method name="PUT">
      <request>
      <param name="X-TEST" style="header" type="xsd:string" required="true" fixed="foo" />
      <param name="X-TEST" style="header" type="xsd:string" required="true" fixed="bar" />
      <param name="X-TEST" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:string" required="false" />
      </request>
      </method>
      <method name="POST">
      <request>
      <representation mediaType="application/xml"/>
      </request>
      </method>
      </resource>
      <resource path="/c">
      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" />
      <param name="X-TEST-OTHER-INT" style="header" type="xsd:int" required="false" />
      <method name="POST">
      <request>
      <param name="X-TEST-OTHER" style="header" type="xsd:date" required="true" />
      <param name="X-TEST-OTHER" style="header" type="xsd:int" required="true" fixed="22" />
      </request>
      </method>
      <method name="GET"/>
      </resource>
      </resources>
      </application>
      register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
               XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1,
                                                      true, true, true, "XalanC",
                                                      false, true))
      reqTypeAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker)
      wellFormedAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker)
      xsdAndReqHeaderXSDHeader2MixAssertionsNoReqTypeSameNameDups(checker)
      And("The following assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
    }
  }
}
