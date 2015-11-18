/***
 *   Copyright 2015 Rackspace US, Inc.
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
import scala.xml._

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResourcesInMixFormat extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesEnabledHeaderCheckDisabled,
    "Config with Roles Enabled and Default Parameters Enabled" -> configWithRaxRolesEnabledDefaultsEnabled,
    "Config with Roles Enabled, Default Parameters Enabled and Duplications Removed" -> configWithRaxRolesEnabledDupsRemovedDefaultsEnabled)

  val wadls = Map[String, NodeSeq](
    "Mixed 1" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
          <resource path="/a" rax:roles="a:admin another:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
          <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
          </resource>
          <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
        </resources>
      </application>,
    "Mixed 2" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="b" rax:roles="b:creator">
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:admin"/>
            </resource>
          </resource>
          <resource path="a" rax:roles="another:admin">
            <resource path="b">
              <method name="POST"/>
              <method name="DELETE" rax:roles="b:observer"/>
            </resource>
          </resource>
          <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
          </resource>
          <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
        </resources>
      </application>,
    "Mixed 3" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
          <resource path="/a" rax:roles="a:admin another:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
            </resource>
          </resource>
          <resource path="/a/b">
            <method name="PUT" rax:roles="b:observer"/>
            <method name="DELETE" rax:roles="b:observer b:admin"/>
            <resource path="c" rax:roles="c:creator">
              <method name="POST"/>
            </resource>
          </resource>
          <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
        </resources>
      </application>,
    "Mixed 4" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
          <resource path="/a" rax:roles="a:admin another:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="{yn}" rax:roles="a:admin">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="GET"/>
            </resource>
            <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
            </resource>
          </resource>
          <resource path="/a/b">
            <method name="PUT" rax:roles="b:observer"/>
            <method name="DELETE" rax:roles="b:observer b:admin"/>
            <resource path="c" rax:roles="c:creator">
              <method name="POST"/>
            </resource>
          </resource>
        </resources>
      </application>,
    "Mixed 1, with X-ROLES Header in method (X-ROLES header should be ignored)" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
          <resource path="/a" rax:roles="a:admin another:admin">
            <method name="PUT" rax:roles="a:observer">
                <request>
                    <!-- Should be ignored -->
                    <param name="X-ROLES" style="header" required="true"
                        fixed="b:observer"/>
                </request>
            </method>
            <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
          <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
          </resource>
          <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
        </resources>
      </application>,
    "Mixed 1, with X-ROLES Header in resource (X-ROLES header should be ignored)" -> <application xmlns="http://wadl.dev.java.net/2009/02"
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
          <resource path="/a" rax:roles="a:admin another:admin">
          <!-- Should be ignored -->
            <param name="X-ROLES" style="header" required="true"
                fixed="b:creator"/>
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
          <resource path="/a/b/c" rax:roles="c:creator">
            <method name="POST"/>
          </resource>
          <resource path="/a/{yn}" rax:roles="a:admin">
            <param name="yn" style="template" type="tst:yesno"/>
            <method name="GET"/>
          </resource>
        </resources>
      </application>
  )

  for ((desc2, configuration) <- configs) {

    for ((desc1, wadl) <- wadls) {
      val description = desc1+": "+desc2

      val validator = Validator((localWADLURI, wadl) ,configuration)

      // PUT /a has resource level a:admin, method level a:observer
      it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a", List("b:observer"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a", List("b:creator"), description)
      it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a", description)

      // DELETE /a has resource level a:admin, method is not defined
      it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"), description)
      it should behave like methodNotAllowed(validator, "DELETE", "/a", List(), description)

      // POST /a/b has parent resource level a:admin, resource level b:creator
      it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "POST", "/a/b", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"), description)
      it should behave like accessIsForbidden(validator, "POST", "/a/b", List("a:observer"), description)
      it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a/b", description)

      // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
      it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
      it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:observer", "a:foo"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("a:creator"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a/b", List(), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("observer"), description)
      it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a/b", description)

      // DELETE /a/b has parent resource level a:admin, resource level b:creator, method level b:admin, b:observer
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"), description)
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "a:admin"), description)
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "another:admin"), description)
      it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"), description)
      it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List(), description)
      it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("a:observer"), description)
      it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("b:foo"), description)
      it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a/b", description)

      // POST /a/b/c has parent resource level a:admin, another:admin, b:creator, c:creator
      it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("b:creator"), description)
      it should behave like accessIsAllowed(validator, "POST", "/a/b/c", List("c:creator"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("c:creator"), description)
      it should behave like accessIsForbidden(validator, "POST", "/a/b", List("c:creator"), description)
      it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("c:creator"), description)
      it should behave like accessIsForbidden(validator, "PUT", "/a", List("c:creator"), description)
      it should behave like methodNotAllowed(validator, "DELETE", "/a/b/c", List("a:admin"), description)
      it should behave like methodNotAllowed(validator, "PUT", "/a/b/c", List("a:admin"), description)
      it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a/b/c", description)

      // GET on /a/yes, /a/no, /a/foo
      it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin"), description)
      it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("another:admin"), description)
      it should behave like accessIsAllowed(validator, "GET", "/a/no", List("another:admin"), description)
      it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin"), description, List("'b'","yes","no"))
      it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin", "a:observer"), description)
      it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin", "a:observer"), description)
      it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin", "a:observer"), description, List("'b'","yes","no"))
      it should behave like resourceNotFound(validator, "GET", "/a/foo", List("another:admin"), description, List("'b'","yes","no"))
      it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("another:admin", "a:observer"), description)
      it should behave like accessIsAllowed(validator, "GET", "/a/no", List("another:admin", "a:observer"), description)
      it should behave like resourceNotFound(validator, "GET", "/a/foo", List("another:admin", "a:observer"), description, List("'b'","yes","no"))
      it should behave like accessIsForbidden(validator, "GET", "/a/yes", List("a:observer"), description)
      it should behave like accessIsForbidden(validator, "GET", "/a/no", List("a:observer"), description)
      it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer"), description, List("'b'","yes","no"))
    }
  }
}
