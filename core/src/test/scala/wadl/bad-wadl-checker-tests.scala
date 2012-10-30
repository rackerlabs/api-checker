
package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

@RunWith(classOf[JUnitRunner])
class BadWADLCheckerSpec extends BaseCheckerSpec {

  //
  //  These simple tests simply affirm that WADL Tools is doing it's
  //  job, and that we are in fact throwing WADL Exceptions, when a
  //  referance is broken.
  //

  feature ("The WADLCheckerBuilder should reject a WADL that violates a WADL format") {

    info ("As a developer")
    info ("I want to be able to ensure that invalid WADL fail the transformation to checker format ")
    info ("so that problems with WADLs are caught early")

    scenario("The WADL contains a missing local reference") {
      given("a WADL with no resources")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource>
                 <method href="#missing"/>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val thrown = intercept[WADLException] {
        builder.build (inWADL, stdConfig)
      }
      then("A WADL Exception should be thrown with the words 'missing' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("missing"))
      assert(thrown.getMessage().contains("does not seem to exist"))
    }

    scenario ("The WADL contains an XSD which is missing an import") {
      given("a WADL that contains an XSD which is missing an import")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <import namespace="http://www.w3.org/1999/XSL/Transform"
                           schemaLocation="transform.xsd"/>
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the wadl is translated")
      val thrown = intercept[WADLException] {
        builder.build (inWADL, stdConfig)
      }
      then("Then a WADLException should be thrown with the words 'transform.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("transform.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      and("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://app/xsd/simple.xsd"))
    }
  }
}
