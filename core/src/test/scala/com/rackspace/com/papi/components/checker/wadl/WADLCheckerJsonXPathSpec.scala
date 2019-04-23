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
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerJsonXPathSpec extends BaseCheckerSpec with LogAssertions {

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
    info ("I want to be able to transform a WADL which references JSON plain params into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (plain params disabled)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, false))
      Then("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 0")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (plain params enabled, but no required path)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="false"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" />
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 0")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (plain params enabled, but missing media type)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation>
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 0")
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with multiple required plain params") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"),
              JsonXPath("$body?test?stepType"), Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (dups on)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(true, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"),
              JsonXPath("$body?test?stepType"), Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (different reps)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                    </representation>
                    <representation mediaType="application/json">
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?stepType"), Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (different reps, multiple params)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                    </representation>
                    <representation mediaType="application/json">
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                       <param name="comment" style="plain" path="$body?test?comment" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 3")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 3")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?comment']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?stepType"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?stepType"),
              JsonXPath("$body?test?comment"), Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (different reps, multiple params on required == false)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                    </representation>
                    <representation mediaType="application/json">
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                       <param name="comment" style="plain" path="$body?test?comment" required="false"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?stepType"), Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with multiple required plain params, well form checks to false") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, false, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id"),
              JsonXPath("$body?test?stepType"), Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with a plain param, if XML is referensed namespaces should be preserved") {
      Given ("a WADL that contains a POST operation with json selecting XML in the JSON")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="parse-xml($body?test?xml)/tst:test/@id" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='parse-xml($body?test?xml)/tst:test/@id']) = 1")

      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[@type='JSON_XPATH']) = 'tst'")
      assert(checker, "namespace-uri-for-prefix('tst', /chk:checker/chk:step[@type='JSON_XPATH']) = 'http://www.rackspace.com/repose/wadl/checker/step/test'")


      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("parse-xml($body?test?xml)/tst:test/@id"), Accept)
    }


    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (rax:code extension)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                      xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" rax:code="401" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" rax:code="500" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", 401), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", 401),
              JsonXPath("$body?test?stepType", 500), Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (rax:message extension)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                      xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" rax:message="missing id" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" rax:message="missing stepType" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", "missing id"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", "missing id"),
              JsonXPath("$body?test?stepType", "missing stepType"), Accept)
    }

    scenario("The WADL contains a POST  operation accepting json with multiple required plain params (rax:message, rax:code extension)") {
      Given ("a WADL that contains a POST operation with json and multiple plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                      xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                      xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/json">
                       <param name="id" style="plain" path="$body?test?id" rax:message="missing id" rax:code="401" required="true"/>
                       <param name="stepType" style="plain" path="$body?test?stepType" rax:message="missing stepType" rax:code="500" required="true"/>
                    </representation>
                  </request>
               </method>
            </resource>
           </resources>
          </application>
      When("the wadl is translated")
      val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
      Then("The following assertions should hold")

      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @version='31']) = 2")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?id']) = 1")
      assert(checker, "count(/chk:checker/chk:step[@type='JSON_XPATH' and @match='$body?test?stepType']) = 1")

      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", "missing id", 401), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$body?test?id", "missing id", 401),
              JsonXPath("$body?test?stepType", "missing stepType", 500), Accept)
    }

  }
}

//4639
