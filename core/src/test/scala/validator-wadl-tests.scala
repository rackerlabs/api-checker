package com.rackspace.com.papi.components.checker

import java.io.File
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
  // POST /any, should allow *any* media type
  //
  // POST /text, should allow any text media type
  //
  // POST /v, should only allow text/plain;charset=UTF8 exactly, as is...
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
           <resource path="/any">
              <method name="POST">
                 <request>
                    <representation mediaType="*/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/text">
              <method name="POST">
                 <request>
                    <representation mediaType="text/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/v">
              <method name="POST">
                 <request>
                    <representation mediaType="text/plain;charset=UTF8"/>
                 </request>
              </method>
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

  test ("PUT on /a/b with application/json;charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","application/json;charset=UTF8"),response,chain)
  }

  test ("PUT on /a/b with aPPlicatioN/Xml should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","aPPlication/Xml"),response,chain)
  }

  test ("PUT on /a/b with aPPlicatioN/Xml;   charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("PUT","/a/b","aPPlication/Xml;   charset=UTF8"),response,chain)
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

  test ("POST on /any with application/json should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","application/json"),response,chain)
  }

  test ("POST on /any with application/xml should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","application/xml"),response,chain)
  }

  test ("POST on /any with application/xml;charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","application/xml;charset=UTF8"),response,chain)
  }

  test ("POST on /any with application/xml; charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","application/xml; charset=UTF8"),response,chain)
  }

  test ("POST on /any with text/plain should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","text/plain"),response,chain)
  }

  test ("POST on /any with text/foo should succeed on validator_RT") {
    validator_RT.validate(request("POST","/any","text/foo"),response,chain)
  }

  test ("POST on /text with text/foo should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/foo"),response,chain)
  }

  test ("POST on /text with text/plain should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/plain"),response,chain)
  }

  test ("POST on /text with text/plain;charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/plain;charset=UTF8"),response,chain)
  }

  test ("POST on /text with text/plain;charset=UTF8,version=1 should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/plain;charset=UTF8,version=1"),response,chain)
  }

  test ("POST on /text with text/css should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/css"),response,chain)
  }

  test ("POST on /text with text/enriched should succeed on validator_RT") {
    validator_RT.validate(request("POST","/text","text/enriched"),response,chain)
  }

  test ("POST on /v with text/plain;charset=UTF8 should succeed on validator_RT") {
    validator_RT.validate(request("POST","/v","text/plain;charset=UTF8"),response,chain)
  }

  test ("GET on /c should succeed on validator_RT") {
    validator_RT.validate(request("GET","/c"),response,chain)
  }

  test ("POST on /any should fail on validator_RT if mediatype is not specified") {
    assertResultFailed(validator_RT.validate(request("POST","/any"),response,chain), 415)
  }

  test ("POST on /text should fail on validator_RT if mediatype is not specified") {
    assertResultFailed(validator_RT.validate(request("POST","/text"),response,chain), 415)
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

  test ("POST on /text should fail on validator_RT if the media type is application/xml") {
    assertResultFailed(validator_RT.validate(request("POST","/text","application/xml"),response,chain), 415)
  }

  test ("POST on /text should fail on validator_RT if the media type is application/json") {
    assertResultFailed(validator_RT.validate(request("POST","/text","application/json"),response,chain), 415)
  }

  test ("POST on /text should fail on validator_RT if the media type is application/json;charset=UTF8") {
    assertResultFailed(validator_RT.validate(request("POST","/text","application/json;charset=UTF8"),response,chain), 415)
  }

  test ("POST on /text should fail on validator_RT if the media type is video/3gpp") {
    assertResultFailed(validator_RT.validate(request("POST","/text","video/3gpp"),response,chain), 415)
  }

  test ("POST on /v should fail on validator_RT if the media type is text/plain") {
    assertResultFailed(validator_RT.validate(request("POST","/v","text/plain"),response,chain), 415)
  }

  test ("POST on /v should fail on validator_RT if the media type is text/plain;charset=WINDOZE") {
    assertResultFailed(validator_RT.validate(request("POST","/v","text/plain;charsetWINDOZE"),response,chain), 415)
  }

  test ("POST on /v should fail on validator_RT if the media type is application/xml") {
    assertResultFailed(validator_RT.validate(request("POST","/v","application/xml"),response,chain), 415)
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
  val localWADLURI = (new File(System.getProperty("user.dir"),"mywadl.wadl")).toURI.toString
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
    , TestConfig(false, false, true, true, false, 1, false, true))


  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1") {
    validator_XSDContentT.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentT with valid XML2") {
    validator_XSDContentT.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1") {
    validator_XSDContentT.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentT with valid XML1, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
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

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentT with valid XML2, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentT.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
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


  test ("POST on /a/b with application/xml should fail on validator_XSDContentT because BEGIN is not an accepted stepType") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>BEGIN</stepType>
                        <even/>
                      </e>)
    assertResultFailed(validator_XSDContentT.validate(req,response,chain), 400)
  }

  test ("POST on /a/b with application/xml should sfail on validator_XSDContentT because BEGIN is not an accepted @stepType") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3" stepType="BEGIN"/>)
    assertResultFailed(validator_XSDContentT.validate(req,response,chain), 400)
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
  // validator_XSDElementContent allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
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
  val validator_XSDElementContent = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
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
    , TestConfig(false, false, true, true, true))

  //
  //  Like validator_XSDElementContent, but using an XPath 2 engine.
  //
  val validator_XSDElementContent2 = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
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
    , TestConfig(false, false, true, true, true, 2))

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContent with valid XML1") {
    validator_XSDElementContent.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContent2 with valid XML1") {
    validator_XSDElementContent2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContent with valid XML1") {
    validator_XSDElementContent.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContent2 with valid XML1") {
    validator_XSDElementContent2.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContent with well formed JSON") {
    validator_XSDElementContent.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContent2 with well formed JSON") {
    validator_XSDElementContent2.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContent with well formed JSON") {
    validator_XSDElementContent.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContent2 with well formed JSON") {
    validator_XSDElementContent2.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContent") {
    validator_XSDElementContent.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContent2") {
    validator_XSDElementContent2.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContent") {
    assertResultFailed(validator_XSDElementContent.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContent2") {
    assertResultFailed(validator_XSDElementContent2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContent") {
    assertResultFailed(validator_XSDElementContent.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContent2") {
    assertResultFailed(validator_XSDElementContent2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContent") {
    assertResultFailed(validator_XSDElementContent.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContent2") {
    assertResultFailed(validator_XSDElementContent2.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContent") {
    assertResultFailed(validator_XSDElementContent.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContent2") {
    assertResultFailed(validator_XSDElementContent2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

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
    , TestConfig(false, false, true, true, true, 1, true))

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
    , TestConfig(false, false, true, true, true, 1, true, false , false, "XalanC", true))

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
    , TestConfig(false, false, true, true, true, 2, true))


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
    , TestConfig(false, false, true, true, true, 2, true, false , false, "XalanC", true))


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

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlain2 with valid XML1") {
    validator_XSDElementContentPlain2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain with valid XML1") {
    validator_XSDElementContentPlain.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlain2 with valid XML1") {
    validator_XSDElementContentPlain2.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain with well formed JSON") {
    validator_XSDElementContentPlain.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlain2 with well formed JSON") {
    validator_XSDElementContentPlain2.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain with well formed JSON") {
    validator_XSDElementContentPlain.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlain2 with well formed JSON") {
    validator_XSDElementContentPlain2.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlain") {
    validator_XSDElementContentPlain.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlain2") {
    validator_XSDElementContentPlain2.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain") {
    assertResultFailed(validator_XSDElementContentPlain.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
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

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlain2") {
    assertResultFailed(validator_XSDElementContentPlain2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            ),response,chain), 400)
  }

  // :-)

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt with valid XML1") {
    validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt2 with valid XML1") {
    validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt with valid XML1") {
    validator_XSDElementContentPlainOpt.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDElementContentPlainOpt2 with valid XML1") {
    validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt with well formed JSON") {
    validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDElementContentPlainOpt2 with well formed JSON") {
    validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt with well formed JSON") {
    validator_XSDElementContentPlainOpt.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDElementContentPlainOpt2 with well formed JSON") {
    validator_XSDElementContentPlainOpt2.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt") {
    validator_XSDElementContentPlainOpt.validate(request("GET","/c"),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDElementContentPlainOpt2") {
    validator_XSDElementContentPlainOpt2.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT with missing required plain params on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", badXML_Plain2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b", "application/xml", goodXML_XSD2),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST with missing required plain params on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("POST","/a/b", "application/xml", badXML_Plain1),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt") {
    assertResultFailed(validator_XSDElementContentPlainOpt.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
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

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_XSDElementContentPlainOpt2") {
    assertResultFailed(validator_XSDElementContentPlainOpt2.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test" stepType="foo">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
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
    , TestConfig(false, false, true, true, false, 1, false, true, true, "XalanC"))

  //
  //  Like validator_XSDContentTT except it uses embeded XSLs
  //

  val validator_XSDContentTTE = Validator((localWADLURI,
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
                          <rax:preprocess>
                              <xsl:stylesheet
                                  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                  xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                                  xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                  version="1.0">

                                    <xsl:template match="node() | @*">
                                        <xsl:copy>
                                          <xsl:apply-templates select="@* | node()"/>
                                        </xsl:copy>
                                    </xsl:template>

                                    <xsl:template match="tst:stepType">
                                      <xsl:choose>
                                        <xsl:when test=". = 'BEGIN'">
                                          <stepType>START</stepType>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <stepType><xsl:value-of select="."/></stepType>
                                        </xsl:otherwise>
                                      </xsl:choose>
                                    </xsl:template>

                                    <xsl:template match="@stepType">
                                        <xsl:choose>
                                          <xsl:when test=". = 'BEGIN'">
                                            <xsl:attribute name="stepType">START</xsl:attribute>
                                          </xsl:when>
                                          <xsl:otherwise>
                                            <xsl:attribute name="stepType"><xsl:value-of select="."/></xsl:attribute>
                                          </xsl:otherwise>
                                      </xsl:choose>
                                    </xsl:template>

                                </xsl:stylesheet>
                          </rax:preprocess>
                      </representation>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml">
                          <rax:preprocess>
                              <xsl:stylesheet
                                  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                  xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
                                  xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                  version="2.0" exclude-result-prefixes="tst">

                                  <xsl:template match="node() | @*">
                                    <xsl:copy>
                                        <xsl:apply-templates select="@* | node()"/>
                                    </xsl:copy>
                                  </xsl:template>

                                  <xsl:template match="tst:stepType">
                                    <xsl:choose>
                                      <xsl:when test=". = 'BEGIN'">
                                          <stepType>START</stepType>
                                      </xsl:when>
                                    <xsl:otherwise>
                                        <stepType><xsl:value-of select="."/></stepType>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </xsl:template>

                                <xsl:template match="@stepType">
                                  <xsl:choose>
                                    <xsl:when test=". = 'BEGIN'">
                                        <xsl:attribute name="stepType">START</xsl:attribute>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="stepType"><xsl:value-of select="."/></xsl:attribute>
                                    </xsl:otherwise>
                                  </xsl:choose>
                                </xsl:template>

                              </xsl:stylesheet>
                          </rax:preprocess>
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
    , TestConfig(false, false, true, true, false, 1, false, true, true, "XalanC"))

  //
  //  Like validator_XSDContentTT except uses Xalan instead of XalanC for XSL 1.0 engine.
  //
  val validator_XSDContentTTX = Validator((localWADLURI,
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
    , TestConfig(false, false, true, true, false, 1, false, true, true, "Xalan"))

  //
  //  Like validator_XSDContentTT except uses Saxon instead of XalanC for XSL 1.0 engine
  //
  val validator_XSDContentTTS = Validator((localWADLURI,
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
    , TestConfig(false, false, true, true, false, 1, false, true, true, "Saxon"))


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

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML1") {
    validator_XSDContentTTE.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML2") {
    validator_XSDContentTTE.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML1") {
    validator_XSDContentTTE.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML1, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>)
    validator_XSDContentTTE.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "stepType").text == "START")
    assert ((dom \ "even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML2, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentTTE.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE with valid XML2") {
    validator_XSDContentTTE.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContentTTE with well formed JSON") {
    validator_XSDContentTTE.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContentTTE with well formed JSON") {
    validator_XSDContentTTE.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContentTTE") {
    validator_XSDContentTTE.validate(request("GET","/c"),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE because BEGIN is not an accepted stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>BEGIN</stepType>
                        <even/>
                      </e>)
    validator_XSDContentTTE.validate(req,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTE because BEGIN is not an accepted @stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3" stepType="BEGIN"/>)
    validator_XSDContentTTE.validate(req,response,chain)
  }


  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTE") {
    assertResultFailed(validator_XSDContentTTE.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTE") {
    assertResultFailed(validator_XSDContentTTE.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML1") {
    validator_XSDContentTTX.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML2") {
    validator_XSDContentTTX.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML1") {
    validator_XSDContentTTX.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML1, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>)
    validator_XSDContentTTX.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "stepType").text == "START")
    assert ((dom \ "even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML2, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentTTX.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX with valid XML2") {
    validator_XSDContentTTX.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContentTTX with well formed JSON") {
    validator_XSDContentTTX.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContentTTX with well formed JSON") {
    validator_XSDContentTTX.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContentTTX") {
    validator_XSDContentTTX.validate(request("GET","/c"),response,chain)
  }


  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX because BEGIN is not an accepted stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>BEGIN</stepType>
                        <even/>
                      </e>)
    validator_XSDContentTTX.validate(req,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTX because BEGIN is not an accepted @stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3" stepType="BEGIN"/>)
    validator_XSDContentTTX.validate(req,response,chain)
  }


  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTX") {
    assertResultFailed(validator_XSDContentTTX.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTX") {
    assertResultFailed(validator_XSDContentTTX.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }


  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML1") {
    validator_XSDContentTTS.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML2") {
    validator_XSDContentTTS.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML1") {
    validator_XSDContentTTS.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML1, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>)
    validator_XSDContentTTS.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "stepType").text == "START")
    assert ((dom \ "even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML2, default values should be filled in") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"/>)
    validator_XSDContentTTS.validate(req,response,chain)
    val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
    assert ((dom \ "@stepType").text == "START")
    assert ((dom \ "@even").text == "50")
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS with valid XML2") {
    validator_XSDContentTTS.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_XSDContentTTS with well formed JSON") {
    validator_XSDContentTTS.validate(request("PUT","/a/b","application/json", goodJSON),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_XSDContentTTS with well formed JSON") {
    validator_XSDContentTTS.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_XSDContentTTS") {
    validator_XSDContentTTS.validate(request("GET","/c"),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS because BEGIN is not an accepted stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                      <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>BEGIN</stepType>
                        <even/>
                      </e>)
    validator_XSDContentTTS.validate(req,response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_XSDContentTTS because BEGIN is not an accepted @stepType, but it's converted to START") {
    val req = request("POST","/a/b","application/xml",
                      <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3" stepType="BEGIN"/>)
    validator_XSDContentTTS.validate(req,response,chain)
  }


  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTS") {
    assertResultFailed(validator_XSDContentTTS.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_XSDContentTTS") {
    assertResultFailed(validator_XSDContentTTS.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
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
    validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->"foo")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header with valid XML1") {
    validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->"foo")),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_Header with well formed JSON") {
    validator_Header.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST"->"foo")),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_Header with well formed JSON") {
    validator_Header.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST"->"foo")),response,chain)
  }

  test ("GOT on /c should succeed on validator_Header") {
    validator_Header.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header isn't set") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST header is mispelled") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TESTs"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header  if the X-TEST header isn't set") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_Header  if the X-TEST header is mispelled") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TESTs"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_Header") {
    assertResultFailed(validator_Header.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_Header") {
    assertResultFailed(validator_Header.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID with valid XML1") {
    validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderUUID with well formed JSON") {
    validator_HeaderUUID.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderUUID with well formed JSON") {
    validator_HeaderUUID.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderUUID") {
    validator_HeaderUUID.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID header isn't set") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID header is mispelled") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUIDs"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-UUID contains malformed data") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->"b8870590e58411e191a37f4ba748be90")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header isn't set") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID header is mispelled") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUIDs"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderUUID  if the X-TEST-UUID contains malformed data") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->"b8870590e58411e191a37f4ba748be90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderUUID") {
    assertResultFailed(validator_HeaderUUID.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-UUID"->"b8870590-e584-11e1-91a3-7f4ba748be90")),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt with valid XML1") {
    validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderInt with well formed JSON") {
    validator_HeaderInt.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderInt with well formed JSON") {
    validator_HeaderInt.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderInt") {
    validator_HeaderInt.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT header isn't set") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT header is mispelled") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->"90")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderInt  if the X-TEST-INT contains malformed data") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderInt") {
    assertResultFailed(validator_HeaderInt.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPut with valid XML1") {
    validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut with valid XML1") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderIntPut with well formed JSON") {
    validator_HeaderIntPut.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderIntPut with well formed JSON") {
    validator_HeaderIntPut.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-INT"->"90")),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderIntPut") {
    validator_HeaderIntPut.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT header isn't set") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false),response, chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT header is mispelled") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->"90")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPut  if the X-TEST-INT contains malformed data") {
    validator_HeaderIntPut.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"foo")),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML POST in the wrong location in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderIntPut") {
    assertResultFailed(validator_HeaderIntPut.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_HeaderIntPutMix with valid XML1") {
    validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix with valid XML1") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->"foo")),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_HeaderIntPutMix with well formed JSON") {
    validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/json", goodJSON, false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_HeaderIntPutMix with well formed JSON") {
    validator_HeaderIntPutMix.validate(request("POST","/c","application/json", goodJSON, false, Map("X-TEST-foo"->"foo", "X-TEST"->"foo")),response,chain)
  }

  test ("GOT on /c should succeed on validator_HeaderIntPutMix") {
    validator_HeaderIntPutMix.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b with application/xml should fail on validator_HeaderIntPutMix if X-TEST is not set") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header isn't set in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT header is mispelled in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INTs"->"90", "X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML PUT, if the X-TEST-INT contains malformed data in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b","application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"foo", "X-TEST"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT header isn't set") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST"->"foo")),response, chain)
  }

  test ("POST on /a/b with application/xml should fail on validator_HeaderIntPutMix  if the X-TEST iheader isn't set") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"50")),response, chain), 400)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT header is mispelled") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INTs"->"90", "X-TEST"->"foo")),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_HeaderIntPutMix  if the X-TEST-INT contains malformed data") {
    validator_HeaderIntPutMix.validate(request("POST","/a/b","application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"foo", "X-TEST"->"foo")),response,chain)
  }

  test ("PUT on /a/b should fail with well formed XML PUT in the wrong location in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML_XSD1, false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain), 400)
  }

  test ("POST on /a/b should failed with well formed XML POST in the wrong location in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("POST","/a/b", "application/xml", goodXML_XSD2, false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml", goodXML, false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML, correct element, butdoes not validate against the schema in validator_HeaderIntPutMix") {
    assertResultFailed(validator_HeaderIntPutMix.validate(request("PUT","/a/b", "application/xml",
                                                             <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                                <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                                <stepType>URL_FAIL</stepType>
                                                                <even>22</even>
                                                              </a>
                                                            , false, Map("X-TEST-INT"->"90", "X-TEST"->"foo")),response,chain), 400)
  }
}
