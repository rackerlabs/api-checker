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

trait RaxRolesBehaviors {
  this: FlatSpec =>

  def base: BaseValidatorSuite = new BaseValidatorSuite()

  val xml = base.goodXML_XSD2

  def response: HttpServletResponse = mock(classOf[HttpServletResponse]);

  def chain: FilterChain = mock(classOf[FilterChain])

  def localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString

  def configWithRolesEnabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false, true)

  def configWithRolesEnabledDupsRemoved = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false, true)

  def configWithRolesMaskedEnabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false, true,
                                                false, true)

  def configWithRolesMaskedEnabledDupsRemoved = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false,
                                                           true, false, true)

  def configWithRolesDisabledMaskedEnabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false, false,
                                                        false, true)

  def configWithRolesDisabledHeaderCheckEnabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, true, false, false, false)

  def configWithRolesDisabledHeaderCheckDisabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, false, true, true, false, false, false)

  def configWithRolesEnabledHeaderCheckDisabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, false, true, true, false, false, true)

  def configWithRolesMaskedEnabledHeaderCheckDisabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, false, true, true, false, false, true,
                                                                  false, true)

  def configWithRolesEnabledMessageExtDisabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, false, false, false, true)

  def configWithRolesMaskedEnabledMessageExtDisabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true, true, false, false, false, true,
                                                                 false, true)

  def accessIsAllowed(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "succeed when " + method + " on " + path + " and X-Roles has " + roles + " for " + conf in {
      validator.validate(request, response, chain)
    }
  }

  def accessIsForbidden(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 403 when " + method + " on " + path + " and X-Roles has " + roles + " for " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 403, "You are forbidden to perform the operation")
    }
  }

  def methodNotAllowed(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config",
                       matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 405 when " + method + " on " + path + " and X-Roles has " + roles + " for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 405, matchStrings)
    }
  }

  def resourceNotFound(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config",
                      matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 404 when " + method + " on " + path + " and X-Roles has " + roles + " for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 404, matchStrings)
    }
  }

  def resourceNotFoundWhenNoXRoles(validator: => Validator, method: => String, path: => String, conf: => String = "Valid Config",
                                  matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 404 when " + method + " on " + path + " and no X-Roles headers for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 404, matchStrings)
    }
  }

  def methodNotAllowedWhenNoXRoles(validator: => Validator, method: => String, path: => String, conf: => String = "Valid Config",
                                  matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 405 when " + method + " on " + path + " and no X-Roles headers for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 405, matchStrings)
    }
  }

  def accessIsForbiddenWhenNoXRoles(validator: => Validator, method: => String, path: => String, conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 403 when " + method + " on " + path + " and no X-Roles header" + " for " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 403, "You are forbidden to perform the operation")
    }
  }

  def accessIsAllowedWhenNoXRoles(validator: => Validator, method: => String, path: => String, conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "succeed when " + method + " on " + path + " and no X-Roles" + " for " + conf in {
      validator.validate(request, response, chain)
    }
  }

  def accessIsAllowedWithHeader(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles, "X-Auth-Token" -> List("some-token"), "some-generic-header" -> List("something")))
    it should "succeed when " + method + " on " + path + " has an extra header and X-Roles has " + roles + " for " + conf in {
      validator.validate(request, response, chain)
    }
  }

  def accessIsForbiddenWithHeader(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles, "X-Auth-Token" -> List("some-token")))
    it should "fail with a 403 when " + method + " on " + path + " and X-Roles has " + roles + " for " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 403, "You are forbidden to perform the operation")
    }
  }

  def resourceNotFoundWithHeader(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config",
                       matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles, "X-Auth-Token" -> List("some-token")))
    it should "fail with a 404 when " + method + " on " + path + " has an extra header and X-Roles has " + roles + " for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 404, matchStrings)
    }
  }

  def methodNotAllowedWithHeader(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config",
                       matchStrings : List[String] = List()) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles, "X-Auth-Token" -> List("some-token")))
    it should "fail with a 405 when " + method + " on " + path + " has an extra header and X-Roles has " + roles + " for  " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 405, matchStrings)
    }
  }

  def badRequestWhenHeaderIsMissing(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 400 when " + method + " on " + path + " and no X-Auth-Token header" + " for " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 400, "Bad Content: Expecting an HTTP header X-Auth-Token to have a value matching .*")
    }
  }

  def badRequestWhenOneHeaderIsMissing(validator: => Validator, method: => String, path: => String, roles: => List[String], conf: => String = "Valid Config") {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles, "X-Auth-Token" -> List("something")))
    it should "fail with a 400 when " + method + " on " + path + " and no one X-Auth-Token header" + " for " + conf in {
      base.assertResultFailed(validator.validate(request, response, chain), 400, "Bad Content: Expecting an HTTP header some-generic-header to have a value matching .*")
    }
  }
}
