package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.w3c.dom.Document
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

  def allowAccess(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "succeed when " +method+ " on " +path+ " and X-Roles has " + roles + "" in {
      validator.validate(request, response, chain)
    }
  }

  def preventAccess(validator: => Validator, method: => String, path: => String, roles: => List[String]) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false, Map("X-ROLES" -> roles))
    it should "fail with a 403 when " + method + " on " + path + " and X-Roles has " + roles + "" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }

  def preventAccessWhenNoXRoles(validator: => Validator, method: => String, path: => String) {
    def request: HttpServletRequest = base.request(method, path, "application/xml", xml, false)
    it should "fail with a 403 when " + method + " on " + path + " and no X-Roles header" in {
      base.assertResultFailed(validator.validate(request, response, chain), 403)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtMethodLevel extends FlatSpec with RaxRolesBehaviors {

  val localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString
  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                 xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
          <method name="POST" rax:roles="foo:creator">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="GET" rax:roles="foo:observer">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  "A validator (with WADL containing roles at method level)"
  it should behave like allowAccess(validator, "GET", "/a", List("foo:observer"))
  it should behave like allowAccess(validator, "POST", "/a", List("foo:creator"))

  it should behave like preventAccess(validator, "POST", "/a", List("foo:observer"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:creator"))

  it should behave like preventAccessWhenNoXRoles(validator, "POST", "/a")
  it should behave like preventAccessWhenNoXRoles(validator, "GET", "/a")

}

@RunWith(classOf[JUnitRunner])
class GivenAWadlWithRolesAtResourceLevel extends FlatSpec with RaxRolesBehaviors {
  val localWADLURI = (new File(System.getProperty("user.dir"), "mywadl.wadl")).toURI.toString

  val validator = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                 xmlns:rax="http://docs.rackspace.com/api">
      <resources base="https://test.api.openstack.com">
        <resource path="/a" rax:roles="foo:creator">
          <method name="POST">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="GET">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  "A validator (with WADL containing foo:creator role at resource level)"
  it should behave like allowAccess(validator, "GET", "/a", List("foo:creator"))
  it should behave like allowAccess(validator, "POST", "/a", List("foo:creator"))

  it should behave like preventAccess(validator, "POST", "/a", List("foo:observer"))
  it should behave like preventAccess(validator, "GET", "/a", List("foo:observer"))

  it should behave like preventAccessWhenNoXRoles(validator, "POST", "/a")
  it should behave like preventAccessWhenNoXRoles(validator, "GET", "/a")

}
