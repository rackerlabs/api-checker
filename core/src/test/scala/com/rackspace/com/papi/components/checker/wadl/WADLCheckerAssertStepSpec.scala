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
package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.{LogAssertions, TestConfig}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerAssertStepSpec extends BaseCheckerSpec with LogAssertions {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("xsl","http://www.w3.org/1999/XSL/Transform")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst","http://www.rackspace.com/repose/wadl/checker/step/test")


  //
  //  Configs...
  //

  val assertDisabled = {
    val tc = TestConfig()
    tc.enableAssertExtension = false
    tc.removeDups = false
    tc
  }

  val assertEnabled = {
    val tc = TestConfig()
    tc.enableAssertExtension = true
    tc.removeDups = false
    tc
  }

  val assertEnabledRemoveDups = {
    val tc = TestConfig()
    tc.enableAssertExtension = true
    tc.removeDups = true
    tc
  }

  //
  // WADLs...
  //
  val assertAtMethodLevel =
            <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
            </resource>
           </resources>
          </application>

  val assertAtMethodMultiRep =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
            </resource>
           </resources>
          </application>

  val assertAtResourceLevel =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:assert test="contains($req:uri,'/a')"/>
            </resource>
           </resources>
          </application>

  val assertAtResourceLevelApplyChildrenFalse =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:assert test="contains($req:uri,'/a')" applyToChildren="false"/>
            </resource>
           </resources>
          </application>

  val assertAtResourceLevelApplyChildrenTrue =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:assert test="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
           </resources>
          </application>


  val assertAtResourceLevelApplyChildrenTrueRepAsserts =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:test="http://test/foo"
                     >
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml">
                      <rax:assert test="/test:test/@foo == 'bar'"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                      <rax:assert test="'text/x-yaml' = req:headers('Accept', true())"/>
                    </representation>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json">
                            <rax:assert test="'application/json' = req:headers('Accept', true())"/>
                       </representation>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:assert test="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
           </resources>
          </application>

  val assertAtResourceLevelApplyChildrenTrueRepResourcesAsserts =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:test="http://test/foo">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml">
                      <rax:assert test="/test:test/@foo == 'bar'"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                      <rax:assert test="'text/x-yaml' = req:headers('Accept', true())"/>
                    </representation>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"  message="Not auth to send X-AUTH unless it's 'foo!'" code="403"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json">
                            <rax:assert test="'application/json' = req:headers('Accept', true())" message="You should specify JSON!"/>
                       </representation>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:assert test="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
            <rax:assert test="not(empty($req:uri))"/>
           </resources>
          </application>

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references rax:assert extensions into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    scenario ("The WADL contains a misplaced rax:assert") {
      Given("A WADL with a misplaced rax:assert")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                  </request>
                  <!-- Notice accert at method level, but not request level -->
                  <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
               </method>
            </resource>
           </resources>
          </application>

      When("the wadl is translated")
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, assertEnabled)
        Then("No asserts should exist in the checker format")
        assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      }
      And ("There should be a warning in the log that denotes that denotes the bad placement")
      assert(checkerLog, "bad placement for <rax:assert")
      assert(checkerLog, "req:header('X-AUTH') = 'foo!'")
    }

    scenario ("The WADL contains a method with a rax:assert at the request level (rax:assert disabled)") {
      Given("A WADL with a GET operation with a rax:assert at the request level, but rax:assert disabled")
      val inWADL = assertAtMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), Accept)
    }

    scenario ("The WADL contains a method with a rax:assert at the request level (rax:assert enabled)") {
      Given("A WADL with a GET operation with a rax:assert at the request level, but rax:assert enabled")
      val inWADL = assertAtMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 1")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ContentFail)
    }

    scenario ("The WADL contains a method with a rax:assert at the request level, XML, JSON, and YAML representations (rax:assert disabled)") {
      Given("A WADL with a GET operation with a rax:assert at the request level, but rax:assert disabled")
      val inWADL = assertAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a method with a rax:assert at the request level, XML, JSON, and YAML representations (rax:assert enabled)") {
      Given("A WADL with a GET operation with a rax:assert at the request level, but rax:assert enabled")
      val inWADL = assertAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 3")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a method with a rax:assert at the request level, XML, JSON, and YAML representations (rax:assert enabled, remove dups)") {
      Given("A WADL with a GET operation with a rax:assert at the request level, but rax:assert enabled, remove dups")
      val inWADL = assertAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 1")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert disabled)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert disabled")
      val inWADL = assertAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled)") {
      Given("A WADL with a with a rax:assert at the resource level, but rax:assert enabled")
      val inWADL = assertAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 9")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, removedups)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled, removedups")
      val inWADL = assertAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }


    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert disabled, applyChildrenFalse)") {
      Given("A WADL with a rax:assert at the request level, but rax:assert disabled, applyChildrenFalse")
      val inWADL = assertAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }


    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenFalse)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChldernFalse")
      val inWADL = assertAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 9")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, removedups, applyChildrenFalse)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChldrenFalse")
      val inWADL = assertAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert disabled, applyChildrenTrue)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert disabled applyChlidrenTrue")
      val inWADL = assertAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }


    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenTrue)") {
      Given("A WADL with a rax:assert at the resource level applyChildernTrue")
      val inWADL = assertAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 16")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, removedups, applyChildrenTrue)") {
      Given("A WADL with a rax:assert at the request level, but rax:assert enabled, removeDups, and applyChlidrenTrue")
      val inWADL = assertAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert disabled, applyChildrenTrue, asserts at representation)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert disabled applyChlidrenTrue, and asserts at representation")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenTrue, asserts at representation)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChlidrenTrue, and asserts at representation")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='ASSERT']) = 19")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@match,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("/test:test/@foo == 'bar'"),
             RaxAssert("req:header('X-AUTH') = 'foo!'"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("'text/x-yaml' = req:headers('Accept', true())"),
             RaxAssert("req:header('X-AUTH') = 'foo!'"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("'application/json' = req:headers('Accept', true())"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenTrue, asserts at representation, removeDups)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChlidrenTrue, and asserts at representation with removeDups")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 5")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@match,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("/test:test/@foo == 'bar'"),
             RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("req:header('X-AUTH') = 'foo!'"),
             RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("'text/x-yaml' = req:headers('Accept', true())"),
             RaxAssert("req:header('X-AUTH') = 'foo!'"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("'application/json' = req:headers('Accept', true())"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert disabled, applyChildrenTrue, asserts at representation and resources level)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert disabled applyChlidrenTrue, and asserts at representation and resources level")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenTrue, asserts at representation and resources level)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChlidrenTrue, and asserts at representation and resources level")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 32")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@match,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("/test:test/@foo == 'bar'"),
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("'text/x-yaml' = req:headers('Accept', true())"),
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("'application/json' = req:headers('Accept', true())","You should specify JSON!"),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }

    scenario ("The WADL contains a resource with a rax:assert at the resource level, XML, JSON, and YAML representations in methods (rax:assert enabled, applyChildrenTrue, asserts at representation and resources level, remove Dups on)") {
      Given("A WADL with a rax:assert at the resource level, but rax:assert enabled applyChlidrenTrue, and asserts at representation and resources level remove Dups on")
      val inWADL = assertAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, assertEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='ASSERT']) = 6")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@match,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("/test:test/@foo == 'bar'"),
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("'text/x-yaml' = req:headers('Accept', true())"),
             RaxAssert("req:header('X-AUTH') = 'foo!'","Not auth to send X-AUTH unless it's 'foo!'", 403),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxAssert("'application/json' = req:headers('Accept', true())","You should specify JSON!"),
             RaxAssert("contains($req:uri,'/a')"), RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxAssert("contains($req:uri,'/a')"),
             RaxAssert("not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), ContentFail)
    }
  }
}
