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
class ValidatorWADLHeaderSuite extends BaseValidatorSuite {
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
               <param name="X-TEST" style="header" type="xsd:string" required="true"/>
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
  // Like validator header, but expects the header to contain a mixed value
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
               <param name="X-TEST" style="header" type="xsd:string" fixed="foo" required="true"/>
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
               <param name="X-TEST-UUID" style="header" type="tst:UUID" required="true"/>
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
               <param name="X-TEST-INT" style="header" type="xsd:int" required="true"/>
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
                      <param name="X-TEST-INT" style="header" type="xsd:int" required="true"/>
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
              <param name="X-TEST" style="header" type="xsd:string" required="true"/>
               <method name="PUT">
                  <request>
                      <param name="X-TEST-INT" style="header" type="xsd:int" required="true"/>
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

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1 (multiple X-TEST headers)") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo","bar"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_Header with valid XML1 (multiple X-TEST vaules in a single header)") {
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->List("foo, bar"))),response,chain)
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
}
