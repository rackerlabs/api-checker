/***
 *   Copyright 2017 Rackspace US, Inc.
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
class GivenAWadlWithSparseNestedResourcesMasked extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesMaskedEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesMaskedEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesMaskedEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesMaskedEnabledHeaderCheckDisabled,
    "Config With Roles Enabled and Defaults Enabled" -> configWithRolesMaskedEnabledDefaultsEnabled,
    "Config With Roles Enabled and Dubplications Removed and Defaults Enabled" -> configWithRolesMaskedEnabledDupsRemovedDefaultsEnabled)

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
          <resource path="/a">
            <method name="PUT"/>
            <resource path="/z">
              <method name="PUT"/>
            </resource>
            <resource path="/b" rax:roles="b:creator">
              <method name="PUT"/>
              <resource path="c" rax:roles="c:creator">
                <method name="POST"/>
              </resource>
            </resource>
          </resource>
        </resources>
      </application>)
      , configuration)

    // PUT /a has resource level a:admin, method level a:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("another:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("b:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("b:creator"), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a", description)

    // PUT /a/z has resource level a:admin, method level a:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("another:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("a:observer", "a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("b:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/z", List("b:creator"), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a/z", description)

    //  Should be not found on /a/b/z
    it should behave like resourceNotFound(validator, "PUT", "/a/b/z", List("b:creator"), description, List("{z}","'c'"))

    //  Should be not found on /a/b/c/z
    it should behave like resourceNotFound(validator, "PUT", "/a/b/c/z", List("b:creator"), description, List("{z}"))


    // DELETE /a has resource level a:admin, method is not defined
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"), description)
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List(), description)

    // POST /a/b has parent resource level a:admin, resource level b:creator
    it should behave like resourceNotFound(validator, "POST", "/a/b", List("a:admin"), description, List("{b}"))

    // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("a:admin"), description, List("{b}"))
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("another:admin"), description, List("{b}"))
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("b:observer", "a:foo"), description, List("{b}"))
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("a:creator"), description, List("{b}"))
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List(), description, List("{b}"))
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("observer"), description, List("{b}"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a/b", description, List("{b}"))


    // POST /a/b/c has parent resource level a:admin, another:admin, b:creator, c:creator
    it should behave like resourceNotFound(validator, "POST", "/a/b/c", List("a:admin"), description, List("{b}"))
    it should behave like resourceNotFound(validator, "POST", "/a/b/c", List("another:admin"), description, List("{b}"))
    it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("c:creator"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a/b", List("c:creator"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b/c", List("a:admin"), description, List("{b}"))
    it should behave like resourceNotFound(validator, "PUT", "/a/b/c", List("a:admin"), description, List("{b}"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "POST", "/a/b/c", description, List("{b}"))
  }
}
