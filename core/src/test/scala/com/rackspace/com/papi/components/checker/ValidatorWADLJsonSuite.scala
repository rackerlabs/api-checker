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
class ValidatorWADLJsonSuite extends BaseValidatorSuite {

  //
  // validator_JSONContent allows:
  //
  //
  // PUT /a/b with json and xml support
  // POST /a/b with xml support
  //
  // POST /c with json support
  // GET /c
  //
  // The validator checks for wellformness in XML and JSON and grammar checks
  // on JSON requests.
  //
  // The validator is used in the following tests.
  //
  val validator_JSONContent = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
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
    , TestConfig(removeDups = false, saxoneeValidation = false,
                 checkXSDGrammar = false,  checkElements = false,
                 xpathVersion = 1, checkPlainParams = false,
                 doXSDGrammarTransform = false, enablePreProcessExtension = false,
                 xslEngine = "XalanC", joinXPathChecks = false,
                 checkHeaders = false, enableIgnoreXSDExtension = false,
                 enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                 checkJSONGrammar = true, wellFormed = true))

  test ("PUT on /a/b with application/xml should succeed on validator_JSONContent with well formed XML") {
    validator_JSONContent.validate(request("PUT","/a/b","application/xml", goodXML),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_JSONContent with well formed XML") {
    validator_JSONContent.validate(request("POST","/a/b","application/xml", goodXML),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContent with valid JSON1") {
    validator_JSONContent.validate(request("PUT","/a/b","application/json", goodJSON_Schema1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContent with valid JSON2") {
    validator_JSONContent.validate(request("PUT","/a/b","application/json", goodJSON_Schema2),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContent with valid JSON1") {
    validator_JSONContent.validate(request("POST","/c","application/json", goodJSON_Schema1),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContent with valid JSON2") {
    validator_JSONContent.validate(request("POST","/c","application/json", goodJSON_Schema2),response,chain)
  }

  test ("GOT on /c should succeed on validator_JSONContent") {
    validator_JSONContent.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed JSON that does not match schema on validator_JSONContent") {
    assertResultFailed(validator_JSONContent.validate(request("PUT","/a/b", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not match schema on validator_JSONContent") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema (junk-1)") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json",
                                                              """
                                                                 {
                                                                   "junt" : true
                                                                 }
                                                              """
                                                           ),response,chain), 400, List("missing", "firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema (bad-type-1)") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : "Kraft",
                                                                 "age" : true
                                                              }
                                                              """
                                                           ),response,chain), 400, List("age","integer","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema (bad-type-2)") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : 7,
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("firstName","string","integer"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema (bad-type-3)") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : false,
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("lastName","string","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema (missing)") {
    assertResultFailed(validator_JSONContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("missing","firstName"))
  }


  //
  // Like JSONContent, but the JSON Schema is embedded.
  //
  val grammar = """
    {
        "title": "Example Schema",
        "type": "object",
        "properties": {
            "firstName": {
			       "type": "string"
		      },
		      "lastName": {
			       "type": "string"
		      },
		      "age": {
			       "description": "Age in years",
			       "type": "integer",
			       "minimum": 0
		      }
	     },
	     "required": ["firstName", "lastName"]
    }

      """
  val validator_JSONContentE = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:json="http://json-schema.org/schema#">
        <grammars>
          <json:schema>
             { grammar }
          </json:schema>
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
    , TestConfig(removeDups = false, saxoneeValidation = false,
                 checkXSDGrammar = false,  checkElements = false,
                 xpathVersion = 1, checkPlainParams = false,
                 doXSDGrammarTransform = false, enablePreProcessExtension = false,
                 xslEngine = "XalanC", joinXPathChecks = false,
                 checkHeaders = false, enableIgnoreXSDExtension = false,
                 enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                 checkJSONGrammar = true, wellFormed = true))

  test ("PUT on /a/b with application/xml should succeed on validator_JSONContentE with well formed XML") {
    validator_JSONContentE.validate(request("PUT","/a/b","application/xml", goodXML),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_JSONContentE with well formed XML") {
    validator_JSONContentE.validate(request("POST","/a/b","application/xml", goodXML),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContentE with valid JSON1") {
    validator_JSONContentE.validate(request("PUT","/a/b","application/json", goodJSON_Schema1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContentE with valid JSON2") {
    validator_JSONContentE.validate(request("PUT","/a/b","application/json", goodJSON_Schema2),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContentE with valid JSON1") {
    validator_JSONContentE.validate(request("POST","/c","application/json", goodJSON_Schema1),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContentE with valid JSON2") {
    validator_JSONContentE.validate(request("POST","/c","application/json", goodJSON_Schema2),response,chain)
  }

  test ("GOT on /c should succeed on validator_JSONContentE") {
    validator_JSONContentE.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed JSON that does not match schema on validator_JSONContentE") {
    assertResultFailed(validator_JSONContentE.validate(request("PUT","/a/b", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not match schema on validator_JSONContentE") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONContentE (junk-1)") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json",
                                                              """
                                                                 {
                                                                   "junt" : true
                                                                 }
                                                              """
                                                           ),response,chain), 400, List("missing", "firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONContentE (bad-type-1)") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : "Kraft",
                                                                 "age" : true
                                                              }
                                                              """
                                                           ),response,chain), 400, List("age","integer","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONContentE (bad-type-2)") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : 7,
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("firstName","string","integer"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONContentE (bad-type-3)") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : false,
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("lastName","string","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONContentE (missing)") {
    assertResultFailed(validator_JSONContentE.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("missing","firstName"))
  }

  //
  // Like validator_JSONContent but does not validate JSON content on POST /c
  //
  //
  val validator_JSONContentI = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:rax="http://docs.rackspace.com/api"
                    >
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
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
                  <request rax:ignoreJSONSchema="true">
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>)
    , TestConfig(removeDups = false, saxoneeValidation = false,
                 checkXSDGrammar = false,  checkElements = false,
                 xpathVersion = 1, checkPlainParams = false,
                 doXSDGrammarTransform = false, enablePreProcessExtension = false,
                 xslEngine = "XalanC", joinXPathChecks = false,
                 checkHeaders = false, enableIgnoreXSDExtension = false,
                 enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                 checkJSONGrammar = true, wellFormed = true))

  test ("PUT on /a/b with application/xml should succeed on validator_JSONContentI with well formed XML") {
    validator_JSONContentI.validate(request("PUT","/a/b","application/xml", goodXML),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_JSONContentI with well formed XML") {
    validator_JSONContentI.validate(request("POST","/a/b","application/xml", goodXML),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContentI with valid JSON1") {
    validator_JSONContentI.validate(request("PUT","/a/b","application/json", goodJSON_Schema1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONContentI with valid JSON2") {
    validator_JSONContentI.validate(request("PUT","/a/b","application/json", goodJSON_Schema2),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContentI with valid JSON1") {
    validator_JSONContentI.validate(request("POST","/c","application/json", goodJSON_Schema1),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContentI with valid JSON2") {
    validator_JSONContentI.validate(request("POST","/c","application/json", goodJSON_Schema2),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONContentI with well formed JSON content") {
    validator_JSONContentI.validate(request("POST","/c","application/json", goodJSON),response,chain)
  }

  test ("GOT on /c should succeed on validator_JSONContentI") {
    validator_JSONContentI.validate(request("GET","/c"),response,chain)
  }

  test ("POST on /c should succeed with well formed JSON that does not validate against the schema (junk-1)") {
    validator_JSONContentI.validate(request("POST","/c", "application/json",
                                            """
                                            {
                                              "junt" : true
                                            }
                                            """), response, chain)
  }

  test ("PUT on /a/b should fail with well formed JSON that does not match schema on validator_JSONContentI") {
    assertResultFailed(validator_JSONContentI.validate(request("PUT","/a/b", "application/json", goodJSON),response,chain), 400, List("missing", "firstName", "lastName"))
  }

  //
  // Like validator_JSONContent, but also performes XSD checks.
  //
  val validator_JSONXSDContent = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
           <include href="src/test/resources/jsonSchema/test.json"/>
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
    , TestConfig(removeDups = false, saxoneeValidation = false,
                 checkXSDGrammar = true,  checkElements = false,
                 xpathVersion = 1, checkPlainParams = false,
                 doXSDGrammarTransform = false, enablePreProcessExtension = false,
                 xslEngine = "XalanC", joinXPathChecks = false,
                 checkHeaders = false, enableIgnoreXSDExtension = false,
                 enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                 checkJSONGrammar = true, wellFormed = true))

  test ("PUT on /a/b with application/xml should succeed on validator_JSONXSDContent with valid XML1") {
    validator_JSONXSDContent.validate(request("PUT","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("PUT on /a/b with application/xml should succeed on validator_JSONXSDContent with valid XML2") {
    validator_JSONXSDContent.validate(request("PUT","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_JSONXSDContent with valid XML1") {
    validator_JSONXSDContent.validate(request("POST","/a/b","application/xml", goodXML_XSD1),response,chain)
  }

  test ("POST on /a/b with application/xml should succeed on validator_JSONXSDContent with valid XML2") {
    validator_JSONXSDContent.validate(request("POST","/a/b","application/xml", goodXML_XSD2),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONXSDContent with valid JSON1") {
    validator_JSONXSDContent.validate(request("PUT","/a/b","application/json", goodJSON_Schema1),response,chain)
  }

  test ("PUT on /a/b with application/json should succeed on validator_JSONXSDContent with valid JSON2") {
    validator_JSONXSDContent.validate(request("PUT","/a/b","application/json", goodJSON_Schema2),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONXSDContent with valid JSON1") {
    validator_JSONXSDContent.validate(request("POST","/c","application/json", goodJSON_Schema1),response,chain)
  }

  test ("POST on /c with application/json should succeed on validator_JSONXSDContent with valid JSON2") {
    validator_JSONXSDContent.validate(request("POST","/c","application/json", goodJSON_Schema2),response,chain)
  }

  test ("GOT on /c should succeed on validator_JSONXSDContent") {
    validator_JSONXSDContent.validate(request("GET","/c"),response,chain)
  }

  test ("PUT on /a/b should fail with well formed JSON that does not match schema on validator_JSONXSDContent") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not match schema on validator_JSONXSDContent") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json", goodJSON),response,chain), 400,
                     List("missing","firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONXSDContent (junk-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json",
                                                              """
                                                                 {
                                                                   "junt" : true
                                                                 }
                                                              """
                                                           ),response,chain), 400, List("missing", "firstName", "lastName"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONXSDContent (bad-type-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : "Kraft",
                                                                 "age" : true
                                                              }
                                                              """
                                                           ),response,chain), 400, List("age","integer","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONXSDContent (bad-type-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : 7,
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("firstName","string","integer"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONXSDContent (bad-type-3)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                "firstName" : "Rachel",
                                                                 "lastName" : false,
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("lastName","string","boolean"))
  }

  test ("POST on /c should fail with well formed JSON that does not validate against the schema on validator_JSONXSDContent (missing)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/c", "application/json",
                                                              """
                                                              {
                                                                 "lastName" : "Kraft",
                                                                 "age" : 32
                                                              }
                                                              """
                                                           ),response,chain), 400, List("missing","firstName"))
  }

  test ("PUT on /a/b should fail with well formed XML that does not match schema on validator_JSONXSDContent") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml", goodXML),response,chain), 400)
  }

  test ("POST on /a/b should fail with well formed XML that does not match schema on validator_JSONXSDContent") {
    assertResultFailed(validator_JSONXSDContent.validate(request("POST","/a/b", "application/xml", goodXML),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (junk-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                               <junk/>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (id-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcecz</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (step-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>NOT</stepType>
                                                               <even>22</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-o-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>220</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-a-1)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                             <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                                                               <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                                                               <stepType>URL_FAIL</stepType>
                                                               <even>23</even>
                                                             </e>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (junk-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="22"
                                                                 junk="true"/>
                                                           ),response,chain), 400)
  }

  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (id-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcecz"
                                                                 stepType="ACCEPT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (step-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="NOT"
                                                                 even="22"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-o-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="220"/>
                                                           ),response,chain), 400)
  }


  test ("PUT on /a/b should fail with well formed XML that does not validate against the schema (even-a-2)") {
    assertResultFailed(validator_JSONXSDContent.validate(request("PUT","/a/b", "application/xml",
                                                               <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                                                                 id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                                                                 stepType="ACCEPT"
                                                                 even="23"/>
                                                           ),response,chain), 400)
  }

}
