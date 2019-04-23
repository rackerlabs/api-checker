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
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorWADLPrioritySuite extends BaseValidatorSuite {

  val configs1 = Map[String, Config]("Config without ops " -> TestConfig(false, false, true, true, true, 2, true, false, false, "SaxonHE", false),
                                    "Config with removeDups" -> TestConfig(true, false, true, true, true, 2, true, false, false, "SaxonHE", false),
                                    "Config with joinXPathChecks" -> TestConfig(false, false, true, true, true, 2, true, false, false, "SaxonHE", true),
                                    "Config with removeDups and joinXPathChecks" -> TestConfig(true, false, true, true, true, 2, true, false, false, "SaxonHE", true))

  for ((description, config) <- configs1) {

    //
    // validator_XSDElementContentManyPlain allows:
    //
    //
    // POST /a/b with xml support
    // POST /a/c with xml support
    //
    //
    // The validator checks for wellformness in XML and grammar checks
    // XSD requests.  It also checks the element type, and it checks
    // constraints against required plain params.  One path contains
    // many plain parameters, another does not. We want to make sure
    // that when things fail, the XSD errors win out, thus adhereing to
    // the the proper error priorites.
    //
    // The validator is used in the following tests.
    //

    val validator_XSDElementContentManyPlain = Validator((localWADLURI,
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
           <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml">
                          <param style="plain" path="true()" required="true"/>
                          <param style="plain" path="not(false())" required="true"/>
                          <param style="plain" path="tst:a/@stepType" required="true"/>
                          <param style="plain" path="tst:a" required="true"/>
                      </representation>
                      <representation mediaType="application/xml" />
                  </request>
               </method>
           </resource>
        </resources>
    </application>)
    , config)

    test ("POST on /a/c with application/xml should succeed on validator validator_XSDElementContentManyPlain with good XML "+description){
      validator_XSDElementContentManyPlain.validate(request("POST","/a/c","application/xml", goodXML_XSD2),response,chain)
    }

    test ("POST on /a/c with application/xml should succeed on validator validator_XSDElementContentManyPlain with good XML 2 "+description){
      validator_XSDElementContentManyPlain.validate(request("POST","/a/c","application/xml", goodXML_XSD1),response,chain)
    }

    test("POST on /a/c with application/xml should fail with XSD error if badXML is provided, should fail because of XSD check  "+description) {
      assertResultFailed(validator_XSDElementContentManyPlain.validate(request("POST","/a/c","application/xml", <bad />),response,chain), 400,
                         List("declaration","'bad'"))
    }
  }

  val configs2 = Map[String, Config]("Config without ops " -> TestConfig(false, false, true, true, true, 2, true, false, false, "SaxonHE", false,
                                                                         false, false, false, false, false, true, false, true),
                                     "Config with removeDups" -> TestConfig(true, false, true, true, true, 2, true, false, false, "SaxonHE", false,
                                                                           false, false, false, false, false, true, false, true))

  for ((description, config) <- configs2) {

    // validator_getRoles allows:
    //
    // GET    /a for roles a:observer and a:admin
    // POST   /a for role a:admin
    // DELETE /a for role a:admin
    //
    // This raxrole-mask validiator is testing to make sure prioritis
    // are properly set for method errors.  If the role is a:observer
    // on a PUT the allow header should be GET. If the role is a:admin
    // in combination with any other role (including a:observer) then
    // the allow header should be DELETE, GET, and POST.
    //

    val validator_getRoles = Validator((localWADLURI,
     <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="http://localhost:${targetPort}">
        <resource path="/a">
            <method name="GET" rax:roles="a:observer a:admin"/>
            <method name="POST" rax:roles="a:admin"/>
            <method name="DELETE" rax:roles="a:admin"/>
        </resource>
      </resources>
    </application>)
    , config)

    test ("GET on /a should succeed with role a:observer and a:admin "+description) {
      validator_getRoles.validate(request("GET", "/a", "application/xml", "<foo />", false, Map("X-ROLES" ->List("a:observer", "a:admin"))), response, chain)
    }

    test ("PUT on /a with role of a:admin should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:admin"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }

    test ("PUT on /a with role of a:observer should fail with 405 and Allow = GET "+ description) {
      assertResultFailed(validator_getRoles.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:observer"))),
                                                     response, chain), 405, Map("Allow"->"GET"))
    }

    test ("PUT on /a with role of a:observer and a:admin should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:observer", "a:admin"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }

    test ("PUT on /a with role of a:admin and a:observer  should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:admin", "a:observer"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }
  }

  for ((description, config) <- configs2) {

    //
    // validator_getRoles2 is like validatior_getRoles above but
    // defines methods in a slightly different order.
    //
    val validator_getRoles2 = Validator((localWADLURI,
     <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
      <resources base="http://localhost:${targetPort}">
        <resource path="/a">
            <method name="POST" rax:roles="a:admin"/>
            <method name="DELETE" rax:roles="a:admin"/>
            <method name="GET" rax:roles="a:observer a:admin"/>
        </resource>
      </resources>
    </application>)
    , config)

    test ("GET on /a in roles 2, should succeed with role a:observer and a:admin "+description) {
      validator_getRoles2.validate(request("GET", "/a", "application/xml", "<foo />", false, Map("X-ROLES" ->List("a:observer", "a:admin"))), response, chain)
    }

    test ("PUT on /a in roles 2, with role of a:admin should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles2.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:admin"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }

    test ("PUT on /a in roles 2, with role of a:observer should fail with 405 and Allow = GET "+ description) {
      assertResultFailed(validator_getRoles2.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:observer"))),
                                                     response, chain), 405, Map("Allow"->"GET"))
    }

    test ("PUT on /a in roles 2, with role of a:observer and a:admin should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles2.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:observer", "a:admin"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }

    test ("PUT on /a in roles 2, with role of a:admin and a:observer  should fail with 405 and Allow = DELETE, GET, POST "+ description) {
      assertResultFailed(validator_getRoles2.validate(request("PUT", "/a", "application/xml", "<foo />", false, Map("X-ROLES"->List("a:admin", "a:observer"))),
                                                     response, chain), 405, Map("Allow"->"DELETE, GET, POST"))
    }
  }

}
