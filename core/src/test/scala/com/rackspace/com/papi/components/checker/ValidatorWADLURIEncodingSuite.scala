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

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ValidatorWADLURIEncodingSuite extends BaseValidatorSuite {
  val validator_URIEncoding = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/+hello">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
              <resource path="/a/ hello">
                   <method name="PUT">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>
    , assertConfig)

  test("GET on /a/+hello should succeed on validator_URIEncoding") {
    validator_URIEncoding.validate(request("GET","/a/+hello"), response, chain)
  }

  test("PUT on /a/ hello should succeed on validator_URIEncoding") {
    validator_URIEncoding.validate(request("PUT","/a/%20hello"), response, chain)
  }

  test("GET on /a/ hello should fail on validator_URIEncoding") {
    assertResultFailed(validator_URIEncoding.validate(request("GET","/a/%20hello"),response,chain), 405)
  }

  test("PUT on /a/+hello should fail on validator_URIEncoding") {
    assertResultFailed(validator_URIEncoding.validate(request("PUT","/a/+hello"),response,chain), 405)
  }
}
