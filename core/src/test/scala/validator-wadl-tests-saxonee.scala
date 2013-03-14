package com.rackspace.com.papi.components.checker

import java.io.File
import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLSuiteSaxonEE extends BaseValidatorSuite {
    //
  //  The following assertions are used for the next couple of tests
  //
  def WADLSchemaAssertions (validator : Validator) {

    test ("GET on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f should succeed on "+validator) {
      validator.validate(request("GET","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f"),response,chain)
    }

    test ("GET on /path/to/my/resource/e5b13268-6477-11e1-8e8a-ff0ea421704f should succeed on "+validator) {
      validator.validate(request("GET","/path/to/my/resource/e5b13268-6477-11e1-8e8a-ff0ea421704f"),response,chain)
    }

    test ("GET on /path/to/my/resource/16dfce76-6478-11e1-9e38-97e6e1882c28 should succeed on "+validator) {
      validator.validate(request("GET","/path/to/my/resource/16dfce76-6478-11e1-9e38-97e6e1882c28"),response,chain)
    }

    test ("DELETE on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f should fail on "+validator) {
      assertResultFailed(validator.validate(request("DELETE","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f"),response,chain), 405, Map("Allow"->"GET"))
    }

    test ("GET on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498z should fail on "+validator) {
      assertResultFailed(validator.validate(request("GET","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498z"),response,chain), 404)
    }

    test ("GET on /path/to/100 should succeed on "+validator) {
      validator.validate(request("GET","/path/to/100"),response,chain)
    }

    test ("GET on /path/to/20 should succeed on "+validator) {
      validator.validate(request("GET","/path/to/20"),response,chain)
    }

    test ("GET on /path/to/hello should fail on "+validator) {
      assertResultFailed(validator.validate(request("GET","/path/to/hello"),response,chain), 404)
    }

    test ("GET on /path/to/101 should fail on "+validator) {
      assertResultFailed(validator.validate(request("GET","/path/to/101"),response,chain), 404)
    }
  }

  //
  // validator_UUID allows:
  //
  // The validator is used in the following tests, it uses an external
  // grammar.
  //
  val validator_UUID = Validator((localWADLURI,
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:csapi="http://docs.openstack.org/compute/api/v1.1">
           <grammars>
              <include href="src/test/resources/xsd/common.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="uuid" path="path/to/my/resource/{uuid}">
                   <param name="uuid" style="template" type="csapi:UUID"/>
                   <method href="#getMethod" />
              </resource>
              <resource id="progress" path="path/to/{progress}">
                   <param name="progress" style="template" type="csapi:Progress"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>)
    , assertConfigSaxonEE)

  WADLSchemaAssertions(validator_UUID)


  //
  // validator_UUID_inline allows:
  //
  // The validator is used in the following tests, it uses an inline
  // grammar.
  //
  val validator_UUID_inline = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:csapi="http://docs.openstack.org/compute/api/v1.1">
           <grammars>
              <schema elementFormDefault="qualified"
                      attributeFormDefault="unqualified"
                      xmlns="http://www.w3.org/2001/XMLSchema"
                      xmlns:csapi="http://docs.openstack.org/compute/api/v1.1"
                      xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                      targetNamespace="http://docs.openstack.org/compute/api/v1.1">
              <simpleType name="Progress">
                <annotation>
                   <xsd:documentation
                    xml:lang="EN"
                    xmlns="http://www.w3.org/1999/xhtml">
                    <p>
                      An integer between 0 and 100 that denotes the progress of an
                      operation.
                    </p>
                 </xsd:documentation>
               </annotation>
               <restriction base="xsd:int">
                  <minInclusive value="0"/>
                  <maxInclusive value="100" />
               </restriction>
              </simpleType>
              <simpleType name="UUID">
                 <annotation>
                     <xsd:documentation
                         xml:lang="EN"
                         xmlns="http://www.w3.org/1999/xhtml">
                         <p>
                             A universally unique identifier.
                         </p>
                     </xsd:documentation>
                 </annotation>
                 <restriction base="xsd:string">
                     <length value="36" fixed="true"/>
                     <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
                 </restriction>
              </simpleType>
             </schema>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="uuid" path="path/to/my/resource/{uuid}">
                   <param name="uuid" style="template" type="csapi:UUID"/>
                   <method href="#getMethod" />
              </resource>
              <resource id="progress" path="path/to/{progress}">
                   <param name="progress" style="template" type="csapi:Progress"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
    , assertConfigSaxonEE)

  WADLSchemaAssertions(validator_UUID_inline)

  //
  // validator_UUID_inline2 allows:
  //
  // The validator is used in the following tests, it uses an inline
  // grammar, with all namespaces in the root document
  //
  val validator_UUID_inline2 = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                 xmlns:html="http://www.w3.org/1999/xhtml"
                 xmlns:csapi="http://docs.openstack.org/compute/api/v1.1">
           <grammars>
              <xsd:schema elementFormDefault="qualified"
                      attributeFormDefault="unqualified"
                      targetNamespace="http://docs.openstack.org/compute/api/v1.1">
              <xsd:simpleType name="Progress">
                <xsd:annotation>
                   <xsd:documentation
                    xml:lang="EN">
                    <html:p>
                      An integer between 0 and 100 that denotes the progress of an
                      operation.
                    </html:p>
                 </xsd:documentation>
               </xsd:annotation>
               <xsd:restriction base="xsd:int">
                  <xsd:minInclusive value="0"/>
                  <xsd:maxInclusive value="100" />
               </xsd:restriction>
              </xsd:simpleType>
              <xsd:simpleType name="UUID">
                 <xsd:annotation>
                     <xsd:documentation
                         xml:lang="EN">
                         <html:p>
                             A universally unique identifier.
                         </html:p>
                     </xsd:documentation>
                 </xsd:annotation>
                 <xsd:restriction base="xsd:string">
                     <xsd:length value="36" fixed="true"/>
                     <xsd:pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
                 </xsd:restriction>
              </xsd:simpleType>
             </xsd:schema>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="uuid" path="path/to/my/resource/{uuid}">
                   <param name="uuid" style="template" type="csapi:UUID"/>
                   <method href="#getMethod" />
              </resource>
              <resource id="progress" path="path/to/{progress}">
                   <param name="progress" style="template" type="csapi:Progress"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
    , assertConfigSaxonEE)

  WADLSchemaAssertions(validator_UUID_inline2)


  //
  // validator_XSDContent allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests.
  //
  // The validator is used in the following tests.
  //
  val validator_XSDContent = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
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
    , TestConfig(false, true, true, true))

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContent with valid XML1") {
    validator_XSDContent.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContent with valid XML2") {
    validator_XSDContent.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContent with valid XML1") {
    validator_XSDContent.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContent with valid XML2") {
    validator_XSDContent.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContent with well formed JSON") {
    validator_XSDContent.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContent with well formed JSON") {
    validator_XSDContent.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContent") {
    validator_XSDContent.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContent") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContent") {
    assertResultFailed(validator_XSDContent.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (junk-1)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                               <junk/>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (id-1)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcecz</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (step-1)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>NOT</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-o-1)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>220</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-a-1)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>23</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (junk-2)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="22"
                                                                 junk="true"/>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (id-2)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcecz"
                                                                 stepType="ACCEPT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (step-2)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="NOT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-o-2)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="220"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-a-2)") {
    assertResultFailed(validator_XSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="23"/>
                                                           ),response,chain), 400)
  }

  //
  // validator_XSDContentT allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests. The XML grammar checks should fill in default values.
  //
  // The validator is used in the following tests.
  //
  val validator_XSDContentT = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
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
    , TestConfig(false, true, true, true, false, 1, false, true))

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1") {
    validator_XSDContentT.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML2") {
    validator_XSDContentT.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1, default values should be filled in") {
    val req = request("PUT","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                         <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>)
    validator_XSDContentT.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "stepType").text == "START")
    assert ((dom \ "even").text == "50")
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML2, default values should be filled in") {
    val req = request("PUT","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentT.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1") {
    validator_XSDContentT.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentT with valid XML2") {
    validator_XSDContentT.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContentT with well formed JSON") {
    validator_XSDContentT.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContentT with well formed JSON") {
    validator_XSDContentT.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContentT") {
    validator_XSDContentT.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContentT") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContentT") {
    assertResultFailed(validator_XSDContentT.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (junk-1)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                               <junk/>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (id-1)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcecz</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (step-1)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>NOT</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (even-o-1)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>220</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (even-a-1)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>23</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (junk-2)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="22"
                                                                 junk="true"/>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (id-2)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcecz"
                                                                 stepType="ACCEPT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (step-2)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="NOT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (even-o-2)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="220"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema on validator_XSDContentT (even-a-2)") {
    assertResultFailed(validator_XSDContentT.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="23"/>
                                                           ),response,chain), 400)
  }

  //
  // validator_XSDContentTT allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests. The XML grammar checks should fill in default
  // values. A simple transform is attached to change the
  // stepType:BEGIN to START.
  //
  // The validator is used in the following tests.
  //
  val validator_XSDContentTT = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml">
                          <rax:preprocess href="src/test/resources/xsl/beginStart.xsl"/>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml">
                          <rax:preprocess href="src/test/resources/xsl/beginStart2.xsl"/>
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
    , TestConfig(false, true, true, true, false, 1, false, true, true, "Saxon"))

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML1") {
    validator_XSDContentTT.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML2") {
    validator_XSDContentTT.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML1") {
    validator_XSDContentTT.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML1, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>)
    validator_XSDContentTT.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "stepType").text == "START")
    assert ((dom \ "even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML2, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentTT.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT with valid XML2") {
    validator_XSDContentTT.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContentTT with well formed JSON") {
    validator_XSDContentTT.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContentTT with well formed JSON") {
    validator_XSDContentTT.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContentTT") {
    validator_XSDContentTT.validate(request("GET","/c"),response,chain)
  }


  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT because BEGIN is not an accepted stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>BEGIN</stepType>
                        <even/>
                      </e>)
    validator_XSDContentTT.validate(req,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTT because BEGIN is not an accepted @stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3" stepType="BEGIN"/>)
    validator_XSDContentTT.validate(req,response,chain)
  }


  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTT") {
    assertResultFailed(validator_XSDContentTT.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTT") {
    assertResultFailed(validator_XSDContentTT.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }


  //
  // validator_HeaderUUID allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  // The header X-TEST-UUID must be specified
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
    , TestConfig(false, true, true, true, true, 1, true, true, true, "Saxon", true, true))

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
    , TestConfig(false, true, true, true, true, 1, true, true, true, "Saxon", true, true))

  //
  // Like validator header int, but expects the header only in the put
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
    , TestConfig(false, true, true, true, true, 1, true, true, true, "Saxon", true, true))

  //
  // Like validator header int put, but string header in PUT and POST.
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
    , TestConfig(false, true, true, true, true, 1, true, true, true, "Saxon", true, true))

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1 (multiple headers)") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90","3b14fd08-3294-11e2-bf4b-539af5d76c9f"))),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1 (multiple values in a single header)") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->List("b8870590-e584-11e1-91a3-7f4ba748be90, 3b14fd08-3294-11e2-bf4b-539af5d76c9f"))),response,chain)
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

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header isn't set") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header is mispelled") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUIDs"->List("b8870590-e584-11e1-91a3-7f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID contains malformed data") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID contains malformed data (multiple headers)") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("3b14fd08-3294-11e2-bf4b-539af5d76c9f", "b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID contains malformed data (multiple values in a single header)") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->List("3b14fd08-3294-11e2-bf4b-539af5d76c9f, b8870590e58411e191a37f4ba748be90"))),response,chain), 400)
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

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data (multiple values in a single header)") {
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

  test ("POST on /a/b should failed with well formed XML POST in the wrong location in validator_HeaderIntPut") {
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
