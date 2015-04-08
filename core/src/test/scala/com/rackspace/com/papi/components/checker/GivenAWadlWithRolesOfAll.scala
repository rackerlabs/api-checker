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

import org.scalatest.FlatSpec
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.FilterChain
import java.io.File
import org.mockito.Mockito._

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesOfAll extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl With Roles of All"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="a:admin">
          <method name="POST" rax:roles="a:creator"/>
          <method name="GET" rax:roles="#all"/>
          <method name="PUT"/>
        </resource>
        <resource path="/b" rax:roles="#all">
          <method name="GET"/>
          <resource path="/c" rax:roles="c:admin">
            <method name="POST"/>
            <method name="GET" rax:roles="c:observer"/>
          </resource>
        </resource>
      </resources>
    </application>)
    , configWithRolesEnabled)

  // GET on /a has resource level a:admin, method level #all
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:bar"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:bar", "a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List(), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/a", description)

  // PUT on /a has resource level a:admin, no method level
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar", "a:admin"), description)
  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:observer", "a:bar"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a", description)

  // GET on /b has resource level #all, no method level
  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List(), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List("bar"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b", description)

  // POST on /b has resource level #all, method is not allowed
  it should behave like methodNotAllowed(validator, "POST", "/b", List("a:admin"), description)
  it should behave like methodNotAllowed(validator, "POST", "/b", List(), description)

  // POST on /b/c has parent resource level #all, resource level c:admin, no method level
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List("c:admin"), description)
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List(), description)
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List("bar"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "POST", "/b/c", description)

  // GET on /b/c has perent resource #all and c:admin and resource c:observer
  it should behave like accessIsAllowed(validator, "GET", "/b/c", List("c:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b/c", List("c:observer"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b/c", List(), description)
  it should behave like accessIsAllowed(validator, "GET", "/b/c", List("bar"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b/c", description)
}
