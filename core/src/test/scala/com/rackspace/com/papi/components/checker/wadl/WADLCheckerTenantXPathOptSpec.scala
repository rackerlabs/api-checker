/***
 *   Copyright 2018 Rackspace US, Inc.
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

import com.rackspace.com.papi.components.checker.Config
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.xml._


@RunWith(classOf[JUnitRunner])
class WADLCheckerTenantXPathOptSpec extends BaseCheckerSpec {

  //
  // Custom configs
  //
  val plainConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c.checkPlainParams = true
    c.removeDups = false
    c.joinXPathChecks = false
    c
  }

  val plainDupsConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c.checkPlainParams = true
    c.removeDups = true
    c.joinXPathChecks = true
    c
  }

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("xsl", "http://www.w3.org/1999/XSL/Transform")

  feature ("The WADLCheckerBuilder can correctly handle rax:isTenant extension with XPath join optimization") {
    info ("As a developer")
    info ("When translating a WADL to checker format with rax:isTenant, I wast XPath join optimization to work correctly")
    info ("so that I can take advantage of the optimization when it's appropriate.")


    scenario ("A WADL with two XPaths no rax:isTenant set to false and joinXPathChecks disabled") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainConfig)
      Then("There should be the right number of XPath steps")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='id']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='stepType']) = 1")
    }

    scenario ("A WADL with two XPaths no rax:isTenant set to false and joinXPathChecks enabled") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainDupsConfig)
      Then("There should be no XPath steps and an XSL should be in its place")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@id']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@stepType']) = 1")
    }

    scenario ("A WADL with two XPaths no rax:isTenant set to false and joinXPathChecks enabled (explicit false)") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="false"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" rax:isTenant="false"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainDupsConfig)
      Then("There should be no XPath steps and an XSL should be in its place")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@id']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@stepType']) = 1")
    }

    scenario ("A WADL with two XPaths  rax:isTenant set to true to both of them and joinXPathChecks enabled") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" rax:isTenant="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainDupsConfig)
      Then("There should be no XSL steps and an XPATH steps should be in its place")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='id']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='stepType']) = 1")
    }

    scenario ("A WADL with two XPaths rax:isTenant set to true on the first one and joinXPathChecks enabled") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" rax:isTenant="false"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainDupsConfig)
      Then("There should be no XSL steps and an XPATH steps should be in its place")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='id']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='stepType']) = 1")
    }

    scenario ("A WADL with two XPaths rax:isTenant set to true on the second one and joinXPathChecks enabled") {
      Given("A WADL with two XPath, no rax:isTenant")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
           >
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
                <request>
                    <representation mediaType="application/xml">
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="false"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" rax:isTenant="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The wadl is translated")
      val checker = builder.build (inWADL, plainDupsConfig)
      Then("There should be no XSL steps and an XPATH steps should be in its place")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XSL']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='id']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @name='stepType']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@id']) = 1")
      assert (checker, "count(//xsl:when[@test='/tst:a/@stepType']) = 0")
    }

  }
}
