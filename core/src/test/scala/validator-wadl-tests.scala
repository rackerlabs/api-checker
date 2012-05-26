package com.rackspace.com.papi.components.checker

import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.rackspace.cloud.api.wadl.Converters._

@RunWith(classOf[JUnitRunner])
class ValidatorWADLSuite extends BaseValidatorSuite {
  //
  // validator_EMPTY does not allow ANY HTTP requests. The validator
  // is used in the following tests.
  //
  val validator_EMPTY = Validator(
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource/>
           </resources>
        </application>, assertConfig)

  test ("GET on / should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/"),response,chain), 405)
  }

  test ("an empty GET should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET",""),response,chain), 405)
  }

  test ("GET on /a should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a"),response,chain), 404)
  }

  test ("POST on /a/b/c/hello/there should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a/b/c/hello/there"),response,chain), 404)
  }

  test ("a completely empty request should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("",""),response,chain), 405)
  }

  //
  // validator_AB allows a GET on /a/b. The validator is used in the
  // following tests.
  //
  val validator_AB = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/b">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>
    , assertConfig)

  test ("GET on /a/b should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b"),response,chain)
  }

  test ("GET on /a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b/"),response,chain)
  }

  test ("GET on a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","a/b/"),response,chain)
  }

  test ("GET on / should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/"),response,chain), 405)
  }

  test ("an empty GET should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET",""),response,chain), 405)
  }

  test ("GET on /a should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a"),response,chain), 405)
  }

  test ("GET on /a/b/c/d should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a/b/c/d"),response,chain), 404)
  }

  test ("POST on /a/b should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("POST","/a/b"),response,chain), 405)
  }

  test ("GET on /index.html should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/index.html"),response,chain), 404)
  }


  //
  // validator_ABAC allows a GET on /a/b and /a/c. The validator is used in the
  // following tests.
  //
  val validator_ABAC = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/b">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
              <resource path="/a/c">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>
    , assertConfig)

  test ("GET on /a/b should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","/a/b"),response,chain)
  }

  test ("GET on /a/b/ should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","/a/b/"),response,chain)
  }

  test ("GET on a/b/ should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","a/b/"),response,chain)
  }

  test ("GET on /a/c should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","/a/c"),response,chain)
  }

  test ("GET on /a/c/ should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","/a/c/"),response,chain)
  }

  test ("GET on a/c/ should succeed on validator_ABAC") {
    validator_ABAC.validate(request("GET","a/c/"),response,chain)
  }

  test ("GET on /a/d should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("GET","/a/d"),response,chain), 404)
  }

  test ("GET on /a/b/c/d should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("GET","/a/b/c/d"),response,chain), 404)
  }

  test ("POST on /a/b should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("POST","/a/b"),response,chain), 405)
  }

  test ("GET on /index.html should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("GET","/index.html"),response,chain), 404)
  }

  test ("GET on /a/c/c/d should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("GET","/a/c/c/d"),response,chain), 404)
  }

  test ("POST on /a/c should fail on validator_ABAC") {
    assertResultFailed(validator_ABAC.validate(request("POST","/a/c"),response,chain), 405)
  }

  //
  // validator_REG allows a GET on /a/.*/c. That is, the 2nd URI
  // component can be anything. The validator is used in the
  // following tests.
  //
  val validator_REG = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="a/{b}/c">
                   <param name="b" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
    </application>
    , assertConfig)

  test ("GET on /a/7/c should succeed on validator_REG") {
    validator_REG.validate(request("GET","/a/7/c"),response,chain)
  }

  test ("GET on /a/-7/c should succeed on validator_REG") {
    validator_REG.validate(request("GET","/a/-7/c"),response,chain)
  }

  test ("GET on /a/<randomLong>/c should succeed on validator_REG") {
    val rl = new Random(new Date().getTime()).nextLong()
    validator_REG.validate(request("GET","/a/"+rl+"/c"),response,chain)
  }

  test ("GET on /a/<bigInt>/c should succeed on validator_REG") {
    val bi = new BigInteger(1024, new Random(new Date().getTime()).self)
    validator_REG.validate(request("GET","/a/"+bi+"/c"),response,chain)
  }

  test ("GET on /a/<randomDouble>/c should succeed validator_REG") {
    val rf = new Random(new Date().getTime()).nextDouble()
    validator_REG.validate(request("GET","/a/"+rf+"/c"),response,chain)
  }

  test ("GET on /a/<uuid>/c should succeed validator_REG") {
    val uuid = UUID.randomUUID().toString()
    validator_REG.validate(request("GET","/a/"+uuid+"/c"),response,chain)
  }

  test ("GET on /a/<katakana>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/c"),response,chain)
  }

  test ("GET on /a/<arrows>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/c"),response,chain)
  }

  test ("GET on /a/<snowman>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E2%98%83/c"),response,chain)
  }

  test ("GET on /a/b/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/b/c"),response,chain)
  }

  test ("GET on /a/ 7/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/+7/c"),response,chain)
  }

  test ("GET on /a/+7/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%2B7/c"),response,chain)
  }

  test ("GET on /a/    /c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/++++/c"),response,chain)
  }

  test ("GET on /a/  hi  /c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/++hi++/c"),response,chain)
  }

  test ("GET on /a//c should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a//c"),response,chain), 405)
  }

  test ("GET on /a should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a"),response,chain), 405)
  }

  test ("GET on /a/b/d should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a/b/d"),response,chain), 404)
  }

  //
  // validator_AM allows:
  //
  // GET /.*/b
  // PUT /a/b
  //
  // The of course means that a GET on /a/b is allowed.
  //
  // The validator is used in the following tests.
  //
  val validator_AM = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="{any}/b">
                   <param name="any" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
              <resource path="a/b">
                   <method href="#putMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <method id="putMethod" name="PUT">
               <response status="200"/>
           </method>
    </application>
    , assertConfig)

  test ("GET on /a/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/a/b"),response,chain)
  }

  test ("PUT on /a/b should succeed on validator_AM") {
    validator_AM.validate(request("PUT","/a/b"),response,chain)
  }

  test ("GET on /b/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/b/b"),response,chain)
  }

  test ("GET on /<katakana>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/b"),response,chain)
  }

  test ("GET on /<arrows>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/b"),response,chain)
  }

  test ("POST on /a/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("POST","/a/b"),response,chain), 405)
  }

  test ("DELETE on /z/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("DELETE","/z/b"),response,chain), 405)
  }

  test ("GET on /a/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/a/c"),response,chain), 404)
  }

  test ("GET on /z/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/z/c"),response,chain), 404)
  }

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
      assertResultFailed(validator.validate(request("DELETE","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f"),response,chain), 405)
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
  val validator_UUID = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:csapi="http://docs.openstack.org/compute/api/v1.1">
           <grammars>
              <include href="https://raw.github.com/openstack/compute-api/master/openstack-compute-api-2/src/xsd/common.xsd"/>
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
    , assertConfig)

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
    , assertConfig)

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
    , assertConfig)

  WADLSchemaAssertions(validator_UUID_inline2)

}
