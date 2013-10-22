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

  def configWithRolesEnabled = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true)

  def accessIsAllowed(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "succeed when " +method+ " on " +path+ " and X-Roles has " + roles + "" in {
      validator.validate(request, response, chain)
    }
  }

  def accessIsForbidden(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 403 when " + method + " on " + path + " and X-Roles has " + roles + "" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }

  def methodNotAllowed(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 405 when " + method + " on " + path + " and X-Roles has " + roles + "" in {
      base.assertResultFailed(validator.validate(request, response, chain), 405)
    }
  }

  def accessIsForbiddenWhenNoXRoles(validator: => Validator, method: => String, path: => String) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 403 when " + method + " on " + path + " and no X-Roles header" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }

  def accessIsAllowedWhenNoXRoles(validator: => Validator, method: => String, path: => String) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "succeed when " +method+ " on " +path+ " and no X-Roles" in {
      validator.validate(request, response, chain)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevel extends FlatSpec with RaxRolesBehaviors {

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
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:bar"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:bar", "a:admin"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:admin"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "GET", "/a")

  // POST on /a requires a:admin role
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:bar", "a:admin"))
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:bar"))
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:bar", "a:observer"))
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:observer"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a")

  // PUT has no rax:roles defined, should allow all access
  it should behave like accessIsAllowed(validator, "PUT", "/a", List())
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:bar"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar", "a:jawsome"))
  it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a")

  // DELETE has a:observer and a:admin, treated as ORs, not ANDs
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:bar"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:admin", "a:bar"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:bar", "a:admin"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:admin"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List())
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar", "a:jawsome"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("observer", "creator"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a")
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtResourceLevel extends FlatSpec with RaxRolesBehaviors {

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
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("a:observer"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("b:observer"))
  it should behave like accessIsForbidden(validator, "GET", "/a", List("b:creator"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "GET", "/a")

  it should behave like accessIsAllowed(validator, "POST", "/a", List("a:admin"))
  it should behave like accessIsForbidden(validator, "POST", "/a", List("a:observer"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a")

  // PUT has resource level a:admin, method level a:observer
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"))

  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:bar"))
  it should behave like accessIsForbidden(validator, "PUT", "/a", List())
  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:observe"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a")

  // DELETE has resource level a:admin, method level a:observer and a:admin
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:observer", "a:admin"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a", List("a:creator"))

  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:bar"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List())
  it should behave like accessIsForbidden(validator, "DELETE", "/a", List("a:observe"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a")
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithNestedResources extends FlatSpec with RaxRolesBehaviors {
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
    , configWithRolesEnabled)

  // PUT /a has resource level a:admin, method level a:observer
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:observer", "a:admin"))
  it should behave like accessIsForbidden(validator, "PUT", "/a", List("b:observer"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a")

  // DELETE /a has resource level a:admin, method is not defined
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List("a:admin"))
  it should behave like methodNotAllowed(validator, "DELETE", "/a", List())

  // POST /a/b has parent resource level a:admin, resource level b:creator
  it should behave like accessIsAllowed(validator, "POST", "/a/b", List("a:admin"))
  it should behave like accessIsAllowed(validator, "POST", "/a/b", List("b:creator"))
  it should behave like accessIsForbidden(validator, "POST", "/a/b", List("a:observer"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "POST", "/a/b")

  // PUT /a/b has parent resource level a:admin, resource level b:creator, method level b:observer
  it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("a:admin"))
  it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:creator"))
  it should behave like accessIsAllowed(validator, "PUT", "/a/b", List("b:observer", "a:foo"))
  it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("a:creator"))
  it should behave like accessIsForbidden(validator, "PUT", "/a/b", List())
  it should behave like accessIsForbidden(validator, "PUT", "/a/b", List("observer"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a/b")

  // DELETE /a/b has parent resource level a:admin, resource level b:creator, method level b:admin, b:observer
  it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("a:admin"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:creator"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:observer", "a:admin"))
  it should behave like accessIsAllowed(validator, "DELETE", "/a/b", List("b:admin"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List())
  it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("a:observer"))
  it should behave like accessIsForbidden(validator, "DELETE", "/a/b", List("b:foo"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "DELETE", "/a/b")
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesOfAll extends FlatSpec with RaxRolesBehaviors {
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
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer", "a:bar"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:bar"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:bar", "a:admin"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List())
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/a")

  // PUT on /a has resource level a:admin, no method level
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:bar", "a:admin"))
  it should behave like accessIsForbidden(validator, "PUT", "/a", List("a:observer", "a:bar"))
  it should behave like accessIsForbiddenWhenNoXRoles(validator, "PUT", "/a")

  // GET on /b has resource level #all, no method level
  it should behave like accessIsAllowed(validator, "GET", "/b", List("a:admin"))
  it should behave like accessIsAllowed(validator, "GET", "/b", List())
  it should behave like accessIsAllowed(validator, "GET", "/b", List("bar"))
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/b")

  // POST on /b has resource level #all, method is not allowed
  it should behave like methodNotAllowed(validator, "POST", "/b", List("a:admin"))
  it should behave like methodNotAllowed(validator, "POST", "/b", List())

  // POST on /b/c has parent resource level #all, resource level c:admin, no method level
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List("c:admin"))
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List())
  it should behave like accessIsAllowed(validator, "POST", "/b/c", List("bar"))
  it should behave like accessIsAllowedWhenNoXRoles(validator, "POST", "/b/c")
}

@RunWith(classOf[JUnitRunner])
class GivenNoRolesInWadl extends FlatSpec with RaxRolesBehaviors {

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="GET"/>
          <method name="PUT"/>
        </resource>
      </resources>
    </application>)
    , configWithRolesEnabled)

  // GET on /a has no roles
  it should behave like accessIsAllowed(validator, "GET", "/a", List("a:observer"))
  it should behave like accessIsAllowed(validator, "GET", "/a", List())
  it should behave like accessIsAllowedWhenNoXRoles(validator, "GET", "/a")

  // PUT on /a has no roles
  it should behave like accessIsAllowed(validator, "PUT", "/a", List("a:admin"))
  it should behave like accessIsAllowed(validator, "PUT", "/a", List())
  it should behave like accessIsAllowedWhenNoXRoles(validator, "PUT", "/a")

  // POST on /a has no roles, method is not allowed
  it should behave like methodNotAllowed(validator, "POST", "/a", List("a:admin"))
  it should behave like methodNotAllowed(validator, "POST", "/a", List())
}
