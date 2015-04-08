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
class GivenRaxRolesIsDisabled extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Disabled and Header Checks Enabled" -> configWithRolesDisabledHeaderCheckEnabled,
                                    "Config With Roles Disabled and Header Checks Disabled" -> configWithRolesDisabledHeaderCheckDisabled,
                                    "Config With Roles Disabled and MaskRoles Enabled" -> configWithRolesDisabledMaskedEnabled)

  for ((description, configuration) <- configs) {

    val validator = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
        </resources>
      </application>)
      , configuration)

    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:noone"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:creator"), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a", description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "DELETE", "/a/b", description)
  }
}
