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
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevel extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl With Roles At Method Level"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
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

}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevelMasked extends FlatSpec with RaxRolesBehaviors {

  val description = "Wadl With Roles At Method Level"

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
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
        <resource path="/b">
          <method name="POST" rax:roles="a:admin">
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
      </resources>
    </application>)
    , configWithRolesMaskedEnabled)

  // GET on /a requires a:observer role
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"), description)
  it should behave like methodNotAllowed(validator, "GET", "/a", List("a:bar"), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowed(validator, "GET", "/a", List("a:bar", "a:admin"), description, List("does not match","'DELETE|POST|PUT'"))
  it should behave like methodNotAllowed(validator, "GET", "/a", List("a:admin"), description, List("does not match","'DELETE|POST|PUT'"))
  it should behave like methodNotAllowedWhenNoXRoles (validator, "GET", "/a", description, List("does not match","'PUT'"))

  // POST on /b requires admin role
  it should behave like accessIsAllowed(validator, "POST", "/b", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "POST", "/b", List("a:observer","a:admin"), description)
  it should behave like resourceNotFound(validator,"POST", "/b", List("a:bar"), description, List("does not match","'a'"))
  it should behave like resourceNotFound(validator,"POST", "/foo", List("a:bar"), description, List("does not match","'a'"))
  it should behave like resourceNotFound(validator,"POST", "/foo", List("a:admin"), description, List("does not match","'a|b'"))
  it should behave like methodNotAllowed(validator,"GET",  "/b", List("a:admin"), description, List("does not match","'DELETE|POST'"))
  it should behave like methodNotAllowed(validator,"POST", "/b", List("a:observer"), description, List("does not match","'DELETE'"))
  it should behave like methodNotAllowed(validator,"POST", "/b", List("a:observer", "a:foo"), description, List("does not match","'DELETE'"))


  // POST on /a requires a:admin role
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:bar", "a:admin"), description)
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:bar"), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:bar", "a:observer"), description, List("does not match","'DELETE|GET|PUT'"))
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:observer"), description, List("does not match","'DELETE|GET|PUT'"))
  it should behave like methodNotAllowedWhenNoXRoles(validator, "POST", "/a", description, List("does not match","'PUT'"))


  // DELETE on /b requires a:admin role
  it should behave like accessIsAllowed(validator, "DELETE", "/b", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/b", List("a:bar", "a:admin"), description)
  it should behave like resourceNotFound(validator, "DELETE", "/b", List("a:bar"), description, List("does not match","'a'"))
  it should behave like accessIsAllowed(validator, "DELETE", "/b", List("a:bar", "a:observer"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/b", List("a:observer"), description)
  it should behave like resourceNotFoundWhenNoXRoles(validator, "DELETE", "/b", description, List("does not match","'a'"))


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
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List(), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:bar"), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:bar", "a:jawsome"), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List("observer", "creator"), description, List("does not match","'PUT'"))
  it should behave like methodNotAllowedWhenNoXRoles(validator, "DELETE", "/a", description, List("does not match","'PUT'"))
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtResourceLevel extends FlatSpec with RaxRolesBehaviors {

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
    , configWithRolesEnabled)

  // When a single value rax:roles at resource level but not at method level
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:observer"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("b:observer"), description)
  it should behave like accessIsForbidden(validator, "GET", "/a", List("b:creator"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "GET", "/a", description)

  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"), description)
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:observer"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a", description)

  // PUT has resource level a:admin, method level a:observer
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)

  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:bar"), description)
  it should behave like accessIsForbidden(validator, "PUT", "/a", List(), description)
  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:observe"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a", description)

  // DELETE has resource level a:admin, method level a:observer and a:admin
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:admin"), description)
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:creator"), description)

  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar"), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List(), description)
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:observe"), description)
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a", description)
}


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


@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesOfAllMasked extends FlatSpec with RaxRolesBehaviors {

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
    , configWithRolesMaskedEnabled)

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
  it should behave like methodNotAllowed(validator, "PUT", "/a", List("a:observer", "a:bar"), description, List("does not match","'GET'"))
  it should behave like methodNotAllowedWhenNoXRoles(validator, "PUT", "/a", description, List("does not match","'GET'"))

  // GET on /b has resource level #all, no method level
  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List(), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List("bar"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b", description)

  // POST on /b has resource level #all, method is not allowed
  it should behave like methodNotAllowed(validator, "POST", "/b", List("a:admin"), description, List("does not match","'GET'"))
  it should behave like methodNotAllowed(validator, "POST", "/b", List(), description, List("does not match","'GET'"))

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

@RunWith(classOf[JUnitRunner])
class GivenNoRolesInWadl extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
                                    "Config With Roles Masked Enabled" -> configWithRolesMaskedEnabled)

  for ((description, configuration) <- configs) {

    val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="GET"/>
          <method name="PUT"/>
        </resource>
      </resources>
    </application>)
    , configuration)

    // GET on /a has no roles
    it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a", List(), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/a", description)

    // PUT on /a has no roles
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List(), description)
    it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a", description)

    // POST on /a has no roles, method is not allowed
    it should behave like methodNotAllowed(validator, "POST", "/a", List("a:admin"), description, List("does not match","'GET|PUT'"))
    it should behave like methodNotAllowed(validator, "POST", "/a", List(), description, List("does not match","'GET|PUT'"))
  }
}

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

