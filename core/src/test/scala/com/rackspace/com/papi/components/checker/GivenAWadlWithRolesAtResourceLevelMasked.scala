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
class GivenAWadlWithRolesAtResourceLevelMasked extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl With Roles At Resource Level"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="a:admin">
          <method name="POST"/>
          <method name="GET"/>
          <method name="PUT" rax:roles="a:observer"/>
          <method name="DELETE" rax:roles="a:observer a:admin a:creator"/>
        </resource>
      </resources>
    </application>)
    , configWithRolesMaskedEnabled)

  // When a single value rax:roles at resource level but not at method level
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"), description)
  it should behave like methodNotAllowed(validator, "GET", "/a", List("a:observer"), description, List("does not match","'DELETE|PUT'"))
  it should behave like methodNotAllowed(validator, "GET", "/a", List("a:creator"), description, List("does not match","'DELETE'"))
  it should behave like resourceNotFound(validator, "GET", "/a", List("b:observer"), description)
  it should behave like resourceNotFound(validator, "GET", "/a", List("b:creator"), description)
  it should behave like resourceNotFoundWhenNoXRoles(validator, "GET", "/a", description)

  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"), description)
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:observer"), description, List("does not match","'DELETE|PUT'"))
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:creator"), description, List("does not match","'DELETE'"))
  it should behave like resourceNotFound(validator, "POST", "/a", List("b:creator"), description)
  it should behave like resourceNotFoundWhenNoXRoles(validator, "POST", "/a", description)

  // PUT has resource level a:admin, method level a:observer
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:creator"), description)
  it should behave like methodNotAllowed(validator, "PUT", "/a", List("a:creator"), description, List("does not match","'DELETE'"))

  it should behave like resourceNotFound(validator, "PUT", "/a", List("a:bar"), description)
  it should behave like resourceNotFound(validator, "PUT", "/a", List(), description)
  it should behave like resourceNotFound(validator, "PUT", "/a", List("a:observe"), description)
  it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a", description)

  // DELETE has resource level a:admin, method level a:observer and a:admin
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:creator"), description)

  it should behave like resourceNotFound(validator, "DELETE", "/a", List("a:bar"), description)
  it should behave like resourceNotFound(validator, "DELETE", "/a", List(), description)
  it should behave like resourceNotFound(validator, "DELETE", "/a", List("a:observe"), description)
  it should behave like resourceNotFoundWhenNoXRoles(validator, "DELETE", "/a", description)
}
