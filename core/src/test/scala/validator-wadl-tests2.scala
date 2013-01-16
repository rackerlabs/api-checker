package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLSuite2 extends BaseValidatorSuite {
  //
  // validator_SLASH test scenarios where slash '/' is used exclusively in
  // a resource path.
  //

  val validator_SLASH = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
    <grammars/>
    <resources base="https://test.api.openstack.com">
      <resource path="/">
        <method name="GET">
          <response status="200"/>
        </method>
        <resource path="element">
            <resource path="/">
                <resource path="/">
                    <method name="GET">
                          <response status="200"/>
                    </method>
                    <resource path="element2">
                        <method name="POST">
                            <response status="200"/>
                         </method>
                    </resource>
                  </resource>
                </resource>
          </resource>
        </resource>
       </resources>
      </application>
    , assertConfig)

  test ("GET on / should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET","/"),response,chain)
  }

  test ("GET on '' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET",""),response,chain)
  }

  test ("GET on '/element' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("GET","/element"),response,chain)
  }

  test ("POST on '/element/element2' should succeed on validator_SLASH") {
    validator_SLASH.validate(request("POST","/element/element2"),response,chain)
  }

  test ("POST on / should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("POST","/"),response,chain), 405)
  }

  test ("POST on /element should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("POST","/element"),response,chain), 405)
  }

  test ("GET on /element/element2 should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/element2"),response,chain), 405)
  }

  test ("GET on /foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/foo"),response,chain), 404)
  }

  test ("GET on /element/foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/foo"),response,chain), 404)
  }

  test ("GET on /element/element2/foo should fail on validator_SLASH") {
    assertResultFailed(validator_SLASH.validate(request("GET","/element/element2/foo"),response,chain), 404)
  }

}
