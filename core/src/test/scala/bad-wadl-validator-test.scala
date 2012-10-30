package com.rackspace.com.papi.components.checker

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import com.rackspace.com.papi.components.checker.wadl.WADLException

@RunWith(classOf[JUnitRunner])
class BadWADLValidatorSuite extends BaseValidatorSuite {
  //
  //  We are simply testing that WADLTools is doing it's job catching
  //  errors, and the tha WADL Processing Error propigates correctly
  //  when cerating the validator.
  //
  //  The error cases on WADL flaws are well coverd by WADLTools
  //  tests.
  //
  test("Missing internal reference should produce WADL exception") {
    val thrown = intercept[WADLException] {
      val validator_EMPTY = Validator(
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="foo">
                   <method href="#fooMethod"/>
              </resource>
           </resources>
        </application>, assertConfig)
    }
    assert(thrown.getMessage().contains("fooMethod"))
    assert(thrown.getMessage().contains("does not seem to exist"))
  }
}
