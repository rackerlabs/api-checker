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
class RaxRolesNotInheritedFromSibling extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl Without Roles"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="a:admin">
          <method name="POST" rax:roles="a:creator"/>
          <method name="GET" rax:roles="#all"/>
          <method name="PUT"/>
        </resource>
        <resource path="/b">
          <method name="GET"/>
          <resource path="/c">
            <method name="POST"/>
            <method name="GET"/>
          </resource>
        </resource>
      </resources>
    </application>)
    , configWithRolesEnabled)

  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a", description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"), description)

  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:noone"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:creator"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "POST", "/b/c", description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b/c", description)
}
