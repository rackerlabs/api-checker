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
package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLSuite2 extends BaseValidatorSuite {
  //
  // validator_SLASH test scenarios where slash '/' is used exclusively in
  // a resource path.
  //

  val validator_SLASH = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
    <grammars/>
    <resources base="https://test.api.openstack.com">
      <resource path="/">
        <method name="GET">
          <response status="200"/>
        </method>
        <resource path="element">
            <resource path="/">
                <resource path="/">
                    <method name="GET">
                          <response status="200"/>
                    </method>
                    <resource path="element2">
                        <method name="POST">
                            <response status="200"/>
                         </method>
                    </resource>
                  </resource>
                </resource>
          </resource>
        </resource>
       </resources>
      </application>
    , assertConfig)

  test ("GET on / should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET","/"),response,chain)
  }

  test ("GET on '' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET",""),response,chain)
  }

  test ("GET on '/element' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET","/element"),response,chain)
  }

  test ("POST on '/element/element2' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("POST","/element/element2"),response,chain)
  }

  test ("POST on / should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("POST","/"),response,chain), 405, Map("Allow"->"GET"))
  }

  test ("POST on /element should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("POST","/element"),response,chain), 405, Map("Allow"->"GET"))
  }

  test ("GET on /element/element2 should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/element2"),response,chain), 405, Map("Allow"->"POST"))
  }

  test ("GET on /foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/foo"),response,chain), 404)
  }

  test ("GET on /element/foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/foo"),response,chain), 404)
  }

  test ("GET on /element/element2/foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/element2/foo"),response,chain), 404)
  }

}
