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
class GivenAWadlWithNestedResourcesAndMethodReferencesAndOddRoleNamesMasked extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesMaskedEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesMaskedEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesMaskedEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesMaskedEnabledHeaderCheckDisabled)

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
          <resource path="/a" rax:roles="a:admin-foo">
            <method href="#putOnA" rax:roles="a:observer% a:observer&#xA0;wsp"/>
            <resource path="/b" rax:roles="b:creator">
              <method href="#postOnB"/>
              <method href="#putOnB"/>
              <method href="#deleteOnB" rax:roles="AR-Payments-Billing-Support b:admin"/>
            </resource>
            <resource path="{yn}" rax:roles="a:admin-foo">
              <param name="yn" style="template" type="tst:yesno"/>
              <method href="#yn"/>
            </resource>
          </resource>
        </resources>
        <method id="putOnA" name="PUT"/>
        <method id="postOnB" name="POST"/>
        <method id="putOnB" name="PUT" rax:roles="AR-Payments-Billing-Support"/>
        <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
        <method id="yn" name="GET" />
      </application>)
      , configuration)

    // PUT /a has resource level a:admin-foo, method level a:observer%
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer%"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer%", "a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer wsp"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer wsp", "a:admin-foo"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a", List("AR-Payments-Billing-Support"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a", description)

    // DELETE /a has resource level a:admin-foo, method is not defined
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin-foo"), description, List("does not match","'PUT'"))
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:observer%"), description, List("does not match","'PUT'"))
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:observer wsp"), description, List("does not match","'PUT'"))
    it should behave like resourceNotFound(validator, "DELETE", "/a", List(), description)

    // POST /a/b has parent resource level a:admin-foo, resource level b:creator
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"), description)
    it should behave like resourceNotFound(validator, "POST", "/a/b", List("a:observer%"), description)
    it should behave like resourceNotFound(validator, "POST", "/a/b", List("a:observer wsp"), description)
    it should behave like methodNotAllowed(validator, "POST", "/a/b", List("AR-Payments-Billing-Support"), description, List("does not match","'DELETE|PUT'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "POST", "/a/b", description)

    // PUT /a/b has parent resource level a:admin-foo, resource level b:creator, method level AR-Payments-Billing-Support
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("AR-Payments-Billing-Support", "a:foo"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("a:creator"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("observer"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a/b", List("b:admin"), description, List("does not match","'DELETE'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a/b", description)

    // DELETE /a/b has parent resource level a:admin-foo, resource level b:creator, method level b:admin, AR-Payments-Billing-Support
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("AR-Payments-Billing-Support", "a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("a:observer%"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("a:observer wsp"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("b:foo"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "DELETE", "/a/b", description)

    // GET on /a/yes, /a/no, /a/foo
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin-foo"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin-foo"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin-foo"), description, List("'b'","yes","no"))
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin-foo", "a:observer%"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin-foo", "a:observer%"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin-foo", "a:observer%"), description, List("'b'","yes","no"))
    it should behave like resourceNotFound(validator, "GET", "/a/yes", List("a:observer%"), description, List("{yes}"))
    it should behave like resourceNotFound(validator, "GET", "/a/no", List("a:observer%"), description, List("{no}"))
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer%"), description, List("{foo}"))
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin-foo", "a:observer wsp"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin-foo", "a:observer wsp"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin-foo", "a:observer wsp"), description, List("'b'","yes","no"))
    it should behave like resourceNotFound(validator, "GET", "/a/yes", List("a:observer wsp"), description, List("{yes}"))
    it should behave like resourceNotFound(validator, "GET", "/a/no", List("a:observer wsp"), description, List("{no}"))
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer wsp"), description, List("{foo}"))
  }
}
