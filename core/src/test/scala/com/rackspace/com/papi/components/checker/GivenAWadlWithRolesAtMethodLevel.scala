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
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevel extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl With Roles At Method Level"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:rax="http://docs.rackspace.com/api"
                 xmlns:xs="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="POST" rax:roles="a:admin">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="GET" rax:roles="a:observer">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="PUT">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="DELETE" rax:roles="a:observer a:admin">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
        <resource path="/c">
          <param name="X-Auth-Token" style="header" required="true"/>
          <param name="X-INT" style="header" type="xs:int" required="true"/>
          <method name="GET" rax:roles="a:admin">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="POST" rax:roles="a:observer a:admin">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="PUT" rax:roles="#all">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="DELETE" rax:roles="a:admin">
            <request>
              <param name="some-generic-header" style="header" required="true"/>
            </request>
          </method>
        </resource>
        <resource path="/d">
          <method name="GET" rax:roles="a:admin">
            <request>
              <param name="X-Auth-Token" style="header" required="true"/>
              <param name="X-INT" style="header" type="xs:int" required="true"/>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="POST" rax:roles="#all">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>)
    , configWithRolesEnabled)

  // GET on /a requires a:observer role
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:bar"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:bar", "a:admin"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:admin"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "GET", "/a", description)

  // POST on /a requires a:admin role
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:bar", "a:admin"), description)
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:bar"), description)
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:bar", "a:observer"), description)
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:observer"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a", description)

  // PUT has no rax:roles defined, should allow all access
  it should behave like accessIsAllowed(validator, "PUT", "/a", List(), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar", "a:jawsome"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a", description)

  // DELETE has a:observer and a:admin, treated as ORs, not ANDs
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:admin", "a:bar"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:bar", "a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:admin"), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List(), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar"), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar", "a:jawsome"), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("observer", "creator"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a", description)

  //GET on /c requires a header and a:admin role
  it should behave like accessIsAllowedWithHeader(validator, "PUT", "/c", List("a:admin"), description)
  it should behave like accessIsAllowedWithHeader(validator, "PUT", "/c", List("a:observer"), description)
  it should behave like accessIsAllowedWithHeader(validator, "PUT", "/c", List("a:bar"), description)
  it should behave like accessIsAllowedWithHeader(validator, "PUT", "/c", List("a:admin", "a:bar"), description)
  it should behave like accessIsAllowedWithHeader(validator, "GET", "/c", List("a:admin"), description)
  it should behave like accessIsAllowedWithHeader(validator, "POST", "/c", List("a:admin"), description)
  it should behave like accessIsAllowedWithHeader(validator, "GET", "/c", List("a:admin", "a:bar"), description)
  it should behave like accessIsAllowedWithHeader(validator, "DELETE", "/c", List("a:admin", "a:bar"), description)
  it should behave like accessIsForbiddenWithHeader(validator, "GET", "/c", List("a:bar"), description)
  it should behave like accessIsForbiddenWithHeader(validator, "GET", "/c", List("a:bar", "a:observer"))
  it should behave like accessIsForbiddenWithHeader(validator, "GET", "/c", List("a:observer"), description)
  it should behave like badRequestWhenHeaderIsMissing(validator, "GET", "/c", List("a:observer"), description)
  it should behave like badRequestWhenOneHeaderIsMissing(validator, "DELETE", "/c", List("a:admin"), description)
  it should behave like badRequestWhenHeaderIsMissing(validator, "DELETE", "/c", List("a:bar"), description)

  //GET on /d requires a header and admin but POST does not
  it should behave like accessIsAllowedWithHeader(validator, "GET", "/d", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "POST", "/d", List("a:bar"), description)
  it should behave like badRequestWhenHeaderIsMissing(validator, "GET", "/d", List("a:admin"), description)
  it should behave like accessIsForbiddenWithHeader(validator, "GET", "/d", List("a:bar"), description)
}
