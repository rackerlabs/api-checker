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

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorWADLPlainParamSuite extends BaseValidatorSuite {
  //
  // validator_XSDElementContentPlain allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests.  It also checks the element type, and it checks
  // constraints against required plain params.  You can PUT an a in
  // /a/b and POST an e in /a/b
  //
  // The validator is used in the following tests.
  //
  val validator_XSDElementContentPlain = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
    </application>)
    , TestConfig(false, false, true, true, true, 10, true))

  //
  // Like validator_XSDElementContentPlain, but with custom rax:message
  //

  val validator_XSDElementContentPlainMsg = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:message="No stepType attribute on a"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:message="no stepType on e"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10, true, false, false, "Xalan",
                 false, false, false, true))

  //
  // Like validator_XSDElementContentPlain, but with custom rax:message, rax:code
  //

  val validator_XSDElementContentPlainMsgCode = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:message="No stepType attribute on a" rax:code="500"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:message="no stepType on e" rax:code="501"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10, true, false, false, "Xalan",
                 false, false, false, true))

  //
  // Like validator_XSDElementContentPlain, but with custom rax:code
  //

  val validator_XSDElementContentPlainCode = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:code="500"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:code="501"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10, true, false, false, "Xalan",
                 false, false, false, true))

  //
  // Like XSDElementContentPlain but with joinopt
  //
  val validator_XSDElementContentPlainOpt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
    </application>)
    , TestConfig(false, false, true, true, true, 10, true, false , false, "XalanC", true))

  //
  // Like validator_XSDElementContentPlainOpt but with custom rax:message
  //
  val validator_XSDElementContentPlainOptMsg = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:message="No stepType attribute on a"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:message="no stepType on e"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10,
                 true, false , false, "XalanC", true,
                 false, false, true))

  //
  // Like validator_XSDElementContentPlainOpt but with custom rax:message, rax:code
  //
  val validator_XSDElementContentPlainOptMsgCode = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:message="No stepType attribute on a" rax:code="500"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:message="no stepType on e" rax:code="501"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10,
                 true, false , false, "XalanC", true,
                 false, false, true))

  //
  // Like validator_XSDElementContentPlainOptMsgCode but using Xalan
  //
  val validator_XSDElementContentPlainOptMsgCodeX = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:message="No stepType attribute on a" rax:code="500"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:message="no stepType on e" rax:code="501"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10,
                 true, false , false, "Xalan", true,
                 false, false, true))

  //
  // Like validator_XSDElementContentPlainOpt but with custom rax:code
  //
  val validator_XSDElementContentPlainOptCode = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                          <param style="plain" path="tst:a/@stepType" required="true" rax:code="500"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e">
                          <param style="plain" path="tst:e/tst:stepType" required="true" rax:code="501"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 10,
                 true, false , false, "XalanC", true,
                 false, false, true))

  //
  //  Like validator_XSDElementContentPlain, but using an XPath 2 engine.
  //
  val validator_XSDElementContentPlain2 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
    </application>)
    , TestConfig(false, false, true, true, true, 20, true))

  //
  // Like validator_XSDElementContentPlain2 but with XPath 3.0 engine
  //
  val validator_XSDElementContentPlain30 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
    </application>)
    , TestConfig(false, false, true, true, true, 30, true))


  //
  // Like validator_XSDElementContentPlain2 but with XPath 3.1 engine
  //
  val validator_XSDElementContentPlain31 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
    </application>)
    , TestConfig(false, false, true, true, true, 31, true))

  //
  //  Like validator_XSDElementContentPlainOpt, but using an XPath 2 engine.
  //
  val validator_XSDElementContentPlainOpt2 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
                          <!-- a silly xpath assertion that will always return true -->
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
    </application>)
    , TestConfig(false, false, true, true, true, 20, true, false , false, "XalanC", true))


  val validator_XSDElementContentPlainOpt30 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
                          <!-- a silly xpath assertion that will always return true -->
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
    </application>)
    , TestConfig(false, false, true, true, true, 30, true, false , false, "XalanC", true))


  val validator_XSDElementContentPlainOpt31 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
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
                          <!-- a silly xpath assertion that will always return true -->
                          <param style="plain" path="string(current-dateTime())" required="true"/>
                          <!-- a silly XPath 3.0 example -->
                          <param style="plain" path="let $t := current-dateTime() return string($t)" required="true"/>
                          <!-- a silly XPath 3.1 example -->
                          <param style="plain" path="let $t := map { 't' : true() } return $t('t')" required="true"/>
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
    </application>)
    , TestConfig(false, false, true, true, true, 31, true, false , false, "XalanC", true))



  val badXML_Plain1 = <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                        <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <even>22</even>
                     </e>
  val badXML_Plain2 = <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                        even="22"/>

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlain with valid XML1") {
    validator_XSDElementContentPlain.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainMsg with valid XML1") {
    validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainCode with valid XML1") {
    validator_XSDElementContentPlainCode.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainMsgCode with valid XML1") {
    validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlain2 with valid XML1") {
    validator_XSDElementContentPlain2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlain30 with valid XML1") {
    validator_XSDElementContentPlain30.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlain31 with valid XML1") {
    validator_XSDElementContentPlain31.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain with valid XML1") {
    validator_XSDElementContentPlain.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainMsg with valid XML1") {
    validator_XSDElementContentPlainMsg.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainCode with valid XML1") {
    validator_XSDElementContentPlainCode.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainMsgCode with valid XML1") {
    validator_XSDElementContentPlainMsgCode.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain2 with valid XML1") {
    validator_XSDElementContentPlain2.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain30 with valid XML1") {
    validator_XSDElementContentPlain30.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain31 with valid XML1") {
    validator_XSDElementContentPlain31.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain with well formed JSON") {
    validator_XSDElementContentPlain.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainMsg with well formed JSON") {
    validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainCode with well formed JSON") {
    validator_XSDElementContentPlainCode.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainMsgCode with well formed JSON") {
    validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain2 with well formed JSON") {
    validator_XSDElementContentPlain2.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain30 with well formed JSON") {
    validator_XSDElementContentPlain30.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain31 with well formed JSON") {
    validator_XSDElementContentPlain31.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain with well formed JSON") {
    validator_XSDElementContentPlain.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainMsg with well formed JSON") {
    validator_XSDElementContentPlainMsg.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainCode with well formed JSON") {
    validator_XSDElementContentPlainCode.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainMsgCode with well formed JSON") {
    validator_XSDElementContentPlainMsgCode.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain2 with well formed JSON") {
    validator_XSDElementContentPlain2.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain30 with well formed JSON") {
    validator_XSDElementContentPlain30.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain31 with well formed JSON") {
    validator_XSDElementContentPlain31.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }


  test ("GOT on /c should succeed on validator_XSDElementContentPlain") {
    validator_XSDElementContentPlain.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainMsg") {
    validator_XSDElementContentPlainMsg.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainCode") {
    validator_XSDElementContentPlainCode.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainMsgCode") {
    validator_XSDElementContentPlainMsgCode.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlain2") {
    validator_XSDElementContentPlain2.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlain30") {
    validator_XSDElementContentPlain30.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlain31") {
    validator_XSDElementContentPlain31.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: No stepType attribute on a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "No stepType attribute on a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: no stepType on e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "no stepType on e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }


  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainMsg") {
    assertResultFailed(validator_XSDElementContentPlainMsg.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainCode") {
    assertResultFailed(validator_XSDElementContentPlainCode.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainMsgCode.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlain30") {
    assertResultFailed(validator_XSDElementContentPlain30.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlain31") {
    assertResultFailed(validator_XSDElementContentPlain31.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }


  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt with valid XML1") {
    validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsg with valid XML1") {
    validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptCode with valid XML1") {
    validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCode with valid XML1") {
    validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCodeX with valid XML1") {
    validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt2 with valid XML1") {
    validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt30 with valid XML1") {
    validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt31 with valid XML1") {
    validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt with valid XML1") {
    validator_XSDElementContentPlainOpt.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsg with valid XML1") {
    validator_XSDElementContentPlainOptMsg.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptCode with valid XML1") {
    validator_XSDElementContentPlainOptCode.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCode with valid XML1") {
    validator_XSDElementContentPlainOptMsgCode.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCodeX with valid XML1") {
    validator_XSDElementContentPlainOptMsgCodeX.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt2 with valid XML1") {
    validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt30 with valid XML1") {
    validator_XSDElementContentPlainOpt30.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt31 with valid XML1") {
    validator_XSDElementContentPlainOpt31.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt with well formed JSON") {
    validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOptMsg with well formed JSON") {
    validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOptCode with well formed JSON") {
    validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOptMsgCode with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOptMsgCodeX with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt2 with well formed JSON") {
    validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt30 with well formed JSON") {
    validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt31 with well formed JSON") {
    validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt with well formed JSON") {
    validator_XSDElementContentPlainOpt.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOptMsg with well formed JSON") {
    validator_XSDElementContentPlainOptMsg.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOptCode with well formed JSON") {
    validator_XSDElementContentPlainOptCode.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOptMsgCode with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCode.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOptMsgCodeX with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCodeX.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt2 with well formed JSON") {
    validator_XSDElementContentPlainOpt2.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt30 with well formed JSON") {
    validator_XSDElementContentPlainOpt30.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt31 with well formed JSON") {
    validator_XSDElementContentPlainOpt31.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt") {
    validator_XSDElementContentPlainOpt.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOptMsg") {
    validator_XSDElementContentPlainOptMsg.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOptCode") {
    validator_XSDElementContentPlainOptCode.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOptMsgCode") {
    validator_XSDElementContentPlainOptMsgCode.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOptMsgCodeX") {
    validator_XSDElementContentPlainOptMsgCodeX.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt2") {
    validator_XSDElementContentPlainOpt2.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt30") {
    validator_XSDElementContentPlainOpt30.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt31") {
    validator_XSDElementContentPlainOpt31.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: No stepType attribute on a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "No stepType attribute on a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "No stepType attribute on a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400, "Bad Content: Expecting tst:a/@stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }


  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: no stepType on e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "no stepType on e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "no stepType on e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400, "Bad Content: Expecting tst:e/tst:stepType")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOptMsg") {
    assertResultFailed(validator_XSDElementContentPlainOptMsg.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOptCode") {
    assertResultFailed(validator_XSDElementContentPlainOptCode.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOptMsgCode") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCode.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOptMsgCodeX") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeX.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlainOpt30") {
    assertResultFailed(validator_XSDElementContentPlainOpt30.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlainOpt31") {
    assertResultFailed(validator_XSDElementContentPlainOpt31.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }
}
