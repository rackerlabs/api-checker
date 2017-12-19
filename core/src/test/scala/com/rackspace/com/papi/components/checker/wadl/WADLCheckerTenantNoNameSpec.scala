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

import com.rackspace.com.papi.components.checker.{LogAssertions, Config}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._


@RunWith(classOf[JUnitRunner])
class WADLCheckerTenantNoNameSpec extends BaseCheckerSpec with LogAssertions {

  case class TenantNoNameTest (desc : String, conf : Config, wadl : NodeSeq, messages : List[String])

  //
  // Custom configs
  //
  val plainConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c.checkPlainParams = true
    c.removeDups = false
    c
  }

  val headerConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c.checkHeaders = true
    c.removeDups = false
    c
  }

  val headerNoDupsConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c.checkHeaders = true
    c.removeDups = true
    c
  }

  val noNameTestCases : Array[TenantNoNameTest] =  Array(
    new TenantNoNameTest("Template pramater of type string missing name and isTenant=true",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param style="template" type="xsd:string" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, List("param of name 'id' is not found")),
    new TenantNoNameTest("Template pramater of type int missing name and isTenant=true",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param style="template" type="xsd:int" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>, List("param of name 'id' is not found")),
    new TenantNoNameTest("XPath with missing name and isTenant=true",
      plainConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                      <param style="plain" path="/tst:a/@id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" />
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("The element requires a name", "/tst:a/@id")),
    new TenantNoNameTest("JSON XPath with missing name and isTenant=true",
      plainConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                    <representation mediaType="application/json">
                      <param style="plain" path="$_?tst?id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="$_?tst?stepType" required="true" />
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("The element requires a name", "$_?tst?id")),
    new TenantNoNameTest("Header with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param style="header" required="true" repeating="true" rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header of type xsd:int with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param style="header" required="true" repeating="true" type="xsd:int" rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true" type="xsd:int" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header (single) with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param style="header" required="true"  rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header (single) of type xsd:int with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param style="header" required="true" type="xsd:int" rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true" type="xsd:int"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header (any) with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="2388"/>
                 <param style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header (xsd:int, any) with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="2388"/>
                 <param style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Header (all) with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                 <param style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("Headers always require a name", "header")),
    new TenantNoNameTest("Capture Header with missing name and isTenant=true",
      headerConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <rax:captureHeader path="$req:uri" isTenant="true"/>
                 <rax:captureHeader name="stepType" path="'CAPTURE_HEADER'" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, List("The element requires a name", "$req:uri"))
  )

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst", "http://www.rackspace.com/repose/wadl/checker/step/test")

  feature ("The WADLCheckerBuilder can correctly identifies a WADL where a parameter name is misseng") {
    info ("As a developer")
    info ("I want to be able to catch errors with rax:isTenants when a param name is missing ")
    info ("so that I can more easily debug a WADL")


    //
    //  In these cases we should make sure that the name is correctly
    //  set or an error is generated.
    //
    for (nnt <- noNameTestCases) {
      val desc = nnt.desc
      scenario(desc) {
        Given(s"a WADL where $desc")
        val inWADL = nnt.wadl
        When("the wadl is translated")
        val checkerLog = log (Level.ERROR) {
          intercept[WADLException] {
            val checker = builder.build(inWADL, nnt.conf)
            println(checker) // Should never print!
          }
        }
        Then ("There should be an error detailing that the checker requires a name")
        nnt.messages.foreach (assert(checkerLog, _))
      }
    }
  }
}
