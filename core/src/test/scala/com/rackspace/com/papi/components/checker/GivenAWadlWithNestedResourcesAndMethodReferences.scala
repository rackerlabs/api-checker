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
class GivenAWadlWithNestedResourcesAndMethodReferences extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesEnabledHeaderCheckDisabled,
    "Config with Roles Enabled and Default Parameters Enabled" -> configWithRaxRolesEnabledDefaultsEnabled,
    "Config with Roles Enabled, Default Parameters Enabled and Duplications Removed" -> configWithRaxRolesEnabledDupsRemovedDefaultsEnabled)

  for ((description, configuration) <- configs) {

    val validator = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method href="#putOnA" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method href="#postOnB"/>
              <method href="#putOnB"/>
              <method href="#deleteOnB" rax:roles="b:observer b:admin"/>
            </resource>
            <resource path="{yn}" rax:roles="a:admin">
              <param name="yn" style="template" type="tst:yesno"/>
              <method href="#yn"/>
            </resource>
          </resource>
        </resources>
        <method id="putOnA" name="PUT"/>
        <method id="postOnB" name="POST"/>
        <method id="putOnB" name="PUT" rax:roles="b:observer"/>
        <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
        <method id="yn" name="GET" />
      </application>)
      , configuration)

    // PUT /a has resource level a:admin, method level a:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)
    it should behave like accessIsForbidden(validator, "PUT", "/a", List("b:observer"), description)
    it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a", description)

    // DELETE /a has resource level a:admin, method is not defined
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"), description)
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List(), description)

    // POST /a/b has parent resource level a:admin, resource level b:creator
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"), description)
    it should behave like accessIsForbidden(validator, "POST", "/a/b", List("a:observer"), description)
    it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a/b", description)

    // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:observer", "a:foo"), description)
    it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("a:creator"), description)
    it should behave like accessIsForbidden(validator, "PUT", "/a/b", List(), description)
    it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("observer"), description)
    it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a/b", description)

    // DELETE /a/b has parent resource level a:admin, resource level b:creator, method level b:admin, b:observer
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"), description)
    it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List(), description)
    it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("a:observer"), description)
    it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("b:foo"), description)
    it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a/b", description)

    // GET on /a/yes, /a/no, /a/foo
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin"), description, List("'b'","yes","no"))
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin", "a:observer"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin", "a:observer"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin", "a:observer"), description, List("'b'","yes","no"))
    it should behave like accessIsForbidden(validator, "GET", "/a/yes", List("a:observer"), description)
    it should behave like accessIsForbidden(validator, "GET", "/a/no", List("a:observer"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer"), description, List("'b'","yes","no"))
  }
}
