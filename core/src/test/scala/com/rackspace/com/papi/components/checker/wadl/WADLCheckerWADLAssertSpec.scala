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
class WADLCheckerWADLAssertSpec extends BaseCheckerSpec with LogAssertions {
  feature ("The WADLCheckerBuilder can correctly identify WADLs that violate checker WADL assertions") {
    info ("As a developer")
    info ("I would like to catch errors not normally caught by WADL tools in WADL that are specific to the")
    info ("api-checker, so that I can more easily debug a WADL")

    scenario("A WADL with a no-name method"){
      Given("a WADL with a no-name method")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/b">
                   <method id="nameless">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
      </application>
      When ("The WADL is translated")
      val checkerLog = log (Level.ERROR) {
        intercept[WADLException] {
          val checker = builder.build(inWADL, new Config())
          println(checker) // Should never print!
        }
      }
      Then ("There should be an error detailing that the method requires a name")
      List("method","nameless","requires a name").foreach(m => {
        assert(checkerLog, m)
      })
    }
  }
}
