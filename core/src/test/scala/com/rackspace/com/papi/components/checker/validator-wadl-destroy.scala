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

import javax.management.InstanceAlreadyExistsException

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import com.rackspace.com.papi.components.checker.handler.InstrumentedHandler

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLDestroySuite extends BaseValidatorSuite {
  val testWADL = <application xmlns="http://wadl.dev.java.net/2009/02">
                   <grammars/>
                   <resources base="https://test.api.openstack.com">
                      <resource/>
                   </resources>
                 </application>

  test("If destroy is called the same WADL may be created twice with the same name") {
    var validator1 = Validator("ATestWADL",testWADL, assertConfig)
    validator1.destroy
    validator1 = null

    var validator2 = Validator("ATestWADL",testWADL, assertConfig)
    validator2.destroy
    validator2 = null
  }

  test("If destroy is called the same WADL may be created twice with the same name (with instrumented handler)") {
    val instConfig = TestConfig()
    instConfig.resultHandler = new InstrumentedHandler
    var validator1 = Validator("ATestWADLInst",testWADL, instConfig)
    validator1.destroy
    validator1 = null

    var validator2 = Validator("ATestWADLInst",testWADL, instConfig)
    validator2.destroy
    validator2 = null
  }

  test("If destroy is *NOT* called then creating the the same validator the same name should cause an exception") {
    var validator1 = Validator("ATestWADL",testWADL, assertConfig)
    validator1 = null

    intercept[InstanceAlreadyExistsException] {
      var validator2 = Validator("ATestWADL",testWADL, assertConfig)
    }
  }

  test("If destroy is *NOT* called then creating the the same validator the same name should cause an exception (with instrumented handler)") {
    val instConfig = TestConfig()
    instConfig.resultHandler = new InstrumentedHandler
    var validator1 = Validator("ATestWADLInst",testWADL, instConfig)
    validator1 = null

    intercept[InstanceAlreadyExistsException] {
      var validator2 = Validator("ATestWADLInst",testWADL, instConfig)
    }
  }

}
