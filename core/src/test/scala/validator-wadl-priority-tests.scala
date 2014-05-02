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
class ValidatorWADLPrioritySuite extends BaseValidatorSuite {

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

  val configs = Map[String, Config]("Config without ops " -> TestConfig(false, false, true, true, true, 2, true, false, false, "SaxonHE", false),
                                    "Config with removeDups" -> TestConfig(true, false, true, true, true, 2, true, false, false, "SaxonHE", false),
                                    "Config with joinXPathChecks" -> TestConfig(false, false, true, true, true, 2, true, false, false, "SaxonHE", true),
                                    "Config with removeDups and joinXPathChecks" -> TestConfig(true, false, true, true, true, 2, true, false, false, "SaxonHE", true))

  for ((description, config) <- configs) {

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
}
