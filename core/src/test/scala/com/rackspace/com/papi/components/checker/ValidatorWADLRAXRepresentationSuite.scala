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


import com.rackspace.com.papi.components.checker.RunAssertionsHandler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

import scala.xml.Elem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRAXRepresentationSuite extends BaseValidatorSuite {

  //
  //  Configs...
  //
  val raxRepDisabled = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = false
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = false
    tc.checkElements = false
    tc.checkXSDGrammar = false
    tc.checkJSONGrammar = false
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabled = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = false
    tc.checkElements = false
    tc.checkXSDGrammar = false
    tc.checkJSONGrammar = false
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.joinXPathChecks = false
    tc.checkPlainParams = false
    tc.checkElements = false
    tc.checkXSDGrammar = false
    tc.checkJSONGrammar = false
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledPlain = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = false
    tc.checkJSONGrammar = false
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledPlainRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.joinXPathChecks = true
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = false
    tc.checkJSONGrammar = false
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledPlainGrammar = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledPlainGrammarRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.joinXPathChecks = true
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = false
    tc.maskRaxRoles403 = false
    tc
  }

  //
  //  Configs, rax roles enabled
  //
  val raxRepEnabledPlainGrammarRaxRoles = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = true
    tc.maskRaxRoles403 = false
    tc
  }

  val raxRepEnabledPlainGrammarRaxRolesRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.joinXPathChecks = true
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = true
    tc.maskRaxRoles403 = false
    tc
  }

  //
  //  Configs, rax roles mask enbadled
  //
  val raxRepEnabledPlainGrammarRaxRolesMask = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.joinXPathChecks = false
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = true
    tc.maskRaxRoles403 = true
    tc
  }

  val raxRepEnabledPlainGrammarRaxRolesMaskRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.joinXPathChecks = true
    tc.checkPlainParams = true
    tc.checkElements = true
    tc.checkXSDGrammar = true
    tc.checkJSONGrammar = true
    tc.enableRaxRolesExtension = true
    tc.maskRaxRoles403 = true
    tc
  }

  val jsonSchema = """
  {
    "title" : "Test Schemas",
    "type" : "object",
    "oneOf" : [
        {
            "title" : "firstName and XML",
            "type" : "object",
            "properties" : {
                "firstName" : {
                    "type" : "string"
                },
                "xml" : {
                    "type" : "string"
                }
            },
            "required" : ["firstName", "xml"],
            "additionalProperties" : false
        },
        {
            "title" : "first and last",
            "type" : "object",
            "properties" : {
                "firstName" : {
                    "type" : "string"
                },
                "lastName" : {
                    "type" : "string"
                }
            },
            "required" : ["firstName", "lastName"],
            "additionalProperties" : false
        }
    ]
  }
  """

  val testWADL =
  <application xmlns="http://wadl.dev.java.net/2009/02"
               xmlns:rax="http://docs.rackspace.com/api"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:json="http://json-schema.org/schema#"
               xmlns:tst="test.org">
      <grammars>
          <schema elementFormDefault="qualified"
                  attributeFormDefault="unqualified"
                  xmlns="http://www.w3.org/2001/XMLSchema"
                  targetNamespace="test.org">

              <element name="user" type="tst:User"/>
              <element name="user2" type="tst:User2"/>
              <element name="some_xml" type="tst:SomeXML"/>
              <element name="other_xml" type="tst:OtherXML"/>

              <complexType name="User">
                  <attribute name="firstName" type="xsd:string" use="required"/>
                  <attribute name="lastName" type="xsd:string" use="required"/>
              </complexType>

              <complexType name="User2">
                  <attribute name="fn" type="xsd:string" use="required"/>
                  <attribute name="ln" type="xsd:string" use="required"/>
              </complexType>

              <complexType name="SomeXML">
                  <choice minOccurs="0">
                      <element name="json"   type="xsd:string" />
                      <element name="json2"  type="xsd:string" />
                  </choice>
              </complexType>

              <complexType name="OtherXML">
                  <sequence>
                      <element name="xml" type="xsd:string" minOccurs="0"/>
                  </sequence>
              </complexType>
          </schema>
          <schema elementFormDefault="qualified"
                  attributeFormDefault="unqualified"
                  xmlns="http://www.w3.org/2001/XMLSchema"
                  xmlns:tst2="test.org/2"
                  targetNamespace="test.org/2">

              <element name="user" type="tst2:User"/>

              <complexType name="User">
                  <sequence>
                      <element name="json" type="xsd:string"/>
                  </sequence>
              </complexType>
          </schema>
          <json:schema>
              { jsonSchema }
          </json:schema>
      </grammars>
      <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="admin">
              <method name="PUT" rax:roles="allowPUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:some_xml">
                          <rax:representation
                              mediaType="application/myApp+json"
                              path="/tst:some_xml/tst:json"
                              name="jsonContent">
                              <param name="test" style="plain"
                                     path="$_?firstName" required="true"
                                     rax:message="Need a first name" rax:code="403"/>
                          </rax:representation>
                          <rax:representation
                              mediaType="application/myApp+json"
                              path="/tst:some_xml/tst:json2"
                              name="jsonContent2">
                              <param name="test" style="plain"
                                     path="$_?firstName" required="true"
                                     rax:message="Need a first name" rax:code="403"/>
                          </rax:representation>
                      </representation>
                      <representation mediaType="application/json">
                          <param name="test" style="plain"
                                 path="$_?firstName" required="true"
                                 rax:message="Need a first name" rax:code="403"/>
                          <rax:representation
                              mediaType="application/myApp+xml"
                              path="$body('xml')"
                              name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                              <rax:representation
                                  mediaType="application/myApp+json"
                                  path="/tst2:user/tst2:json">
                                  <param name="test3" style="plain"
                                         path="$_?lastName" required="true"
                                         rax:message="Need a last name" rax:code="403"/>
                                  />
                              </rax:representation>
                          </rax:representation>
                      </representation>
                  </request>
              </method>
              <method name="POST" rax:roles="allowPOST">
                  <request>
                      <representation mediaType="application/xml" element="tst:some_xml"/>
                      <representation mediaType="application/json" />
                      <representation mediaType="text/yaml"/>
                      <rax:representation mediaType="application/myApp+json" name="jsonUser"
                                          path="req:header('X-JSON-USER')">
                          <param name="test3" style="plain"
                                 path="$_?firstName" required="true"
                                 rax:message="Need a first name"
                                 rax:code="403"/>
                          <param name="test4" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                      </rax:representation>
                  </request>
              </method>
              <resource path="b" rax:roles="#all">
                  <method name="PUT">
                      <request>
                          <representation mediaType="application/xml"
                                          element="tst:other_xml">
                              <rax:representation mediaType="application/MyApp+xml" name="xmlUser"
                                                  path="/tst:other_xml/tst:xml" element="tst:user"
                                                  applyToChildren="true">
                                  <param name="test5" style="plain"
                                         path="tst:user/@firstName" required="true"
                                         rax:message="Need a first name"
                                         rax:code="403"/>
                                  <param name="test6" style="plain"
                                         path="tst:user/@lastName" required="true"
                                         rax:message="Need a last name"
                                         rax:code="403"/>
                              </rax:representation>
                          </representation>
                          <representation mediaType="application/json" />
                          <representation mediaType="text/yaml"/>
                      </request>
                  </method>
              </resource>
              <rax:representation mediaType="application/MyApp+xml" name="xmlUser"
                                  path="req:header('X-XML-USER')" element="tst:user"
                                  applyToChildren="true">
                  <param name="test5" style="plain"
                         path="tst:user/@firstName" required="true"
                         rax:message="Need a first name"
                         rax:code="403"/>
                  <param name="test6" style="plain"
                         path="tst:user/@lastName" required="true"
                         rax:message="Need a last name"
                         rax:code="403"/>
              </rax:representation>
          </resource>
          <rax:representation mediaType="application/myApp+xml" name="xmlUser"
                              path="req:header('X-XML-USER2')" element="tst:user2">
              <param name="test5" style="plain"
                     path="tst:user2/@fn" required="true"
                     rax:message="Need a first name"
                     rax:code="403"/>
              <param name="test6" style="plain"
                     path="tst:user2/@ln" required="true"
                     rax:message="Need a last name"
                     rax:code="403"/>
          </rax:representation>
      </resources>
  </application>

  //
  // Config combinations
  //
  val repDisabledConfigs = Map[String, Config](
    "Config with rax:rep disabled" -> raxRepDisabled)

  val repEnabledConfigs = Map[String, Config](
    "Config with rax:rep enabled"->raxRepEnabled,
    "Config with rax:rep enabled and remove dups"->raxRepEnabledRemoveDups)

  val repEnabledPlainConfigs = Map[String, Config](
    "Config with rax:rep and plain params enabled"->raxRepEnabledPlain,
    "Config with rax:rep and plain params enabled and remove dups"->raxRepEnabledPlainRemoveDups)

  val repEnabledPlainGrammarConfigs = Map[String, Config](
    "Config with rax:rep and plain params and grammar enabled"->raxRepEnabledPlainGrammar,
    "Config with rax:rep and plain params and grammar enabled and remove dups"->raxRepEnabledPlainGrammarRemoveDups)

  val repEnabledPlainGrammarRolesConfigs = Map[String, Config](
    "Config with rax:rep and plain params and grammar and rax:roles enabled"->raxRepEnabledPlainGrammarRaxRoles,
    "Config with rax:rep and plain params and grammar and rax:roles enabled and remove dups"->raxRepEnabledPlainGrammarRaxRolesRemoveDups)

  val repEnabledPlainGrammarRolesMaskConfigs = Map[String, Config](
    "Config with rax:rep and plain params and grammar and rax:roles mask enabled"->raxRepEnabledPlainGrammarRaxRolesMask,
    "Config with rax:rep and plain params and grammar and rax:roles mask enabled and remove dups"->raxRepEnabledPlainGrammarRaxRolesMaskRemoveDups)


  val repWADLs = Map[String, Elem](
    "WADL with rax:representations"->testWADL)

  //
  //  Good Samples, these should validate if submited to the right
  //  resource.
  //
  val goodRepJSON1 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>"
   }

  """

  val goodRepJSON2 =
    """
   {
       "firstName" : "Jorge",
       "lastName" : "Williams"
   }

  """

  val goodRepXML1 =
   <some_xml xmlns="test.org">
       <json> {
         goodRepJSON2
      }</json>
    </some_xml>

  val goodRepXML2 =
   <some_xml xmlns="test.org">
       <json2>{
         goodRepJSON2
       }</json2>
  </some_xml>

  val goodRepXML3 =
   <user xmlns="test.org" firstName="Jorge" lastName="Williams"/>

  val goodRepXML4 =
    <user2 xmlns="test.org" fn="Jorge" ln="Williams"/>

  val goodRepXML5 =
    <some_xml xmlns="test.org"/>

  val goodRepXML6 =
    <other_xml xmlns="test.org"/>

  val goodRepXML7 =
    <other_xml xmlns="test.org">
      <xml>
        &lt;user xmlns="test.org" firstName="Jorge" lastName="Williams"/&gt;
      </xml>
    </other_xml>

  val goodRepYAML1 =
    """
    ---
    firstName: Jorge
    lastName: Williams

    """

  //
  //  Bad samples, these should not validate, unless rax:rep is
  //  disabled.
  //

  val badRepJSON1 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;lastName&quot; ; &quot;Williams&quot;, &quot;firstName&quot; | &quot;Jorge&quot; }</json></user>"
   }

  """

  val badRepJSON2 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json><user>"
   }

  """


  val badRepJSON3 =
    """
   {
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>"
   }

  """

  val badRepJSON4 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>"
   }

  """

  val badRepJSON5 =
    """
   {
       "firstName" : "Jorge",
       "lastName"  : "Williams",
       "wooga"     : false
   }
  """

  val badRepJSON6 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>",
       "wooga" : true
   }

  """

  val badRepJSON7 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2' wooga='true'><json>{ &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>"
   }

  """

  val badRepJSON8 =
    """
   {
       "firstName" : "Jorge",
       "xml" : "<user xmlns='test.org/2'><json>{ &quot;item&quot; : false, &quot;lastName&quot; : &quot;Williams&quot;, &quot;firstName&quot; : &quot;Jorge&quot; }</json></user>"
   }

  """

  val badRepJSON9 =
    """
   {
       "firstName" : "Jorge",
       "lastName" : "Williams",
       "items" : [false, false, true]
   }

  """



  val badRepXML1 =
   <some_xml xmlns="test.org">
       <json> /booga\  </json>
  </some_xml>

  val badRepXML2 =
   <some_xml xmlns="test.org">
       <json> 42 </json>
    </some_xml>

  val badRepXML3 =
   <user xmlns="test.org" firstN="Jorge" lastName="Williams"/>

  val badRepXML4 =
    <user2 xmlns="test.org" fn="Jorge" lastN="Williams"/>

  val badRepXML5 =
   <some_xml xmlns="test.org" wooga="true">
       <json> {
         goodRepJSON2
      }</json>
    </some_xml>

  val badRepXML6 =
   <some_xml xmlns="test.org">
       <json> {badRepJSON5} </json>
  </some_xml>

  val badRepXML7 =
    <user2 xmlns="test.org" fn="Jorge" ln="Williams" firstName="Jorge"/>


  val badRepXML8 =
    <other_xml xmlns="test.org">
      <xml>
        (user :  firstName="Jorge" : lastName="Williams")
      </xml>
    </other_xml>

  val badRepXML9 =
    <other_xml xmlns="test.org">
      <xml>
        &lt;user xmlns="test.org" firstName="Jorge" last="Williams"/&gt;
      </xml>
    </other_xml>

  val badRepXML10 =
    <other_xml xmlns="test.org">
      <xml>
        &lt;user xmlns="test.org" firstName="Jorge" lastName="Williams" why="yes"/&gt;
      </xml>
    </other_xml>



  //
  //  Assertions!
  //
  def happyPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with good XML 1 $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 2 $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 3 if it is specified in the X-XML-USER header $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 3 if it is specified in the X-XML-USER header (json Rep) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header (json Rep) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with good JSON 1 $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A POST on /a should succeed with good XML 3 if it is specified in the X-XML-USER header $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("admin"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain)
    }


    test(s"A POST on /a should succeed with good XML 3 if it is specified in the X-XML-USER header (json) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header (json) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header (json) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain)
    }

    test(s"A POST on /a should succeed with good XML 3 if it is specified in the X-XML-USER header (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("admin"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 7 $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML7, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain)
    }


    test(s"A PUT on /a/b should succeed with good XML 3 if it is specified in the X-XML-USER header $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 4 if it is specified in the X-XML-USER2 header $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 3 if it is specified in the X-XML-USER header (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 4 if it is specified in the X-XML-USER2 header (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 3 if it is specified in the X-XML-USER header (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with good XML 4 if it is specified in the X-XML-USER2 header (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }
  }

  //
  //  Some sanity tests, these assertions should always fail
  //  regardless of configuration
  //

  def happySadPaths (validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"Plain text PUT should fail on /a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT","/a","plain/text","hello!", false,
        Map[String,List[String]]("X-Roles"->List("admin"))), response, chain),
        415, List("content type","application/xml","application/json"))
    }

    test (s"Plain text POST should fail on /a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST","/a","plain/text","hello!", false,
        Map[String,List[String]]("X-Roles"->List("allowPOST"))), response, chain),
        415, List("content type","application/xml","application/json","text/yaml"))
    }

    test (s"Plain text PUT should fail on /a/b $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT","/a/b","plain/text","hello!", false,
        Map[String,List[String]]("X-Roles"->List("user"))), response, chain),
        415, List("content type","application/xml","application/json","text/yaml"))
    }

    test (s"A PATCH on /a should fail with a 405 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PATCH", "/a", "application/xml",goodXML, false,
        Map[String,List[String]]("a"->List("abba"),
          "b"->List("ababa"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("admin"))), response, chain),
        405, List("Method", "POST", "PUT"))
    }

    test (s"A PATCH on /a/b should fail with a 405 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PATCH", "/a/b", "application/xml",goodXML, false,
        Map[String,List[String]]("a"->List("abba"),
          "b"->List("ababa"),
          "X-Auth"->List("foo!"),
          "X-Roles"->List("admin"))), response, chain),
        405, List("Method", "PUT"))
    }
  }


  //
  //  These assertions will succeed only if rax:rep is disabled.
  //
  def happyWhenRaxRepIsDisabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with bad XML 1 if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", badRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad XML in  X-XML-USER header if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER"->List("!This is not XML!"))),response, chain)
    }


    test(s"A PUT on /a should succeed with bad XML in  X-XML-USER2 header if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER2"->List("!This is not XML!"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad if it is specified in the X-XML-USER if rax:rep is disabled (json Rep) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-XML-USER"->List("!This is not XML!"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 1 if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 2 if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A POST on /a should succeed  if bad json is specified in the X-JSON_USER header if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List("!This is not JSON!"))),response, chain)
    }

    test(s"A PUT on /a/b should succeed if bad XML is specified in the X-XML-USER header if rax:rep is disabled (yaml) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER"->List("!This is not XML!"))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with bad embedded XML if rax:rep is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", badRepXML8, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain)
    }

  }

  //
  //  These assertions will fail if rax:rep is enabled
  //
  def sadWhenRaxRepIsEnabled (validator : Validator, wadlDesc : String, configDesc : String) {

    test(s"A PUT on /a should fail with bad XML 1 if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", badRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content"))
    }

    test(s"A PUT on /a should fail with bad XML in  X-XML-USER header if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
          "X-XML-USER"->List("!This is not XML!"),
          "X-XML-USER2"->List("!This is not XML!"))),response, chain),
        400, List("Bad Content", "not allowed in prolog"))
    }

    test(s"A PUT on /a should fail with bad if it is specified in the X-XML-USER if rax:rep is enabled (json Rep) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", goodRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
          "X-XML-USER2"->List("!This is not XML!"))),response, chain),
        400, List("Bad Content", "not allowed in prolog"))
    }

    test(s"A PUT on /a should fail with bad JSON 1 if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content", "unexpected character"))
    }

    test(s"A PUT on /a should fail with bad JSON 2 if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON2, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content"))
    }

    test(s"A POST on /a should fail  if bad json is specified in the X-JSON_USER header if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
          "X-XML-USER"->List("!This is not XML!"),
          "X-XML-USER2"->List("!This is not XML!"),
          "X-JSON-USER"->List("!This is not JSON!"))),response, chain),
        400, List("Bad Content"))
    }

    test(s"A PUT on /a/b should fail if bad XML is specified in the X-XML-USER header if rax:rep is enabled (yaml) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
          "X-XML-USER"->List("!This is not XML!"),
          "X-XML-USER2"->List("!This is not XML!"),
          "X-JSON-USER"->List("!This is not JSON!"))),response, chain),
        400, List("Bad Content"))
    }

    test(s"A PUT on /a/b should fail with bad embedded XML if rax:rep is enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", badRepXML8, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain),
        400, List("Bad Content"))
    }
  }

  //
  //  These checks should succeed when plain params are disabled
  //
  def happyWhenPlainParamsAreDisabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with other_xml if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"),
          "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad params in embedded json if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", badRepXML2, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 3 if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON3, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 4 if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON4, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A POST on /a should succeed with bad JSON 2 if it is specified in the X-JSON_USER header if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List(badRepJSON1))),response, chain)
    }


    test(s"A PUT on /a/b should succeed with bad XML 3 in X-XML-USER header (yaml) if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER"->List(badRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with bad XML 4 in the X-XML-USER2 header if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
                                 "X-XML-USER2"->List(badRepXML4.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with bad embedded XML if plain params are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", badRepXML9, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain)
    }
  }


  //
  //  These checks should fail when plain params are enabled
  //
  def sadWhenPlainParamsAreEnabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should fail with other_xml if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"),
          "X-XML-USER"->List(goodRepXML3.toString))),response, chain),
        400, List("Expecting", "some_xml"))
    }

    test(s"A PUT on /a should fail with bad params in embedded json if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", badRepXML2, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        403, List("Need a first name"))
    }

    test(s"A PUT on /a should fail with bad JSON 3 if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON3, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        403, List("Need a first name"))
    }

    test(s"A PUT on /a should fail with bad JSON 4 if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON4, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        403, List("Need a last name"))
    }

    test(s"A POST on /a should fail with bad JSON 2 if it is specified in the X-JSON_USER header if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
          "X-JSON-USER"->List(badRepJSON1))),response, chain),
        403, List("Need a last name"))
    }


    test(s"A PUT on /a/b should fail with bad XML 3 in X-XML-USER header (yaml) if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "text/yaml", goodRepYAML1, false,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
          "X-XML-USER"->List(badRepXML3.toString))),response, chain),
        403, List("Need a first name"))
    }

    test(s"A PUT on /a/b should fail with bad XML 4 in the X-XML-USER2 header if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
          "X-XML-USER2"->List(badRepXML4.toString))),response, chain),
        403, List("Need a last name"))
    }

    test(s"A PUT on /a/b should fail with bad embedded XML if plain params are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", badRepXML9, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain),
        403, List("Need a last name"))
    }
  }

  //
  //  These checks should succeed when grammar checks are disabled.
  //
  def happyWhenGrammarChecksAreDisabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with bad XML 5 when grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", badRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad XML 6 when grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", badRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }


    test(s"A PUT on /a should succeed with bad JSON 6 when grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 7 when grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON7, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A PUT on /a should succeed with bad JSON 8 when grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/json", badRepJSON8, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain)
    }

    test(s"A POST on /a should succeed with bad JSON 9 in the X-JSON_USER when grammar checkes are disabled header $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-JSON-USER"->List(badRepJSON9))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with bad XML 7  in the X-XML-USER2 header whet grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
          "X-XML-USER2"->List(badRepXML7.toString))),response, chain)
    }

    test(s"A PUT on /a/b should succeed with bad embedded XML grammar checks are disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/xml", badRepXML10, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain)
    }

  }


  //
  //  These checks should fail when grammar checks are enabled
  //
  def sadWhenGrammarChecksAreEnabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should fail with bad XML 5 when grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", badRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content", "wooga", "not allowed"))
    }

    test(s"A PUT on /a should fail with bad XML 6 when grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", badRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content", "failed to match","schema"))
    }


    test(s"A PUT on /a should fail with bad JSON 6 when grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON6, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content", "failed to match","schema"))
    }

    test(s"A PUT on /a should fail with bad JSON 7 when grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON7, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content","wooga", "not allowed"))
    }

    test(s"A PUT on /a should fail with bad JSON 8 when grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/json", badRepJSON8, true,
        Map[String,List[String]]("X-ROLES"->List("admin"))),response, chain),
        400, List("Bad Content", "failed to match","schema"))
    }

    test(s"A POST on /a should fail with bad JSON 9 in the X-JSON_USER when grammar checkes are enabled header $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
          "X-JSON-USER"->List(badRepJSON9))),response, chain),
        400, List("Bad Content", "failed to match","schema"))
    }

    test(s"A PUT on /a/b should fail with bad XML 7  in the X-XML-USER2 header whet grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", goodRepXML6, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"),
          "X-XML-USER2"->List(badRepXML7.toString))),response, chain),
        400, List("Bad Content","firstName", "not allowed"))
    }

    test(s"A PUT on /a/b should fail with bad embedded XML grammar checks are enabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/xml", badRepXML10, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain),
        400, List("Bad Content","why", "not allowed"))
    }

  }


  //
  //  These checks should succeed when raxroles is disabled
  //
  def happyWhenRaxRolesAreDisabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with good XML 1 with role wooga should if rax:roles is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 1 with roles allowPOST/DELETE if rax:roles is disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST", "allowDELETE"))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 3 if it is specified in the X-XML-USER header with role allowPOST with rax:roles disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain)
    }

    test(s"A PUT on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header with role booga and rax:roles disabled $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("booga"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain)
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header with role allowPUT and rax:roles disabled $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain)
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header (json) with roles bugga / allowPUT with rax:roles disabled $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("bugga", "allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain)
    }
  }

  //
  //  These checks should fail when raxroles is enabled
  //
  def sadWhenRaxRolesAreEnabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with good XML 1 with role wooga should if rax:roles is disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A PUT on /a should succeed with good XML 1 with roles allowPOST/DELETE if rax:roles is disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST", "allowDELETE"))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A PUT on /a should succeed with good XML 3 if it is specified in the X-XML-USER header with role allowPOST with rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A PUT on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header with role booga and rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("booga"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header with role allowPUT and rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header (json) with roles bugga / allowPUT with rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("bugga", "allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain),
        403, List("You are forbidden to perform the operation"))
    }
  }

  //
  //  These checks should fail when raxroles mask is enabled
  //
  def sadWhenRaxRolesMaskAreEnabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /a should succeed with good XML 1 with role wooga should if rax:roles is disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("wooga"))),response, chain),
        405, List("Bad Method","PUT"))
    }

    test(s"A PUT on /a should succeed with good XML 1 with roles allowPOST/DELETE if rax:roles is disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML1, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST", "allowDELETE"))),response, chain),
        405, List("Bad Method","PUT","does not match","POST"))
    }

    test(s"A PUT on /a should succeed with good XML 3 if it is specified in the X-XML-USER header with role allowPOST with rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPOST"),
                                 "X-XML-USER"->List(goodRepXML3.toString))),response, chain),
        405, List("Bad Method","PUT","does not match","POST"))
    }

    test(s"A PUT on /a should succeed with good XML 4 if it is specified in the X-XML-USER2 header with role booga and rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("booga"),
                                 "X-XML-USER2"->List(goodRepXML4.toString))),response, chain),
        405, List("Bad Method","PUT"))
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header with role allowPUT and rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/xml", goodRepXML5, true,
        Map[String,List[String]]("X-ROLES"->List("allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain),
        405, List("Bad Method","PUT","does not match","POST"))
    }

    test(s"A POST on /a should succeed with good JSON 2 if it is specified in the X-JSON_USER header (json) with roles bugga / allowPUT with rax:roles disabled $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a", "application/json", goodRepJSON1, true,
        Map[String,List[String]]("X-ROLES"->List("bugga", "allowPUT"),
                                 "X-JSON-USER"->List(goodRepJSON2))),response, chain),
        405, List("Bad Method","PUT","does not match","POST"))
    }
  }

  //
  //  With assertions disabled
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repDisabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      happyWhenRaxRepIsDisabled(validator, wadlDesc, configDesc)
      happyWhenPlainParamsAreDisabled(validator, wadlDesc, configDesc)
      happyWhenGrammarChecksAreDisabled(validator, wadlDesc, configDesc)
      happyWhenRaxRolesAreDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With rax:rep enabled
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repEnabledConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      sadWhenRaxRepIsEnabled(validator, wadlDesc, configDesc)
      happyWhenPlainParamsAreDisabled(validator, wadlDesc, configDesc)
      happyWhenGrammarChecksAreDisabled(validator, wadlDesc, configDesc)
      happyWhenRaxRolesAreDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With rax:rep, plain enabled
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repEnabledPlainConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      sadWhenRaxRepIsEnabled(validator, wadlDesc, configDesc)
      sadWhenPlainParamsAreEnabled(validator, wadlDesc, configDesc)
      happyWhenGrammarChecksAreDisabled(validator, wadlDesc, configDesc)
      happyWhenRaxRolesAreDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With rax:rep, plain, grammars enabled
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repEnabledPlainGrammarConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      sadWhenRaxRepIsEnabled(validator, wadlDesc, configDesc)
      sadWhenPlainParamsAreEnabled(validator, wadlDesc, configDesc)
      sadWhenGrammarChecksAreEnabled(validator, wadlDesc, configDesc)
      happyWhenRaxRolesAreDisabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With rax:rep, plain, grammars, rax:roles enabled
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repEnabledPlainGrammarRolesConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      sadWhenRaxRepIsEnabled(validator, wadlDesc, configDesc)
      sadWhenPlainParamsAreEnabled(validator, wadlDesc, configDesc)
      sadWhenGrammarChecksAreEnabled(validator, wadlDesc, configDesc)
      sadWhenRaxRolesAreEnabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  With rax:rep, plain enabled, grammars, rax:roles mask
  //
  for ((wadlDesc, wadl) <- repWADLs) {
    for ((configDesc, config) <- repEnabledPlainGrammarRolesMaskConfigs) {
      val validator = Validator(wadl, config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPaths(validator, wadlDesc, configDesc)
      sadWhenRaxRepIsEnabled(validator, wadlDesc, configDesc)
      sadWhenPlainParamsAreEnabled(validator, wadlDesc, configDesc)
      sadWhenGrammarChecksAreEnabled(validator, wadlDesc, configDesc)
      sadWhenRaxRolesMaskAreEnabled(validator, wadlDesc, configDesc)
    }
  }

}
