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
import org.scalatest.junit.JUnitRunner

import scala.xml._

object WADLCheckerTenantSpec {
  //
  //  We create test cases for testing tenants for each step type.
  //
  case class TenantTest (desc : String, conf : Config, wadl : NodeSeq, stepType : String, isTenant : Boolean, stepCount : Int = 1)

  val stdConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = true
    c
  }

  val stdNTEConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = false
    c
  }


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

  val plainNTEConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = false
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

  val headerNTEConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = false
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

  val headerNTENoDupsConfig = {
    val c = new Config
    c.enableRaxIsTenantExtension = false
    c.checkHeaders = true
    c.removeDups = true
    c
  }

  def disabledTestCases : List[TenantTest] = List(
    new TenantTest("The WADL contains a template parameter of type string with rax:isTenant (rax:isTenant extn disabled)",
      stdNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:string" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URL", false),
    new TenantTest("The WADL contains a template parameter of type int with rax:isTenant (rax:isTenant extn disabled)",
      stdNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:int" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URLXSD", false),
    new TenantTest("The WADL contains an XPath plain parameter of type string with rax:isTenant (rax:isTenant extn disabled)",
      plainNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "XPATH", false),
    new TenantTest("The WADL contains a JSON XPath plain parameter of type string with rax:isTenant (rax:isTenant extn disabled)",
      plainNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                      <param name="id" style="plain" path="$_?tst?id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="$_?tst?stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "JSON_XPATH", false),
    new TenantTest("The WADL contains a Header parameter of type string with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param name="id" style="header" required="true" repeating="true" rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER", false),
    new TenantTest("The WADL contains a Header parameter of type int with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                  <param name="stepType" style="header" type="xsd:int" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD", false),
    new TenantTest("The WADL contains a Header (single) parameter of type string with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param name="id" style="header" required="true" rax:isTenant="true" />
                  <param name="stepType" style="header" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_SINGLE", false),
    new TenantTest("The WADL contains a Header (single) parameter of type int with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                  <param name="id" style="header" type="xsd:int" required="true" rax:isTenant="true" />
                  <param name="stepType" style="header" type="xsd:int" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_SINGLE", false),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (remove dups) (rax:isTenant extn disabled)",
      headerNTENoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", false),
    new TenantTest("The WADL contains a Header (any) parameter of type int with rax:isTenant (mixed, 2) (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", false, 2),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", false),
    new TenantTest("The WADL contains a Capture header  with rax:isTenant (rax:isTenant extn disabled)",
      headerNTEConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <rax:captureHeader name="id"       path="$req:uri" isTenant="true"/>
                 <rax:captureHeader name="stepType" path="'CAPTURE_HEADER'" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "CAPTURE_HEADER", false)
  )

  def testCases : List[TenantTest] = List(
    new TenantTest("The WADL contains a template parameter of type string without rax:isTenant",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URL", false),
    new TenantTest("The WADL contains a template parameter of type string without rax:isTenant (explicit false)",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:string" rax:isTenant="false"/>
                   <param name="stepType" style="template" type="xsd:string" rax:isTenant="false"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URL", false),
    new TenantTest("The WADL contains a template parameter of type string with rax:isTenant",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:string" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URL", true),
    new TenantTest("The WADL contains a template parameter of type int without rax:isTenant",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:int"/>
                   <param name="stepType" style="template" type="xsd:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URLXSD", false),
    new TenantTest("The WADL contains a template parameter of type int without rax:isTenant (explicit false)",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:int" rax:isTenant="false"/>
                   <param name="stepType" style="template" type="xsd:int" rax:isTenant="false"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URLXSD", false),
    new TenantTest("The WADL contains a template parameter of type int with rax:isTenant",
      stdConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}">
                   <param name="id" style="template" type="xsd:int" rax:isTenant="true"/>
                   <param name="stepType" style="template" type="xsd:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>, "URLXSD", true),
    new TenantTest("The WADL contains an XPath plain parameter of type string without rax:isTenant",
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
                      <param name="id" style="plain" path="/tst:a/@id" required="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "XPATH", false),
    new TenantTest("The WADL contains an XPath plain parameter of type string without rax:isTenant (explicit false)",
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
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="false"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true" rax:isTenant="false"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "XPATH", false),
    new TenantTest("The WADL contains an XPath plain parameter of type string with rax:isTenant",
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
                      <param name="id" style="plain" path="/tst:a/@id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "XPATH", true),
    new TenantTest("The WADL contains a JSON XPath plain parameter of type string without rax:isTenant",
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
                      <param name="id" style="plain" path="$_?tst?id" required="true"/>
                      <param name="stepType" style="plain" path="$_?tst?stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "JSON_XPATH", false),
    new TenantTest("The WADL contains a JSON XPath plain parameter of type string without rax:isTenant (explicit false)",
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
                      <param name="id" style="plain" path="$_?tst?id" required="true" rax:isTenant="false"/>
                      <param name="stepType" style="plain" path="$_?tst?stepType" required="true" rax:isTenant="false"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "JSON_XPATH", false),
    new TenantTest("The WADL contains a JSON XPath plain parameter of type string with rax:isTenant",
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
                      <param name="id" style="plain" path="$_?tst?id" required="true" rax:isTenant="true"/>
                      <param name="stepType" style="plain" path="$_?tst?stepType" required="true"/>
                   </representation>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "JSON_XPATH", true),
    new TenantTest("The WADL contains a Header parameter of type string without rax:isTenant",
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
                  <param name="id" style="header" required="true" repeating="true"/>
                  <param name="stepType" style="header" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER", false),
    new TenantTest("The WADL contains a Header parameter of type string without rax:isTenant (explicit false)",
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
                  <param name="id" style="header" required="true" repeating="true" rax:isTenant="false"/>
                  <param name="stepType" style="header" required="true" repeating="true" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER", false),
    new TenantTest("The WADL contains a Header parameter of type string with rax:isTenant",
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
                  <param name="id" style="header" required="true" repeating="true" rax:isTenant="true"/>
                  <param name="stepType" style="header" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER", true),
    new TenantTest("The WADL contains a Header parameter of type int without rax:isTenant",
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
                  <param name="id" style="header" type="xsd:int" required="true" repeating="true"/>
                  <param name="stepType" style="header" type="xsd:int" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD", false),
    new TenantTest("The WADL contains a Header parameter of type int without rax:isTenant (explicit false)",
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
                  <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="false"/>
                  <param name="stepType" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD", false),
    new TenantTest("The WADL contains a Header parameter of type int with rax:isTenant",
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
                  <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                  <param name="stepType" style="header" type="xsd:int" required="true" repeating="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD", true),
    new TenantTest("The WADL contains a Header (single) parameter of type string without rax:isTenant",
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
                  <param name="id" style="header" required="true" />
                  <param name="stepType" style="header" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_SINGLE", false),
    new TenantTest("The WADL contains a Header (single) parameter of type string without rax:isTenant (explicit false)",
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
                  <param name="id" style="header" required="true" rax:isTenant="false"/>
                  <param name="stepType" style="header" required="true" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_SINGLE", false),
    new TenantTest("The WADL contains a Header (single) parameter of type string with rax:isTenant",
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
                  <param name="id" style="header" required="true" rax:isTenant="true" />
                  <param name="stepType" style="header" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_SINGLE", true),
    new TenantTest("The WADL contains a Header (single) parameter of type int without rax:isTenant",
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
                  <param name="id" style="header" type="xsd:int" required="true" />
                  <param name="stepType" style="header" type="xsd:int" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_SINGLE", false),
    new TenantTest("The WADL contains a Header (single) parameter of type int without rax:isTenant (explicit false)",
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
                  <param name="id" style="header" type="xsd:int" required="true" rax:isTenant="false"/>
                  <param name="stepType" style="header" type="xsd:int" required="true" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_SINGLE", false),
    new TenantTest("The WADL contains a Header (single) parameter of type int with rax:isTenant",
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
                  <param name="id" style="header" type="xsd:int" required="true" rax:isTenant="true" />
                  <param name="stepType" style="header" type="xsd:int" required="true" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_SINGLE", true),
    new TenantTest("The WADL contains a Header (any) parameter of type string without rax:isTenant",
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", false, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type string without rax:isTenant (remove dups)",
      headerNoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", false),
    new TenantTest("The WADL contains a Header (any) parameter of type string without rax:isTenant (explicit false)",
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", false, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type string without rax:isTenant (remove dups, explicit false)",
      headerNoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", false),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant",
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (mixed)",
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (mixed 2)",
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (remove dups)",
      headerNoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (remove dups, mixed)",
      headerNoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true),
    new TenantTest("The WADL contains a Header (any) parameter of type string with rax:isTenant (remove dups, mixed 2)",
      headerNoDupsConfig, <application xmlns="http://wadl.dev.java.net/2009/02"
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
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false" fixed="2388"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true" fixed="4666"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" rax:anyMatch="true" fixed="bar"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ANY", true),
    new TenantTest("The WADL contains a Header (any) parameter of type int without rax:isTenant",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", false, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type int without rax:isTenant (explicit false)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", false, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type int with rax:isTenant",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", true, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type int with rax:isTenant (mixed)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", true, 2),
    new TenantTest("The WADL contains a Header (any) parameter of type int with rax:isTenant (mixed, 2)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="false"/>
                 <param name="id" style="header" type="xsd:float" required="true" repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:anyMatch="true"/>
                 <param name="stepType" style="header" type="xsd:float" required="true"  repeating="true" rax:anyMatch="true"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADERXSD_ANY", true, 2),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int without rax:isTenant",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" />
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", false),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int without rax:isTenant (explicit false)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="false"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo" rax:isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", false),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int with rax:isTenant",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", true),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int with rax:isTenant (mixed)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="true"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="false"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", true),
    new TenantTest("The WADL contains a Header (all) parameter of types string, int with rax:isTenant (mixed, 2)",
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
                 <param name="id" style="header" type="xsd:int" required="true" repeating="true" rax:isTenant="false"/>
                 <param name="id" style="header" type="xsd:string" required="true" repeating="true" fixed="foo" rax:isTenant="true"/>
                 <param name="stepType" style="header" type="xsd:int" required="true"  repeating="true"/>
                 <param name="stepType" style="header" type="xsd:string" required="true"  repeating="true" fixed="foo"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "HEADER_ALL", true),
    new TenantTest("The WADL contains a Capture header  without rax:isTenant",
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
                 <rax:captureHeader name="id"       path="$req:uri"/>
                 <rax:captureHeader name="stepType" path="'CAPTURE_HEADER'"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "CAPTURE_HEADER", false),
    new TenantTest("The WADL contains a Capture header  without rax:isTenant (explicit false)",
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
                 <rax:captureHeader name="id"       path="$req:uri" isTenant="false"/>
                 <rax:captureHeader name="stepType" path="'CAPTURE_HEADER'" isTenant="false"/>
               </request>
               <response status="200 203"/>
           </method>
        </application>, "CAPTURE_HEADER", false),
    new TenantTest("The WADL contains a Capture header  with rax:isTenant",
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
                 <rax:captureHeader name="id"       path="$req:uri" isTenant="true"/>
                 <rax:captureHeader name="stepType" path="'CAPTURE_HEADER'" />
               </request>
               <response status="200 203"/>
           </method>
        </application>, "CAPTURE_HEADER", true)
  )
}

import WADLCheckerTenantSpec._

@RunWith(classOf[JUnitRunner])
class WADLCheckerTenantSpec extends BaseCheckerSpec {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst", "http://www.rackspace.com/repose/wadl/checker/step/test")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {
    info ("As a developer")
    info ("I want to be able to transform a WADL which references rax:isTenants to a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")
    info ("and correctly set tenant roles")

    //
    //  Happy path tests, we are simply making sure that for each step
    //  type we correctly set (or not set) the isTenant attribute on
    //  the step.
    //
    //  The scenarios are described in the testCases list above, but
    //  the assertions are fairly static in these cases.
    //

    for (tt <- testCases ++ disabledTestCases) {
      val desc = tt.desc
      scenario(desc) {
        val stepType = tt.stepType
        val stepCount = tt.stepCount
        Given(s"a WADL where $desc")
        val inWADL = tt.wadl
        When ("the wadl is translated")
        val checker = builder.build (inWADL, tt.conf)
        Then (s"There should be steps of $stepType")
        assert(checker, s"count(/chk:checker/chk:step[@type='$stepType' and lower-case(@name)='id']) = $stepCount")
        assert(checker, s"count(/chk:checker/chk:step[@type='$stepType' and lower-case(@name)='steptype']) = $stepCount")
        And (s"The $stepType 'stepType' step should have isTenant set to false (or not set)")
        assert(checker, s"every $$s in /chk:checker/chk:step[@type='$stepType' and lower-case(@name)='steptype'] satisfies not(xsd:boolean($$s/@isTenant))")
        if (!tt.isTenant) {
          And (s"The $stepType 'id' step should have isTenant set to false (or not set)")
          assert(checker, s"every $$s in /chk:checker/chk:step[@type='$stepType' and lower-case(@name)='id'] satisfies not(xsd:boolean($$s/@isTenant))")
        } else {
          And (s"The $stepType 'id' step should have isTenant set to true")
          assert(checker, s"every $$s in /chk:checker/chk:step[@type='$stepType' and lower-case(@name)='id'] satisfies xsd:boolean($$s/@isTenant)")
        }
      }
    }
  }
}
