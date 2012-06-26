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
  // validator_RT allows:
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator is used in the following tests.
  //
  val validator_RT = Validator(
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
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
    </application>
    , assertConfig)

  test ("PUT on /a/b with application/xml should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","application/xml"),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","application/json"),response,chain)
  }

  test ("PUT on /a/b with aPPlicatioN/Xml should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","aPPlication/Xml"),response,chain)
  }

  test ("PUT on /a/b with application/jSON should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","application/jSON"),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_RT") {
    validator_RT.validate(request("POST","/a/b","application/xml"),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_RT") {
    validator_RT.validate(request("POST","/c","application/json"),response,chain)
  }

  test ("GET on /c should succeed on validator_RT") {
    validator_RT.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail on validator_RT if the media type is not specified") {
    assertResultFailed(validator_RT.validate(request("PUT","/a/b"),response,chain), 415)
  }

  test ("POST on /a/b should fail on validator_RT if the media type is not specified") {
    assertResultFailed(validator_RT.validate(request("POST","/a/b"),response,chain), 415)
  }

  test ("POST on /c should fail on validator_RT if the media type is not specified") {
    assertResultFailed(validator_RT.validate(request("POST","/c"),response,chain), 415)
  }

  test ("PUT on /c should fail on validator_RT with a 405") {
    assertResultFailed(validator_RT.validate(request("PUT","/c","application/json"),response,chain), 405)
  }

  test ("GET on /a/b should fail on validator_RT with a 405") {
    assertResultFailed(validator_RT.validate(request("GET","/a/b"),response,chain), 405)
  }

  test ("POST on /a/b should fail on validator_RT if the media type is application/json") {
    assertResultFailed(validator_RT.validate(request("POST","/a/b","application/json"),response,chain), 415)
  }

  test ("POST on /c should fail on validator_RT if the media type is application/xml") {
    assertResultFailed(validator_RT.validate(request("POST","/c","application/xml"),response,chain), 415)
  }


  //
  // validator_WELL allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML
  //
  // The validator is used in the following tests.
  //
  val validator_WELL = Validator(
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
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
    </application>
    , TestConfig(false, true))

  //
  //  Good XML and JSON used in the tests below
  //
  val goodXML = <some_xml att='1' xmlns='test.org'>
                  <an_element>
                    <another_element />
                  </an_element>
                </some_xml>

  val goodJSON = """
       {
           "stuff" : {
             "thing" : true,
             "string" : "A String",
             "array" : [ 1, 2, 3, 4],
             "obj" : {
               "a" : "A",
               "b" : "B"
             },
            "null" : null
           }
       }
  """

  test ("PUT on /a/b with application/xml should succeed on validator_WELL") {
    validator_WELL.validate(request("PUT","/a/b","application/xml", goodXML),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_WELL") {
    validator_WELL.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with aPPlicatioN/Xml should succeed on validator_WELL") {
    validator_WELL.validate(request("PUT","/a/b","aPPlication/Xml", goodXML),response,chain)
  }

  test ("PUT on /a/b with application/jSON should succeed on validator_WELL") {
    validator_WELL.validate(request("PUT","/a/b","application/jSON", goodJSON),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_WELL") {
    validator_WELL.validate(request("POST","/a/b","application/xml", goodXML),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_WELL") {
    validator_WELL.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GET on /c should succeed on validator_WELL") {
    validator_WELL.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail on validator_WELL if the media type is not specified") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b"),response,chain), 415)
  }

  test ("POST on /a/b should fail on validator_WELL if the media type is not specified") {
    assertResultFailed(validator_WELL.validate(request("POST","/a/b"),response,chain), 415)
  }

  test ("POST on /c should fail on validator_WELL if the media type is not specified") {
    assertResultFailed(validator_WELL.validate(request("POST","/c"),response,chain), 415)
  }

  test ("PUT on /c should fail on validator_WELL with a 405") {
    assertResultFailed(validator_WELL.validate(request("PUT","/c","application/json"),response,chain), 405)
  }

  test ("GET on /a/b should fail on validator_WELL with a 405") {
    assertResultFailed(validator_WELL.validate(request("GET","/a/b"),response,chain), 405)
  }

  test ("POST on /a/b should fail on validator_WELL if the media type is application/json") {
    assertResultFailed(validator_WELL.validate(request("POST","/a/b","application/json"),response,chain), 415)
  }

  test ("POST on /c should fail on validator_WELL if the media type is application/xml") {
    assertResultFailed(validator_WELL.validate(request("POST","/c","application/xml"),response,chain), 415)
  }

  test ("PUT on /a/b with valid JSON mislabed as XML should fail with 400") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/xml",
                                                      """
                                                      {
                                                        \"flavor\" : {
                                                          \"id\" : \"52415800-8b69-11e0-9b19-734f1195ff37\",
                                                          \"name\" : \"256 MB Server\"
                                                        }
                                                      }
                                                      """),response,chain), 400)
  }

  test ("PUT on /a/b with valid XML mislabed as JSON should fail with 400") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/json",goodXML),response,chain), 400)
  }

  test ("PUT on /a/b with malformed JSON should fail with 400 (unclosed tag)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/json","""
                                                {
                                                  "stuff" : {
                                                    "thing" : true,
                                                    "string" : "A String",
                                                    "array" : [ 1, 2, 3, 4],
                                                    "obj" : {
                                                      "a" : "A",
                                                      "b" : "B"
                                                    },
                                                   "null" : null
                                                  }
                       """),response,chain), 400)
  }

  test ("PUT on /a/b with malformed JSON should fail with 400 (missing value)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/json","""
                                                {
                                                  "stuff" : {
                                                    "thing" : true,
                                                    "string" : "A String",
                                                    "array" : [ 1, 2, 3, 4],
                                                    "obj" : {
                                                      "a" : "A",
                                                      "b" :
                                                    },
                                                   "null" : null
                                                  }
                                                }
                       """),response,chain), 400)
  }

  test ("PUT on /a/b with malformed JSON should fail with 400 (bad quote)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/json","""
                                                {
                                                  "stuff" : {
                                                    "thing" : true,
                                                    'string' : "A String",
                                                    "array" : [ 1, 2, 3, 4],
                                                    "obj" : {
                                                      "a" : "A",
                                                      "b" : "B"
                                                    },
                                                   "null" : null
                                                  }
                                                }
                       """),response,chain), 400)
  }



  test ("PUT on /a/b with malformed XML should fail on validator_WELL (unclosed tag)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org'>
                                     <an_element>
                                         <another_element>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("POST on /a/b with malformed XML should fail on validator_WELL (unclosed attribute)") {
    assertResultFailed(validator_WELL.validate(request("POST","/a/b","application/xml",
                                   """
                                   <some_xml att='1 xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("POST on /a/b with malformed XML should fail on validator_WELL (bad tag)") {
    assertResultFailed(validator_WELL.validate(request("POST","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                         <another
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("PUT on /a/b with malformed XML should fail on validator_WELL (bad namespace)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/xml",
                                   """
                                   <some_xml att='1' xmlns='test.org' xmlns='test.org'>
                                     <an_element>
                                         <another_element/>
                                     </an_element>
                                   </some_xml>
                                   """
                                 ),response,chain), 400)
  }

  test ("PUT on /a/b with an empty requst should fail on validator_WELL ") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/xml",""),response,chain), 400)
  }

  test ("PUT on /a/b with an empty requst should fail on validator_WELL (spaces)") {
    assertResultFailed(validator_WELL.validate(request("PUT","/a/b","application/xml","    "),response,chain), 400)
  }


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
  val validator_XSDContent = Validator(
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
    </application>
    , TestConfig(false, false, true, true))

  val goodXML_XSD1 = <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                        <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>URL_FAIL</stepType>
                        <even>22</even>
                     </e>
  val goodXML_XSD2 = <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                        stepType="ACCEPT"
                        even="22"/>

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
              <include href="../sample/os-compute/xsd/common.xsd"/>
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
