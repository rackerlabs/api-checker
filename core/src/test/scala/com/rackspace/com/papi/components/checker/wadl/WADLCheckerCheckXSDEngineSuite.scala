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
class WADLCheckerCheckXSDEngineSuite extends BaseCheckerSpec with LogAssertions {
  val config = {
    val c = TestConfig()
    c.validateChecker = true
    c.xsdEngine="Xerces"
    c
  }

  scenario ("Load a WADL document check XSDEngine") {
    Given("a WADL document that ends in")
    val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/">
                <resource path="element">
                    <method name="GET">
                        <response status="200"/>
                    </method>
                </resource>
              </resource>
           </resources>
        </application>
    When("the document is loaded...")
    val goodCheckerLog = log (Level.DEBUG) {
      val checker = builder.build (inWADL,config)
    }
    Then ("An appropriate DEBUG messages should be emmited.")
    assert(goodCheckerLog,"Using Xerces for checker validation")
  }
}
