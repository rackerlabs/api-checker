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

import com.rackspace.com.papi.components.checker.RunAssertionsHandler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class ValidatorWADLHeaderSuite extends BaseValidatorSuite {
  val setDefaultsConfig = {
    val tc = TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true)
    tc.setParamDefaults = true
    tc
  }

  val setDefaultsWithRemoveDups = {
    val tc = TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true)
    tc.setParamDefaults = true
    tc
  }

  //
  // validator_Header allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  // The header X-TEST must be specified
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests.  It also checks the element type.  You can PUT an a
  // in /a/b and POST an e in /a/b
  //
  // The validator is used in the following tests.
  //
  val validator_Header = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  //  Like validator_Header, but with header defaults enabled
  //
  val validator_HeaderDefaults = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" required="true" default="test" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , setDefaultsConfig)


  //
  //  Like validator_Header, but returns a custom error code on error
  //
  val validatorCode_Header = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  //  Like validator_Header, but returns a custom error message on error
  //
  val validatorMessage_Header = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  //  Like validator_Header, but returns a custom message and error code on error
  //
  val validatorMessageCode_Header = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like validator header, but expects the header to contain a fixed value
  //
  val validator_HeaderFixed = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  // Like header fixed, but expects the header to contain one of multiple fixed values
  //
  val validator_HeaderFixed2 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like header fixed2, but also expects a non-fixed header value.
  //
  val validator_HeaderFixed3 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  //  Like header fixed 3, but with default values set
  //
  val validator_HeaderFixed3Defaults = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" default="foo!" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" required="true" default="texto" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , setDefaultsConfig)


  //
  // Like header fixed3, but also returns a custom error code and message
  //
  val validatorCode_HeaderFixed3 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" fixed="foo!" required="true" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  // Like header fixed3, but has remove dups optimization enabled
  //
  val validator_HeaderFixed3Opt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  //  Like header fixed3Defaults but with remove dups optimiziation enabled
  //

  val validator_HeaderFixed3DefaultsOpt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" default="foo!" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" required="true" default="texto" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , setDefaultsWithRemoveDups)


  //
  // Like header fixed3Opt, but has a custom error message and code
  //
  val validatorCodeMessage_HeaderFixed3Opt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" fixed="foo!" required="true" repeating="true"/>
               <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" fixed="bar!" required="true" repeating="true"/>
               <param name="X-TESTO" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  // Like header fixed3, but fixed values are only allowed in the PUT request.
  //
  val validator_HeaderFixed4 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TESTO" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
                      <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like header fixed4, but with remove dups optimization on.
  //
  val validator_HeaderFixed4Opt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TESTO" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST" style="header" type="xsd:string" fixed="foo!" required="true" repeating="true"/>
                      <param name="X-TEST" style="header" type="xsd:string" fixed="bar!" required="true" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(true, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like validator header, but expects the header to be a UUID.
  //
  val validator_HeaderUUID = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST-UUID" style="header" type="tst:UUID" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like validator header, but expects the header to be an int.
  //
  val validator_HeaderInt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST-INT" style="header" type="xsd:int" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  // Like HeaderInt, but with custom error code / message on error.
  //
  val validatorCodeMessage_HeaderInt = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <param name="X-TEST-INT" style="header" type="xsd:int" rax:code="401" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  // Like validator header int, but expects the header to only be required in the PUT request.
  //
  val validator_HeaderIntPut = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  //
  // Like validator header int, but expects the header to only be required in the PUT request.
  //
  val validator_HeaderIntPutMix = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
              <param name="X-TEST" style="header" type="xsd:string" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))

  //
  //  Like validator_HeaderIntPutMix but with defaults enabled
  //
  val validator_HeaderIntPutMixDefaults = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
              <param name="X-TEST" style="header" type="xsd:string" required="true" default="foo!" repeating="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST-INT" style="header" type="xsd:int" required="true" default="54321" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , setDefaultsConfig)

  //
  // Like validator header int put mix, but with custom error message and code
  //
  val validatorCodeMessage_HeaderIntPutMix = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
              <param name="X-TEST" style="header" type="xsd:string" rax:code="401" rax:message="No!" required="true" repeating="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST-INT" style="header" type="xsd:int" rax:code="401" rax:message="No!" required="true" repeating="true"/>
                      <representation mediaType="application/xml" element="tst:a"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
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
    , TestConfig(false, false, true, true, true, 1, true, true, true, "XalanC", true, true))


  // testing allResults traversal
  test ("Ensure allResults returns all Results with main at the head of the list" ) {

    assertAllResultsCorrect( validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),
                                                           response,
                                                           chain ),
                             4 )
  }


  // verifies that header error supersedes media type errors
  test ( "POST on /a/b with application/JSON and no header should fail") {
    assertResultFailed( validator_Header.validate( request( "POST", "/a/b", "application/json", goodJSON,
    false, Map[String, List[String]]()), response, chain),
                        400 )
  }

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1 (multiple X-TEST headers)") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
  }

  test ("PUT on /a/b with camel case X-test header should succeed on validator_Header") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-test"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header with valid XML1") {
    validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_Header with well formed JSON") {
    validator_Header.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_Header with well formed JSON") {
    validator_Header.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_Header") {
    validator_Header.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header  if the X-TEST header isn't set") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header  if the X-TEST header is mispelled") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_Header") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  // verifies that header error supersedes media type errors
  test ( "POST on /a/b with application/JSON and no header with default set should fail with a 415") {
   assertResultFailed(validator_HeaderDefaults.validate(request( "POST", "/a/b", "application/json", goodJSON, false, Map[String, List[String]]()), response, chain), 415)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderDefaults with valid XML1") {
    validator_HeaderDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderDefaults with valid XML1 (multiple X-TEST headers)") {
    validator_HeaderDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderDefaults with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
  }

  test ("PUT on /a/b with camel case X-test header should succeed on validator_HeaderDefaults") {
    validator_HeaderDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-test"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderDefaults with valid XML1") {
    validator_HeaderDefaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderDefaults with well formed JSON") {
    validator_HeaderDefaults.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderDefaults with well formed JSON") {
    validator_HeaderDefaults.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderDefaults") {
    validator_HeaderDefaults.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should succeed on validator_headerDefaults  with well formed XML PUT, correct default header should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("test"))
    })
    validator_HeaderDefaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should succeed with a default value with well formed XML PUT, if the X-TEST header is mispelled, and defaults are set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("test"))
      //  Misspelled header should also be set
      assert(csReq.getHeaders("X-TESTs").toList == List("foo"))
    })
    validator_HeaderDefaults.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderDefaults  if the X-TEST header isn't set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("test"))
    })
    validator_HeaderDefaults.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderDefaults  if the X-TEST header is mispelled") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("test"))
      //  Misspelled header should also be set
      assert(csReq.getHeaders("X-TESTs").toList == List("foo"))
    })
    validator_HeaderDefaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderDefaults") {
    assertResultFailed(validator_HeaderDefaults.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderDefaults") {
    assertResultFailed(validator_HeaderDefaults.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderDefaults") {
    assertResultFailed(validator_HeaderDefaults.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderDefaults") {
    assertResultFailed(validator_HeaderDefaults.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_Header with valid XML1") {
    validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_Header with valid XML1 (multiple X-TEST headers)") {
    validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_Header with valid XML1 (multiple X-TEST vaules in a single header)") {
    validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_Header with valid XML1") {
    validatorCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCode_Header with well formed JSON") {
    validatorCode_Header.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCode_Header with well formed JSON") {
    validatorCode_Header.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorCode_Header") {
    validatorCode_Header.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 401)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo"))),response,chain), 401)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_Header  if the X-TEST header isn't set") {
    assertResultFailed(validatorCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 401)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_Header  if the X-TEST header is mispelled") {
    assertResultFailed(validatorCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo"))),response,chain), 401)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorCode_Header") {
    assertResultFailed(validatorCode_Header.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessage_Header with valid XML1") {
    validatorMessage_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessage_Header with valid XML1 (multiple X-TEST headers)") {
    validatorMessage_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessage_Header with valid XML1 (multiple X-TEST vaules in a single header)") {
    validatorMessage_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessage_Header with valid XML1") {
    validatorMessage_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorMessage_Header with well formed JSON") {
    validatorMessage_Header.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorMessage_Header with well formed JSON") {
    validatorMessage_Header.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorMessage_Header") {
    validatorMessage_Header.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400, "Bad Content: No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo"))),response,chain), 400, "Bad Content: No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessage_Header  if the X-TEST header isn't set") {
    assertResultFailed(validatorMessage_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400, "Bad Content: No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessage_Header  if the X-TEST header is mispelled") {
    assertResultFailed(validatorMessage_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo"))),response,chain), 400, "Bad Content: No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorMessage_Header") {
    assertResultFailed(validatorMessage_Header.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessageCode_Header with valid XML1") {
    validatorMessageCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessageCode_Header with valid XML1 (multiple X-TEST headers)") {
    validatorMessageCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorMessageCode_Header with valid XML1 (multiple X-TEST vaules in a single header)") {
    validatorMessageCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessageCode_Header with valid XML1") {
    validatorMessageCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorMessageCode_Header with well formed JSON") {
    validatorMessageCode_Header.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorMessageCode_Header with well formed JSON") {
    validatorMessageCode_Header.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorMessageCode_Header") {
    validatorMessageCode_Header.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo"))),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessageCode_Header  if the X-TEST header isn't set") {
    assertResultFailed(validatorMessageCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorMessageCode_Header  if the X-TEST header is mispelled") {
    assertResultFailed(validatorMessageCode_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorMessageCode_Header") {
    assertResultFailed(validatorMessageCode_Header.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed with valid XML1") {
    validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed with valid XML1") {
    validator_HeaderFixed.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed with well formed JSON") {
    validator_HeaderFixed.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed with well formed JSON") {
    validator_HeaderFixed.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed") {
    validator_HeaderFixed.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_Header with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_Header with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed") {
    assertResultFailed(validator_HeaderFixed.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (bar!)") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1") {
    validator_HeaderFixed2.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed2 with valid XML1 (bar!)") {
    validator_HeaderFixed2.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed2 with well formed JSON") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed2 with well formed JSON (bar!)") {
    validator_HeaderFixed2.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed2 with well formed JSON") {
    validator_HeaderFixed2.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed2 with well formed JSON (bar!)") {
    validator_HeaderFixed2.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed2") {
    validator_HeaderFixed2.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed2  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed2.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed2  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed2.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed2 with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed2 with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed2") {
    assertResultFailed(validator_HeaderFixed2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (bar!)") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1") {
    validator_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3 with valid XML1 (bar!)") {
    validator_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3 with well formed JSON") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3 with well formed JSON (bar!)") {
    validator_HeaderFixed3.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3 with well formed JSON") {
    validator_HeaderFixed3.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3 with well formed JSON (bar!)") {
    validator_HeaderFixed3.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed3") {
    validator_HeaderFixed3.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3 with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3 with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3 with valid XML1 when alternate header is not present.") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3 with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed3") {
    assertResultFailed(validator_HeaderFixed3.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (bar!)") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (multiple X-TEST headers, at least one match)") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (multiple X-TEST headers, multiple match)") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (multiple X-TEST vaules in a single header)") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1") {
    validatorCode_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_HeaderFixed3 with valid XML1 (bar!)") {
    validatorCode_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCode_HeaderFixed3 with well formed JSON") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCode_HeaderFixed3 with well formed JSON (bar!)") {
    validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCode_HeaderFixed3 with well formed JSON") {
    validatorCode_HeaderFixed3.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCode_HeaderFixed3 with well formed JSON (bar!)") {
    validatorCode_HeaderFixed3.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorCode_HeaderFixed3") {
    validatorCode_HeaderFixed3.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_HeaderFixed3  if the X-TEST header isn't set") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorCode_HeaderFixed3  if the X-TEST header is mispelled") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validatorCode_HeaderFixed3 with valid XML1 when header does not match fixed value") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCode_HeaderFixed3 with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCode_HeaderFixed3 with valid XML1 when alternate header is not present.") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCode_HeaderFixed3 with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, but does not validate against the schema in validatorCode_HeaderFixed3") {
    assertResultFailed(validatorCode_HeaderFixed3.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (bar!)") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1") {
    validator_HeaderFixed3Defaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults with valid XML1 (bar!)") {
    validator_HeaderFixed3Defaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3Defaults with well formed JSON") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3Defaults with well formed JSON (bar!)") {
    validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3Defaults with well formed JSON") {
    validator_HeaderFixed3Defaults.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3Defaults with well formed JSON (bar!)") {
    validator_HeaderFixed3Defaults.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed3Defaults") {
    validator_HeaderFixed3Defaults.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed3Defaults, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })
    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed3Defaults, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                               "X-TESTO"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("boo"))
      //  Misspelled header should be accounted for
      assert(csReq.getHeaders("X-TESTs").toList == List("foo!"))
    })
    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults  if the X-TEST header isn't set, defaults should be set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })
    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Defaults  if the X-TEST header is mispelled, defaults should be set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                "X-TESTO"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("boo"))
      //  Misspelled header should be accounted for
      assert(csReq.getHeaders("X-TESTs").toList == List("foo!"))
    })

    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed3Defaults") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed3Defaults") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed3Defaults") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Defaults with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Defaults with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Defaults with valid XML1 when alternate header is not present.") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("bar!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })

    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Defaults with valid XML1 when alternate header is mispelled") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                               "X-TESTOS"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("bar!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
      // Assert misspelled header
      assert(csReq.getHeaders("X-TESTOS").toList == List("boo"))
    })

    validator_HeaderFixed3Defaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed3Defaults") {
    assertResultFailed(validator_HeaderFixed3Defaults.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (bar!)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1") {
    validator_HeaderFixed3DefaultsOpt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt with valid XML1 (bar!)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3DefaultsOpt with well formed JSON") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3DefaultsOpt with well formed JSON (bar!)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3DefaultsOpt with well formed JSON") {
    validator_HeaderFixed3DefaultsOpt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3DefaultsOpt with well formed JSON (bar!)") {
    validator_HeaderFixed3DefaultsOpt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed3DefaultsOpt") {
    validator_HeaderFixed3DefaultsOpt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed3DefaultsOpt, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })
    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed3DefaultsOpt, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                               "X-TESTO"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("boo"))
      //  Misspelled header should be accounted for
      assert(csReq.getHeaders("X-TESTs").toList == List("foo!"))
    })
    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt  if the X-TEST header isn't set, defaults should be set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false)

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })
    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3DefaultsOpt  if the X-TEST header is mispelled, defaults should be set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                "X-TESTO"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("boo"))
      //  Misspelled header should be accounted for
      assert(csReq.getHeaders("X-TESTs").toList == List("foo!"))
    })

    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed3DefaultsOpt") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed3DefaultsOpt") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed3DefaultsOpt") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3DefaultsOpt with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3DefaultsOpt with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3DefaultsOpt with valid XML1 when alternate header is not present.") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("bar!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
    })

    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3DefaultsOpt with valid XML1 when alternate header is mispelled") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                               "X-TESTOS"->List("boo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("bar!"))
      assert(csReq.getHeaders("X-TESTO").toList == List("texto"))
      // Assert misspelled header
      assert(csReq.getHeaders("X-TESTOS").toList == List("boo"))
    })

    validator_HeaderFixed3DefaultsOpt.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed3DefaultsOpt") {
    assertResultFailed(validator_HeaderFixed3DefaultsOpt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (bar!)") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1") {
    validator_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Opt with valid XML1 (bar!)") {
    validator_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3Opt with well formed JSON") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed3Opt with well formed JSON (bar!)") {
    validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3Opt with well formed JSON") {
    validator_HeaderFixed3Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed3Opt with well formed JSON (bar!)") {
    validator_HeaderFixed3Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed3Opt") {
    validator_HeaderFixed3Opt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Opt  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed3Opt  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Opt with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Opt with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Opt with valid XML1 when alternate header is not present.") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed3Opt with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed3Opt") {
    assertResultFailed(validator_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (bar!)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, at least one match)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (multiple X-TEST headers, multiple match)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (multiple X-TEST vaules in a single header)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt with valid XML1 (bar!)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCodeMessage_HeaderFixed3Opt with well formed JSON") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCodeMessage_HeaderFixed3Opt with well formed JSON (bar!)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCodeMessage_HeaderFixed3Opt with well formed JSON") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCodeMessage_HeaderFixed3Opt with well formed JSON (bar!)") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                 "X-TESTO"->List("boo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorCodeMessage_HeaderFixed3Opt") {
    validatorCodeMessage_HeaderFixed3Opt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt  if the X-TEST header isn't set") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 401, "No!")
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderFixed3Opt  if the X-TEST header is mispelled") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                                          "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validatorCodeMessage_HeaderFixed3Opt with valid XML1 when header does not match fixed value") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCodeMessage_HeaderFixed3Opt with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCodeMessage_HeaderFixed3Opt with valid XML1 when alternate header is not present.") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b with application/xml should fail on validatorCodeMessage_HeaderFixed3Opt with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorCodeMessage_HeaderFixed3Opt") {
    assertResultFailed(validatorCodeMessage_HeaderFixed3Opt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (bar!)") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1") {
    validator_HeaderFixed4.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTO"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4 with valid XML1 (bar!)") {
    validator_HeaderFixed4.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTO"->List("bar"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed4 with well formed JSON") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed4 with well formed JSON (bar!)") {
    validator_HeaderFixed4.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed4 with well formed JSON") {
    validator_HeaderFixed4.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed4 with well formed JSON (bar!)") {
    validator_HeaderFixed4.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed4") {
    validator_HeaderFixed4.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed4.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed4.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4 with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4 with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4 with valid XML1 when alternate header is not present.") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4 with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed4") {
    assertResultFailed(validator_HeaderFixed4.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (bar!)") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (multiple X-TEST headers, at least one match)") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (multiple X-TEST headers, at least one match (bar!))") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (multiple X-TEST headers, multiple match)") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!","foo!"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (multiple X-TEST vaules in a single header (bar!))") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!, bar"),
                                                                                                     "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1") {
    validator_HeaderFixed4Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTO"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4Opt with valid XML1 (bar!)") {
    validator_HeaderFixed4Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTO"->List("bar"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed4Opt with well formed JSON") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("foo!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderFixed4Opt with well formed JSON (bar!)") {
    validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->List("bar!"),
                                                                                                  "X-TESTO"->List("boo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed4Opt with well formed JSON") {
    validator_HeaderFixed4Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("foo!"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderFixed4Opt with well formed JSON (bar!)") {
    validator_HeaderFixed4Opt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->List("bar!"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderFixed4Opt") {
    validator_HeaderFixed4Opt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set on validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled on validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->List("foo!"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4Opt  if the X-TEST header isn't set") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderFixed4Opt  if the X-TEST header is mispelled") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo!"),
                                                                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo!"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->List("foo!"),
                                                                                                                    "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4Opt with valid XML1 when header does not match fixed value") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4Opt with valid XML1 when header does not match fixed value (multiple headers)") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar","stool"),
                                                                                                                        "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4Opt with valid XML1 when alternate header is not present.") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderFixed4Opt with valid XML1 when alternate header is mispelled") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("bar!"),
                                                                                                                        "X-TESTOS"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderFixed4Opt") {
    assertResultFailed(validator_HeaderFixed4Opt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->List("foo!"),
                                                                         "X-TESTO"->List("boo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1 (multiple X-TEST-UUID headers)") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90","0c564dc4-3293-11e2-8f6d-db2d9ee11d60"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1 (multiple X-TEST-UUID values in a single header)") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90, 0c564dc4-3293-11e2-8f6d-db2d9ee11d60"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1") {
    validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderUUID with well formed JSON") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderUUID with well formed JSON") {
    validator_HeaderUUID.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderUUID") {
    validator_HeaderUUID.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID header isn't set") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID header is mispelled") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUIDs"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID contains malformed data") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID contains malformed data (multiple X-TEST-UUID headers)") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("0c564dc4-3293-11e2-8f6d-db2d9ee11d60","b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID contains malformed data (multiple X-TEST-UUID values in a single header)") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("0c564dc4-3293-11e2-8f6d-db2d9ee11d60, b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header isn't set") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header is mispelled") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUIDs"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID contains malformed data") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1 (multiple headers)") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90","100"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1 (multiple values in a single header)") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90, 100"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1") {
    validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderInt with well formed JSON") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderInt with well formed JSON") {
    validator_HeaderInt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderInt") {
    validator_HeaderInt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data (multiple headers)") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90","foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data (multiple header values in a single header)") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90, foo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT header isn't set") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT header is mispelled") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT contains malformed data") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

 test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt with valid XML1") {
    validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt with valid XML1 (multiple headers)") {
    validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90","100"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt with valid XML1 (multiple values in a single header)") {
    validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90, 100"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt with valid XML1") {
    validatorCodeMessage_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCodeMessage_HeaderInt with well formed JSON") {
    validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCodeMessage_HeaderInt with well formed JSON") {
    validatorCodeMessage_HeaderInt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorCodeMessage_HeaderInt") {
    validatorCodeMessage_HeaderInt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"))),response,chain), 401, "No!")
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data (multiple headers) on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90","foo"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data (multiple header values in a single header) on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90, foo"))),response,chain), 401, List("No!"))
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt  if the X-TEST-INT header isn't set") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 401, List("No!"))
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt  if the X-TEST-INT header is mispelled") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"))),response,chain), 401, List("No!"))
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderInt  if the X-TEST-INT contains malformed data") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorCodeMessage_HeaderInt") {
    assertResultFailed(validatorCodeMessage_HeaderInt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPut with valid XML1") {
    validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut with valid XML1") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderIntPut with well formed JSON") {
    validator_HeaderIntPut.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderIntPut with well formed JSON") {
    validator_HeaderIntPut.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderIntPut") {
    validator_HeaderIntPut.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT header isn't set") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response, chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT header is mispelled") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT contains malformed data") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPutMix with valid XML1") {
    validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix with valid XML1") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderIntPutMix with well formed JSON") {
    validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderIntPutMix with well formed JSON") {
    validator_HeaderIntPutMix.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-foo"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderIntPutMix") {
    validator_HeaderIntPutMix.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderIntPutMix if X-TEST is not set") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT header isn't set") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response, chain)
  }

  test ("POST on /a/b with application/xml should fail on validator_HeaderIntPutMix  if the X-TEST iheader isn't set") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("50"))),response, chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT header is mispelled") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT contains malformed data") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should failed with well formed XML POST in the wrong location in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults with valid XML1") {
    validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults with valid XML1") {
    validator_HeaderIntPutMixDefaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderIntPutMixDefaults with well formed JSON") {
    validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderIntPutMixDefaults with well formed JSON") {
    validator_HeaderIntPutMixDefaults.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-foo"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderIntPutMixDefaults") {
    validator_HeaderIntPutMixDefaults.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults if X-TEST is not set, default values should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TEST-INT").toList == List("90"))
    })

    validator_HeaderIntPutMixDefaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST-INT header isn't set in validator_HeaderIntPutMixDefaults, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo"))
      assert(csReq.getHeaders("X-TEST-INT").toList == List("54321"))
    })

    validator_HeaderIntPutMixDefaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should succeed with well formed XML PUT, if the X-TEST-INT header is mispelled in validator_HeaderIntPutMixDefaults, defaults should be set") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo"))
      assert(csReq.getHeaders("X-TEST-INT").toList == List("54321"))
      // Assert misspelled header
      assert(csReq.getHeaders("X-TEST-INTs").toList == List("90"))
    })

    validator_HeaderIntPutMixDefaults.validate(req ,response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validator_HeaderIntPutMixDefaults") {
    assertResultFailed(validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults  if the X-TEST-INT header isn't set, defaults should  be set") {
    validator_HeaderIntPutMixDefaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response, chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults  if the X-TEST header isn't set, defaults should be set") {
    val req = request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("50")))

    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct default header should be set
      assert(csReq.getHeaders("X-TEST").toList == List("foo!"))
      assert(csReq.getHeaders("X-TEST-INT").toList == List("50"))
    })

    validator_HeaderIntPutMixDefaults.validate(req ,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults  if the X-TEST-INT header is mispelled") {
    validator_HeaderIntPutMixDefaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMixDefaults  if the X-TEST-INT contains malformed data") {
    validator_HeaderIntPutMixDefaults.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderIntPutMixDefaults") {
    assertResultFailed(validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should failed with well formed XML POST in the wrong location in validator_HeaderIntPutMixDefaults") {
    assertResultFailed(validator_HeaderIntPutMixDefaults.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderIntPutMixDefaults") {
    assertResultFailed(validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderIntPutMixDefaults") {
    assertResultFailed(validator_HeaderIntPutMixDefaults.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validatorCodeMessage_HeaderIntPutMix with valid XML1") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderIntPutMix with valid XML1") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validatorCodeMessage_HeaderIntPutMix with well formed JSON") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /c with application/json should succeed on validatorCodeMessage_HeaderIntPutMix with well formed JSON") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-foo"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("GOT on /c should succeed on validatorCodeMessage_HeaderIntPutMix") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b with application/xml should fail on validatorCodeMessage_HeaderIntPutMix if X-TEST is not set") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo"))),response,chain), 401, List("No!"))
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain), 401, List("No!"))
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderIntPutMix  if the X-TEST-INT header isn't set") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->List("foo"))),response, chain)
  }

  test ("POST on /a/b with application/xml should fail on validatorCodeMessage_HeaderIntPutMix  if the X-TEST iheader isn't set") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("50"))),response, chain), 401, List("No!"))
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderIntPutMix  if the X-TEST-INT header is mispelled") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->List("90"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validatorCodeMessage_HeaderIntPutMix  if the X-TEST-INT contains malformed data") {
    validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("foo"), "X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("POST on /a/b should failed with well formed XML POST in the wrong location in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validatorCodeMessage_HeaderIntPutMix") {
    assertResultFailed(validatorCodeMessage_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->List("90"), "X-TEST"->List("foo"))),response,chain), 400)
  }
}