@RunWith(classOf[JUnitRunner])
class RaxRolesNotInheritedFromSiblingMasked extends FlatSpec with RaxRolesBehaviors {

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
    , configWithRolesMaskedEnabled)

  it should behave like methodNotAllowed(validator, "PUT", "/a", List("a:observer", "a:bar"), description, List("does not match","'GET'"))
  it should behave like methodNotAllowedWhenNoXRoles(validator, "PUT", "/a", description, List("does not match","'GET'"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"), description)
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"), description)

  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:noone"), description)
  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:creator"), description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "POST", "/b/c", description)
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b/c", description)
}


@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResources extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesEnabledHeaderCheckDisabled)

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
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
            <resource path="{yn}" rax:roles="a:admin">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="GET"/>
            </resource>
          </resource>
        </resources>
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

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResourcesMasked extends FlatSpec with RaxRolesBehaviors {

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
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
            <resource path="{yn}" rax:roles="a:admin">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="GET"/>
            </resource>
          </resource>
        </resources>
      </application>)
      , configuration)

    // PUT /a has resource level a:admin, method level a:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a", List("b:observer"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a", description)

    // DELETE /a has resource level a:admin, method is not defined
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"), description, List("does not match","'PUT'"))
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:observer"), description, List("does not match","'PUT'"))
    it should behave like resourceNotFound(validator, "DELETE", "/a", List(), description)


    // POST /a/b has parent resource level a:admin, resource level b:creator
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"), description)
    it should behave like resourceNotFound(validator, "POST", "/a/b", List("a:observer"), description)
    it should behave like methodNotAllowed(validator, "POST", "/a/b", List("b:observer"), description, List("does not match","'DELETE|PUT'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "POST", "/a/b", description)


    // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:observer", "a:foo"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("a:creator"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("observer"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a/b", List("b:admin"), description, List("does not match","'DELETE'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a/b", description)

    // DELETE /a/b has parent resource level a:admin, resource level b:creator, method level b:admin, b:observer
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("a:observer"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("b:foo"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "DELETE", "/a/b", description)

    // GET on /a/yes, /a/no, /a/foo
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin"), description, List("'b'","yes","no"))
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin", "a:observer"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin", "a:observer"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin", "a:observer"), description, List("'b'","yes","no"))
    it should behave like resourceNotFound(validator, "GET", "/a/yes", List("a:observer"), description, List("{yes}"))
    it should behave like resourceNotFound(validator, "GET", "/a/no", List("a:observer"), description, List("{no}"))
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer"), description, List("{foo}"))
  }
}



@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResourcesAndMethodReferences extends FlatSpec with RaxRolesBehaviors {

  val configs = Map[String, Config]("Config With Roles Enabled" -> configWithRolesEnabled,
    "Config With Roles Enabled and Messsage Extensions Disabled" -> configWithRolesEnabledMessageExtDisabled,
    "Config With Roles Enabled and Duplications Removed" -> configWithRolesEnabledDupsRemoved,
    "Config With Roles Enabled and Header Checks Disabled" -> configWithRolesEnabledHeaderCheckDisabled)

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

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResourcesAndMethodReferencesMasked extends FlatSpec with RaxRolesBehaviors {

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
    it should behave like methodNotAllowed(validator, "PUT", "/a", List("b:observer"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a", description)

    // DELETE /a has resource level a:admin, method is not defined
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"), description, List("does not match","'PUT'"))
    it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:observer"), description, List("does not match","'PUT'"))
    it should behave like resourceNotFound(validator, "DELETE", "/a", List(), description)

    // POST /a/b has parent resource level a:admin, resource level b:creator
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"), description)
    it should behave like resourceNotFound(validator, "POST", "/a/b", List("a:observer"), description)
    it should behave like methodNotAllowed(validator, "POST", "/a/b", List("b:observer"), description, List("does not match","'DELETE|PUT'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "POST", "/a/b", description)

    // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:observer", "a:foo"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("a:creator"), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "PUT", "/a/b", List("observer"), description)
    it should behave like methodNotAllowed(validator, "PUT", "/a/b", List("b:admin"), description, List("does not match","'DELETE'"))
    it should behave like resourceNotFoundWhenNoXRoles(validator, "PUT", "/a/b", description)

    // DELETE /a/b has parent resource level a:admin, resource level b:creator, method level b:admin, b:observer
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "a:admin"), description)
    it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List(), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("a:observer"), description)
    it should behave like resourceNotFound(validator, "DELETE", "/a/b", List("b:foo"), description)
    it should behave like resourceNotFoundWhenNoXRoles(validator, "DELETE", "/a/b", description)

    // GET on /a/yes, /a/no, /a/foo
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin"), description, List("'b'","yes","no"))
    it should behave like accessIsAllowed(validator, "GET", "/a/yes", List("a:admin", "a:observer"), description)
    it should behave like accessIsAllowed(validator, "GET", "/a/no", List("a:admin", "a:observer"), description)
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:admin", "a:observer"), description, List("'b'","yes","no"))
    it should behave like resourceNotFound(validator, "GET", "/a/yes", List("a:observer"), description, List("{yes}"))
    it should behave like resourceNotFound(validator, "GET", "/a/no", List("a:observer"), description, List("{no}"))
    it should behave like resourceNotFound(validator, "GET", "/a/foo", List("a:observer"), description, List("{foo}"))
  }
}

