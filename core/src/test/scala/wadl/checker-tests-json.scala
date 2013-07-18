
package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

@RunWith(classOf[JUnitRunner])
class WADLCheckerJsonSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references JSON Schemas into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    //
    //  The following assertions are used to test XSD and ContentError
    //  nodes. They are used in the next couple of tests.
    //

    def jsonAssertions(checker: NodeSeq) : Unit = {
      And("The machine should cantain paths to JSON_SCHEMA types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JSONSchema, Accept)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, JSONSchema, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
    }

    //
    //  The following assertions are used to test ReqType and
    //  ReqTypeFail nodes, they are used in the next couple of tests.
    //
    def reqTypeAssertions(checker : NodeSeq) : Unit = {
      Then("The machine should contain paths to all ReqTypes")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"))
      assert (checker, Start, URL("any"), Method("POST"), AnyReqType)
      assert (checker, Start, URL("text"), Method("POST"), ReqType("(text/)(.*)"))
      assert (checker, Start, URL("v"), Method("POST"), ReqType("(text/plain;charset=UTF8)()"))
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"))
      assert (checker, Start, URL("c"), Method("GET"))
      And("ReqTypeFail states should be after PUT and POST states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqTypeFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
    }

    //
    //  The following assertions are used to test WellFormXML and
    //  ContentError nodes.  They are used in the next couple of tests.
    //
    def wellFormedAssertions(checker : NodeSeq) : Unit = {
      And("The machine should contain paths to WellXML and WELLJSON types")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON)
      And("There should be content failed states")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), ContentFail)
    }

    //
    //  The following count assertions ensure we have the right number
    //  of steps.  They are used in the next couple of steps.
    //
    def countAssertions(checker : NodeSeq) : Unit = {
      And("The following count assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 5")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(.*)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(text/)(.*)']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(text/plain;charset=UTF8)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(.*)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(text/)(.*)']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(text/plain;charset=UTF8)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    }

    def countDupsOnAssertions(checker : NodeSeq) : Unit = {
      And("The following count assertions should also hold:")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 5")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(.*)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(text/)(.*)']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)(text/plain;charset=UTF8)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(.*)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(text/)(.*)']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(text/plain;charset=UTF8)()']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (inline schema)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
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
      val inWADL = (localWADLURI,
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
    </application>)
      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (Well formed not specified)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                              checkJSONGrammar = true, wellFormed = false)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (No Grammar Specified)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 0")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- not used)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to false)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                    >
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request rax:ignoreJSONSchema="false">
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
                  <request rax:ignoreJSONSchema="false">
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to 0)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                    >
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request rax:ignoreJSONSchema="0">
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
                  <request rax:ignoreJSONSchema="0">
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 2")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to true)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                    >
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request rax:ignoreJSONSchema="true">
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 0")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to 1)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                    >
        <grammars>
            <include href="src/test/resources/jsonSchema/test.json"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request rax:ignoreJSONSchema="1">
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
                  <request rax:ignoreJSONSchema="1">
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 0")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to true in representation)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
                      <representation mediaType="application/json" rax:ignoreJSONSchema="true"/>
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
                      <representation mediaType="application/json" rax:ignoreJSONSchema="true"/>
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 0")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- set to true in representation and request)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
                      <representation mediaType="application/json" rax:ignoreJSONSchema="true"/>
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 0")
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with ignore Json schema extension on -- mixed values in representation and requests)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
                      <representation mediaType="application/json" rax:ignoreJSONSchema="false"/>
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = false, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = true,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      countAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 1")
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JSONSchema, Accept)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
      assert (checker, Start, URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, ContentFail)
    }

    scenario("The WADL contains PUT and POST operations accepting JSON which must validate against an JSON Schema (with dups on)") {
      Given ("a WADL that contains multiple PUT and POST operation with JSON that must validate against an JSON Schema")
      val inWADL = (localWADLURI,
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
    </application>)

      When("the wadl is translated")
      val config = TestConfig(removeDups = true, saxoneeValidation = false,
                              checkXSDGrammar = false,  checkElements = false,
                              xpathVersion = 1, checkPlainParams = false,
                              doXSDGrammarTransform = false, enablePreProcessExtension = false,
                              xslEngine = "XalanC", joinXPathChecks = false,
                              checkHeaders = false, enableIgnoreXSDExtension = false,
                              enableMessageExtension= false, enableIgnoreJSONSchemaExtension = false,
                              checkJSONGrammar = true, wellFormed = true)
      val checker = builder.build (inWADL, config)
      reqTypeAssertions(checker)
      wellFormedAssertions(checker)
      jsonAssertions(checker)
      countDupsOnAssertions(checker)
      assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
      assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='JSON_SCHEMA']) = 1")
    }
  }
}
