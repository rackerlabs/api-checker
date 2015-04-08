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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.cloud.api.wadl.Converters._
import Converters._

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes.PARSED_XML

import org.w3c.dom.Document

@RunWith(classOf[JUnitRunner])
class ValidatorWADLPreserveBodySuite extends BaseValidatorSuite {
  //
  // validator_PreserveBody allows:
  //
  //
  // PUT /a/b and xml support
  //
  // The validator checks for wellformness in XML and grammar checks
  // XSD requests.  It also checks the element type, and it checks
  // constraints against required plain params. In all cases, the body
  // should be reusable.
  //
  // The validator is used in the following tests.
  //
  val validator_PreserveBody = Validator((localWADLURI,
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
                  </request>
               </method>
           </resource>
        </resources>
    </application>)
    , TestConfig(removeDups = false, saxoneeValidation = false, wellFormed = true,
                 checkXSDGrammar = true, checkElements = true, xpathVersion = 1,
                 checkPlainParams = true, doXSDGrammarTransform = true,
                 enablePreProcessExtension = false, xslEngine = "XalanC",
                 joinXPathChecks = true, checkHeaders = false,
                 enableIgnoreXSDExtension = false, enableMessageExtension = false,
                 checkJSONGrammar = false, enableIgnoreJSONSchemaExtension = false,
                 enableRaxRolesExtension = false, preserveRequestBody = true))

  test ("PUT on /a/b with application/xml should succeed on validator_PreserveBody with valid XML1") {
    val req = request("PUT","/a/b","application/xml", goodXML_XSD2)
    validator_PreserveBody.validate(req,response,chain)
    assert (req.getAttribute(PARSED_XML) != null)
  }

  test ("PUT on /a/b with wrong element should fail, but request body (as XML) should still be available on validator_PreserveBody") {
    val req = request("PUT","/a/b", "application/xml", <foo />, false)
    assertResultFailed(validator_PreserveBody.validate(req,response,chain), 400, List("root", "element", "tst:a"))
    assert (req.getAttribute(PARSED_XML) != null)
  }

  test ("PUT on /a/b with bad plain param should fail, but request body (as XML) should still be available on validator_PreserveBody") {
    val req = request("PUT","/a/b", "application/xml", <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"/>, false)
    assertResultFailed(validator_PreserveBody.validate(req,response,chain), 400, List("tst:a/@stepType"))
    assert (req.getAttribute(PARSED_XML) != null)
  }

}
