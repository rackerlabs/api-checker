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
import com.rackspace.com.papi.components.checker.wadl.WADLException
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BadWADLValidatorSuite extends BaseValidatorSuite {
  //
  //  We are simply testing that WADLTools is doing it's job catching
  //  errors, and the tha WADL Processing Error propigates correctly
  //  when cerating the validator.
  //
  //  The error cases on WADL flaws are well coverd by WADLTools
  //  tests.
  //
  test("Missing internal reference should produce WADL exception") {
    val thrown = intercept[WADLException] {
      val validator_EMPTY = Validator(
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="foo">
                   <method href="#fooMethod"/>
              </resource>
           </resources>
        </application>, assertConfig)
    }
    assert(thrown.getMessage.contains("fooMethod"))
    assert(thrown.getMessage.contains("does not seem to exist"))
  }
}
