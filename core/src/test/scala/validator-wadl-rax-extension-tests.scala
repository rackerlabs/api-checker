package com.rackspace.com.papi.components.checker

import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRaxExtensionSuite extends BaseValidatorSuite {

  //
  // validator_RolesAtResource allows:
  //
  // PUT /a when header X-Roles includes foo:creator
  // POST /a when header X-Roles includes foo:creator
  //
  // The validator checks that the request contains the
  // appropriate role to access the resource.  You can PUT an a
  // in /a and POST an e in /a if you have the X-Roles with a value
  // that includes "foo:creator"
  //
  val validator_RolesAtResource = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
           <resource path="/a" rax:roles="foo:creator">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
        </resources>
    </application>)
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // validator_RolesAtMethod allows:
  //
  // POST /a when header X-Roles includes foo:creator
  // GET /a when header X-Roles includes foo:observer
  //
  // The validator checks that the request contains the
  // appropriate role to access the resource.  You can GET and
  // POST to /a only if you have an X-Roles header with the
  // expected value
  //
  val validator_RolesAtMethod = Validator((localWADLURI,
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


  test ("POST on /a should succeed on validator_RolesAtMethod with X-Roles header of foo:creator") {
    validator_RolesAtMethod.validate(request("POST","/a","application/xml", goodXML_XSD2, false, Map("X-Roles"->List("foo:creator"))),response,chain)
  }

  test ("GET on /a should succeed on validator_RolesAtMethod with X-Roles header of foo:observer") {
    validator_RolesAtMethod.validate(request("GET","/a","application/xml", goodXML_XSD2, false, Map("X-Roles"->List("foo:observer"))),response,chain)
  }

  test ("POST on /a should fail with a 403, if X-Roles header does not contain foo:creator") {
    assertResultFailed(validator_RolesAtMethod.validate(request("POST","/a","application/xml", goodXML_XSD2, false, Map("X-Roles"->List("foo:observer"))),response,chain), 403)
  }

  test ("GET on /a should fail with a 403,  if X-Roles header does not contain foo:observer") {
    assertResultFailed(validator_RolesAtMethod.validate(request("GET","/a","application/xml", goodXML_XSD2, false, Map("X-Roles"->List("foo:creator"))),response,chain), 403)
  }

  test ("POST on /a should fail with a 403, if X-Roles header isn't set") {
    assertResultFailed(validator_RolesAtMethod.validate(request("POST","/a","application/xml", goodXML_XSD2, false),response,chain), 403)
  }

  test ("GET on /a should fail with a 403, if X-Roles header isn't set") {
    assertResultFailed(validator_RolesAtMethod.validate(request("POST","/a","application/xml", goodXML_XSD2, false),response,chain), 403)
  }

}
