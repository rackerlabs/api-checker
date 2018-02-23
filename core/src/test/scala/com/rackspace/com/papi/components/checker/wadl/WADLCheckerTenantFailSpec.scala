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

@RunWith(classOf[JUnitRunner])
class WADLCheckerTenantFailSpec extends BaseCheckerSpec with LogAssertions {
  //
  // Namespaces
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  //
  //  Custom Configs
  //
  val maskConfig = {
    val c = new Config()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.checkPlainParams = true
    c
  }

  val raxRolesConfig = {
    val c = new Config()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = false
    c.checkPlainParams = true
    c
  }

  feature("The WADLCheckerBuilder identitifes errors related to tenanted roles and fails") {
    info("As a delveloper")
    info("I want to catch errors with tenanted roles early in the loading of a WADL")
    info("so that I can more easly debug a WADL, and anomolies to appear in production")

    //
    //  The two scenarios below are a stop gap, while we implement
    //  support for mask rax:roles.
    //
    scenario ("Given a transform with Mask Rax-Roles enabled") {
      Given("a WADL which leverages rax:roles")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="admin/{id}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, maskConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog,"not implemented")
    }

    scenario ("Given a transform with Mask Rax-Roles enabled (no tenant match)") {
      Given("a WADL which leverages rax:roles mask when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="admin/{foo}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, maskConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog,"not implemented")
    }

    scenario ("Given a transform with Rax-Roles on a teant but missing the tenant parameter") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="admin/{foo}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'foo'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (uri)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }


    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (xpath)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/xml">
                    <param name="id" style="plain" path="//@id" required="true"/>
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (xpath-json)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                    <param name="id" style="plain" path="$body(@id)" required="true"/>
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="true" required="true"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header any)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="true" required="true" rax:anyMatch="true" fixed="foo"/>
                   <param name="id" style="header" repeating="true" required="true" rax:anyMatch="true" fixed="bar"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }


    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header any, first match)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="true" required="true" rax:anyMatch="true" fixed="foo"/>
                   <param name="ID" style="header" repeating="true" required="true" rax:anyMatch="true" fixed="bar"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }


    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header all)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="true" required="true" fixed="foo"/>
                   <param name="id" style="header" repeating="true" required="true" fixed="bar"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header all, first match)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="true" required="true" fixed="foo"/>
                   <param name="ID" style="header" repeating="true" required="true" fixed="bar"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }


    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (header single)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="id" style="header" repeating="false" required="true"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                 <representation mediaType="application/json">
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }

    scenario ("Given a transform with Rax-Roles on a teant but mismatch in case of tenant parameter (capture header)") {
      Given("a WADL which leverages rax:roles when there is no tenant match")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{stepType}" rax:roles="admin/{ID}">
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#postMethod" />
              </resource>
           </resources>
           <method id="postMethod" name="POST">
               <request>
                <representation mediaType="application/json">
                  <rax:captureHeader name="id" path="//@id"/>
                 </representation>
               </request>
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checkerLog = log(Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, raxRolesConfig)
          println (checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that rax:roles mask is currently not supported")
      assert(checkerLog, "no defined param named 'ID'")
    }


    scenario ("Given a transform with Rax-Roles on a teant with a role name that contains a \\") {
      Given("a WADL which leverages rax:roles tenanted with a role name that contains a \\")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="ad\m\in/{header}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
              <rax:captureHeader path="55" name="header"/>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checker = builder.build(inWADL, raxRolesConfig)
      Then ("The backslash should be properly encoded")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @matchingRoles='ad\\m\\in/{header}'])")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @name='X-RELEVANT-ROLES' and contains(@path, 'ad\\m\\in/{header}')])")
    }

    scenario ("Given a transform with Rax-Roles on a teant with a role name that contains a \\\\") {
      Given("a WADL which leverages rax:roles tenanted with a role name that contains a \\\\")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="ad\\m\\in/{header}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
              <rax:captureHeader path="55" name="header"/>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checker = builder.build(inWADL, raxRolesConfig)
      Then ("The backslash should be properly encoded")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @matchingRoles='ad\\\\m\\\\in/{header}'])")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @name='X-RELEVANT-ROLES' and contains(@path, 'ad\\\\m\\\\in/{header}')])")
    }


    scenario ("Given a transform with Rax-Roles on a teant with a role name that contains a space") {
      Given("a WADL which leverages rax:roles tenanted with a role name that contains a space")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource/{stepType}" rax:roles="ad&#xA0;m&#xA0;in/{header}">
                   <param name="id" style="template" type="xsd:string"/>
                   <param name="stepType" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
              <rax:captureHeader path="55" name="header"/>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
      </application>
      When("The WADL is translated")
      val checker = builder.build(inWADL, raxRolesConfig)
      Then ("The backslash should be properly encoded")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @matchingRoles='ad m in/{header}'])")
      assert(checker,"exists(/chk:checker/chk:step[@type='CAPTURE_HEADER' and @name='X-RELEVANT-ROLES' and contains(@path, 'ad m in/{header}')])")
    }

  }

}
