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

import com.rackspace.com.papi.components.checker.TestConfig
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

//
//  Test for bug fix where preproc was turning on WellForm check in
//  appropriately.
//

@RunWith(classOf[JUnitRunner])
class WADLCheckerPreProcSpec extends BaseCheckerSpec {

    register ("chk","http://www.rackspace.com/repose/wadl/checker")

    feature ("The WADLCheckerBuilder should enable wellform checks only when approprite when prepoc extension is enabled") {
      val testWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
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
           <resource path="/any">
              <method name="POST">
                 <request>
                    <representation mediaType="*/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/text">
              <method name="POST">
                 <request>
                    <representation mediaType="text/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/v">
              <method name="POST">
                 <request>
                    <representation mediaType="text/plain;charset=UTF8"/>
                 </request>
              </method>
           </resource>
        </resources>
      </application>

      val testPreProcWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml">
                        <rax:preprocess>
                            <xsl:stylesheet version="1.0">
                              <xsl:output method="xml" encoding="UTF-8"/>
                            </xsl:stylesheet>
                        </rax:preprocess>
                      </representation>
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
           <resource path="/any">
              <method name="POST">
                 <request>
                    <representation mediaType="*/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/text">
              <method name="POST">
                 <request>
                    <representation mediaType="text/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/v">
              <method name="POST">
                 <request>
                    <representation mediaType="text/plain;charset=UTF8"/>
                 </request>
              </method>
           </resource>
        </resources>
      </application>

      scenario("The testWADL is processed with preporc extension disabled") {
        Given("The testWADL with preproc extension disabled")
        When("The WADL is transalted")
        val checker = builder.build(testWADL, TestConfig(false, false, false, false, false, 1, false, false, false))
        Then("There should not be any well-form checks")
        assert(checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 0")
      }

      scenario("The testWADL is processed with preporc extension enabled") {
        Given("The testWADL with preproc extension disabled")
        When("The WADL is transalted")
        val checker = builder.build(testWADL, TestConfig(false, false, false, false, false, 1, false, false, true))
        Then("There should not be any well-form checks")
        assert(checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 0")
      }

      scenario("The testPreProcWADL is processed with preporc extension enabled") {
        Given("The testPreProcWADL with preproc extension disabled")
        When("The WADL is transalted")
        val checker = builder.build(testPreProcWADL, TestConfig(false, false, false, false, false, 1, false, false, true))
        Then("There should be a single WELL_XML step on the POST operation")
        assert(checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
        assert(checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
        assert(checker, Start, URL("a"), URL("b"), Method("POST"),
               ReqType("(application/xml)(;.*)?"), WellXML, XSL, Accept)
      }
    }
}
