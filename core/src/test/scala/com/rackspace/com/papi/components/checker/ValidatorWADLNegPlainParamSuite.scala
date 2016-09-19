/***
 *   Copyright 2016 Rackspace US, Inc.
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

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.rackspace.com.papi.components.checker.wadl.WADLException

@RunWith(classOf[JUnitRunner])
class ValidatorWADLNegPlainParamSuite extends BaseValidatorSuite {

  val wadlWithVer20Param =  <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true"/>
                          <!-- a silly xpath 2.0 assertion that will always return true -->
                          <param style="plain" path="string(current-dateTime())" required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>


  val wadlWithVer30Param =  <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true"/>
                          <!-- a silly xpath 2.0 assertion that will always return true -->
                          <param style="plain" path="string(current-dateTime())" required="true"/>
                          <!-- a silly XPath 3.0 example -->
                          <param style="plain" path="let $t := current-dateTime() return string($t)" required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>

  val wadlWithVer31Param =  <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true"/>
                          <!-- a silly xpath 2.0 assertion that will always return true -->
                          <param style="plain" path="string(current-dateTime())" required="true"/>
                          <!-- a silly XPath 3.0 example -->
                          <param style="plain" path="let $t := current-dateTime() return string($t)" required="true"/>
                          <!-- a silly XPath 3.1 example -->
                          <param style="plain" path="let $t := map { 't' : true() } return $t('t')" required="true"/>
                          <!-- another silly XPath 3.1 example -->
                          <param style="plain" path="let $t := 'foo'=>replace('f', 'b') return $t" required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>




  test ("2.0 XPath should fail when 1.0 is set")  {
    val thrown = intercept[WADLException] {
      Validator((localWADLURI, wadlWithVer20Param),
                TestConfig(false, false, true, true, true, 10, true))
    }
    assert (thrown.getMessage().contains("Error while compiling XPath"))
    assert (thrown.getMessage().contains("current-dateTime"))
  }

  test ("2.0 XPath should succeed when 2.0 is set")  {
    Validator((localWADLURI, wadlWithVer20Param),
             TestConfig(false, false, true, true, true, 20, true))
  }


  test ("3.0 XPath should fail when 2.0 is set")  {
    val thrown = intercept[WADLException] {
      Validator((localWADLURI, wadlWithVer30Param),
                TestConfig(false, false, true, true, true, 20, true))
    }

    assert (thrown.getMessage().contains("Error while compiling XPath"))
    assert (thrown.getMessage().contains("'let' is not permitted in XPath 2.0"))
  }


  test ("3.0 XPath should succeed when 3.0 is set")  {
    Validator((localWADLURI, wadlWithVer30Param),
              TestConfig(false, false, true, true, true, 30, true))
  }


  test ("3.1 XPath should fail when 3.0 is set")  {
    val thrown = intercept[WADLException] {
      Validator((localWADLURI, wadlWithVer31Param),
                TestConfig(false, false, true, true, true, 30, true))
    }

    assert (thrown.getMessage().contains("Error while compiling XPath"))
    assert (thrown.getMessage().contains("use XPath 3.1"))
  }

  test ("3.1 XPath should succeed when 3.1 is set") {
    Validator((localWADLURI, wadlWithVer31Param),
             TestConfig(false, false, true, true, true, 31, true))
  }

}
