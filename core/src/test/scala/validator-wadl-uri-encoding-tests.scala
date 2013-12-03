package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._


@RunWith(classOf[JUnitRunner])
class ValidatorWADLURIEncodingSuite extends BaseValidatorSuite {
  val validator_URIEncoding = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/+hello">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
              <resource path="/a/ hello">
                   <method name="PUT">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>
    , assertConfig)

  test("GET on /a/+hello should succeed on validator_URIEncoding") {
    validator_URIEncoding.validate(request("GET","/a/+hello"), response, chain)
  }

  test("PUT on /a/ hello should succeed on validator_URIEncoding") {
    validator_URIEncoding.validate(request("PUT","/a/%20hello"), response, chain)
  }

  test("GET on /a/ hello should fail on validator_URIEncoding") {
    assertResultFailed(validator_URIEncoding.validate(request("GET","/a/%20hello"),response,chain), 405)
  }

  test("PUT on /a/+hello should fail on validator_URIEncoding") {
    assertResultFailed(validator_URIEncoding.validate(request("PUT","/a/+hello"),response,chain), 405)
  }
}
