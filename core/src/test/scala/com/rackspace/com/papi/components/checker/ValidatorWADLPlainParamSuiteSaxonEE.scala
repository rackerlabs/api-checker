/***
 *   Copyright 2017 Rackspace US, Inc.
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
class ValidatorWADLPlainParamSuiteSaxonEE extends BaseValidatorSuite {
  val badXML_Plain1 = <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                        <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <even>22</even>
                     </e>
  val badXML_Plain2 = <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                        even="22"/>

  //
  // Like validator_XSDElementContentPlainOptMsgCode but using Saxon
  //
  val validator_XSDElementContentPlainOptMsgCodeS = Validator((localWADLURI,
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
                 true, false , false, "SaxonEE", true,
                 false, false, true))

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCodeS with valid XML1") {
    validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOptMsgCodeS with valid XML1") {
    validator_XSDElementContentPlainOptMsgCodeS.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOptMsgCodeS with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOptMsgCodeS with well formed JSON") {
    validator_XSDElementContentPlainOptMsgCodeS.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOptMsgCodeS") {
    validator_XSDElementContentPlainOptMsgCodeS.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400, "Bad Content: Expecting the root element to be: tst:a")
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 500, "No stepType attribute on a")
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400, "Bad Content: Expecting the root element to be: tst:e")
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 501, "no stepType on e")
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validator_XSDElementContentPlainOptMsgCodeS") {
    assertResultFailed(validator_XSDElementContentPlainOptMsgCodeS.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

}
