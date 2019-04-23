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
import org.scalatest.FlatSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GivenNoRolesInWadl extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
                                    "Config With Roles Masked Enabled" -> configWithRolesMaskedEnabled)

  for ((description, configuration) <- configs) {

    val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="GET"/>
          <method name="PUT"/>
        </resource>
      </resources>
    </application>)
    , configuration)

    // GET on /a has no roles
    it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a", List(), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/a", description)

    // PUT on /a has no roles
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List(), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a", description)

    // POST on /a has no roles, method is not allowed
    it should behave like methodNotAllowed(validator, "POST", "/a", List("a:admin"), description, List("does not match","'GET|PUT'"))
    it should behave like methodNotAllowed(validator, "POST", "/a", List(), description, List("does not match","'GET|PUT'"))
  }
}
