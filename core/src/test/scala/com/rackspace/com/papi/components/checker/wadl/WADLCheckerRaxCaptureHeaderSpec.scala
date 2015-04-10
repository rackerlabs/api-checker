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

@RunWith(classOf[JUnitRunner])
class WADLCheckerRaxCaptureHeaderSpec extends BaseCheckerSpec {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("xsd", "http://www.w3.org/2001/XMLSchema")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format when that wadl uses the rax-capture header extension") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references the rax:captureHeader extenson into a ")
    info ("a description of a machine that can correctly set appropriate headers")
    info ("so that an API validator can capture wadl parameter values in headers")

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
                        <param id="foo"  style="plain" required="true" path="/tst:e/@foo"
                            rax:captureHeader="X-FOO"/>
                    </representation>
                </request>
            </method>
            <method name="PUT" rax:roles="put:admin">
                <request>
                    <representation mediaType="application/xml">
                        <param id="foo2pu" style="plain" required="true" path="/tst:e/@foo2"/>
                        <param id="foopu"  style="plain" required="true" path="/tst:e/@foo"
                            rax:captureHeader="X-FOO"/>
                        <param id="foo3pu" style="plain" required="true" path="/tst:e/@foo3"/>
                    </representation>
                </request>
            </method>
            <method name="PATCH">
                <request>
                    <representation mediaType="application/xml">
                        <param id="foopa"  style="plain" required="true" path="/tst:e/@foo"
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
                           rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="OtherHeader" style="header" required="true"
                            type="xs:int"/>
                </request>
            </method>
            <method name="POST">
                <request>
                    <param name="MyHeader" style="header" required="true" fixed="FOO"
                        rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="MyHeader" style="header" required="true" fixed="FAR"
                        rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="MyHeader" style="header" required="true" fixed="OUT"
                        rax:captureHeader="X-OTHER-HEADER"/>
                </request>
            </method>
            <method name="PUT">
                <request>
                    <param name="MyHeader" style="header" required="true" fixed="FOO"
                        type="xs:string" rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="MyHeader" style="header" required="true" fixed="FAR"
                        type="xs:string" />
                    <param name="MyHeader" style="header" required="true" fixed="OUT"
                        type="xs:string" />
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="MyHeader" style="header" required="true" fixed="1"
                        type="xs:int" rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="MyHeader" style="header" required="true" fixed="2"
                        type="xs:int" rax:captureHeader="X-OTHER-HEADER" />
                    <param name="MyHeader" style="header" required="true" fixed="3"
                        type="xs:int" rax:captureHeader="X-OTHER-HEADER" />
                </request>
            </method>
            <method name="PATCH">
                <request>
                    <param name="MyHeader" style="header" required="true" fixed="1"
                        type="xs:int" rax:captureHeader="X-OTHER-HEADER"/>
                    <param name="MyHeader" style="header" required="true" fixed="2"
                        type="xs:int" />
                    <param name="MyHeader" style="header" required="true" fixed="3"
                        type="xs:int" />
                </request>
            </method>
        </resource>
    </resources>
    </application>

    scenario ("A WADL with captureHeaders disabled should not set any captureHeaders"){
      Given ("A WADL with captureHeaders disabled")
      When("The WADL is translated with captureHeaders disabled")
      val checker = builder.build(captureHeaderWADL, baseConfig)
      Then ("The checker should not contain captureHeader attributes")
      assert (checker,"not(//@captureHeader)")
    }

    scenario ("A WADL with captureHeaders enabled should appropreately set captureHeader attributes"){
      Given ("A WADL with captureHeaders disabled")
      When("The WADL is translated with captureHeaders disabled")
      val checker = builder.build(captureHeaderWADL, baseWithCaptureHeaders)
      Then ("The checker should not contain captureHeader attributes")
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("GET"), HeaderWithCapture("MyHeader", ".*", "X-OTHER-HEADER"),
              HeaderXSD("OtherHeader", "xs:int"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"), HeaderAnyWithCapture("MyHeader", "FOO", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"), HeaderAnyWithCapture("MyHeader", "FAR", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"), HeaderAnyWithCapture("MyHeader", "OUT", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"), HeaderAnyWithCapture("MyHeader", "FOO", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"), HeaderAnyWithCapture("MyHeader", "FAR", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"), HeaderAnyWithCapture("MyHeader", "OUT", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"), HeaderAnyWithCapture("MyHeader", "1", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"), HeaderAnyWithCapture("MyHeader", "2", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"), HeaderAnyWithCapture("MyHeader", "3", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"), HeaderAnyWithCapture("MyHeader", "1", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"), HeaderAnyWithCapture("MyHeader", "2", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"), HeaderAnyWithCapture("MyHeader", "3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLXSDWithCapture("xs:int", "X-SUB-RESOURCE2"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("POST"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo2", ""), XPathWithCapture("/tst:e/@foo3", ""),
              XPathWithCapture("/tst:e/@foo", "X-FOO"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PUT"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo2", ""), XPathWithCapture("/tst:e/@foo", "X-FOO"),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PATCH"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo2", ""),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)
    }

    scenario ("A WADL with captureHeaders enabled should appropreately set captureHeader attributes (removeDups)"){
      Given ("A WADL with captureHeaders disabled")
      When("The WADL is translated with captureHeaders disabled")
      val checker = builder.build(captureHeaderWADL, baseWithRemoveDups)
      Then ("The checker should not contain captureHeader attributes")
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("GET"),
              HeaderWithCapture("MyHeader", ".*", "X-OTHER-HEADER"),
              HeaderXSD("OtherHeader", "xs:int"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"),
              HeaderAnyWithCapture("MyHeader", "FOO|FAR|OUT", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FOO", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FAR|OUT", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"),
              HeaderAnyWithCapture("MyHeader", "1|2|3", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "1", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "2|3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLXSDWithCapture("xs:int", "X-SUB-RESOURCE2"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("POST"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo2", ""), XPathWithCapture("/tst:e/@foo3", ""),
              XPathWithCapture("/tst:e/@foo", "X-FOO"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PUT"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo2", ""), XPathWithCapture("/tst:e/@foo", "X-FOO"),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PATCH"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo2", ""),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)
    }

    scenario ("A WADL with captureHeaders enabled should appropreately set captureHeader attributes (joinXPath)"){
      Given ("A WADL with captureHeaders disabled")
      When("The WADL is translated with captureHeaders disabled")
      val checker = builder.build(captureHeaderWADL, baseWithJoinXPaths)
      Then ("The checker should not contain captureHeader attributes")
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("GET"),
              HeaderWithCapture("MyHeader", ".*", "X-OTHER-HEADER"),
              HeaderXSD("OtherHeader", "xs:int"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"),
              HeaderAnyWithCapture("MyHeader", "FOO|FAR|OUT", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FOO", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FAR|OUT", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"),
              HeaderAnyWithCapture("MyHeader", "1|2|3", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "1", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "2|3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLXSDWithCapture("xs:int", "X-SUB-RESOURCE2"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("POST"),
              ReqType("(application/xml)(;.*)?"), XSL, XPathWithCapture("/tst:e/@foo", "X-FOO"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PUT"),
              ReqType("(application/xml)(;.*)?"), XSL, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PATCH"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo2", ""),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)
    }

    scenario ("A WADL with captureHeaders enabled should appropreately set captureHeader attributes (removedups, joinXPath)"){
      Given ("A WADL with captureHeaders disabled")
      When("The WADL is translated with captureHeaders disabled")
      val checker = builder.build(captureHeaderWADL, baseWithJoinXPathsAndRemoveDups)
      Then ("The checker should not contain captureHeader attributes")
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("GET"),
              HeaderWithCapture("MyHeader", ".*", "X-OTHER-HEADER"),
              HeaderXSD("OtherHeader", "xs:int"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("POST"),
              HeaderAnyWithCapture("MyHeader", "FOO|FAR|OUT", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FOO", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PUT"),
              HeaderAnyWithCapture("MyHeader", "FAR|OUT", ""), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("DELETE"),
              HeaderAnyWithCapture("MyHeader", "1|2|3", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "1", "X-OTHER-HEADER"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource1", ""), Method("PATCH"),
              HeaderAnyWithCapture("MyHeader", "2|3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource2", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLXSDWithCapture("xs:int", "X-SUB-RESOURCE2"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)
      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource3", ""), URLWithCapture(".*", "X-SUB-RESOURCE"),
              URLWithCapture(".*", "X-SUB-RESOURCE"), Method("GET"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("POST"),
              ReqType("(application/xml)(;.*)?"), XSL, XPathWithCapture("/tst:e/@foo", "X-FOO"), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PUT"),
              ReqType("(application/xml)(;.*)?"), XSL, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo3", ""), Accept)

      assert (checker, Start, URLWithCapture("path", ""), URLWithCapture("to", ""), URLWithCapture("resource4", ""), Method("PATCH"),
              ReqType("(application/xml)(;.*)?"), WellXML, XPathWithCapture("/tst:e/@foo", "X-FOO"), XPathWithCapture("/tst:e/@foo2", ""),
              XPathWithCapture("/tst:e/@foo3", ""), Accept)
    }

  }
}
