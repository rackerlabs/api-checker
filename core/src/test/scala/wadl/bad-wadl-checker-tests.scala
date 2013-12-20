
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
      Given("a WADL with no resources")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource>
                 <method href="#missing"/>
              </resource>
           </resources>
        </application>
      When("the wadl is translated")
      val thrown = intercept[WADLException] {
        builder.build (inWADL, stdConfig)
      }
      Then("A WADL Exception should be thrown with the words 'missing' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("missing"))
      assert(thrown.getMessage().contains("does not seem to exist"))
    }

    scenario ("The WADL contains an XSD which is missing an import") {
      Given("a WADL that contains an XSD which is missing an import")
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
      When("the wadl is translated")
      val thrown = intercept[WADLException] {
        builder.build (inWADL, stdConfig)
      }
      Then("Then a WADLException should be thrown with the words 'transform.xsd' and 'does not seem to exist'.")
      assert(thrown.getMessage().contains("transform.xsd"))
      assert(thrown.getMessage().contains("does not seem to exist"))
      And("The exception should point to the file in error")
      assert(thrown.getMessage().contains("test://app/xsd/simple.xsd"))
    }

    scenario("A WADL contians extensions before plain params") {
      Given("A WADL that contians extension elements before plain params")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:foo="http://www.rackspace.com/foo/bar"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:check="http://www.rackspace.com/repose/wadl/checker"
             xmlns:atom="http://www.w3.org/2005/Atom">
    <resources base="http://localhost/">
        <resource path="y" type="#FOO"/>
    </resources>
    <resource_type id="FOO">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:bar">
               <rax:preprocess>
                  <xsl:transform check:mergable="true" version="2.0">
                     <xsl:output method="xml" encoding="UTF-8"/>
                     <xsl:variable name="entry" select="/atom:entry"/>
                     <xsl:variable name="event" select="$entry/atom:content/event:event"/>
                     <xsl:template match="/">
                        <xsl:choose>
                           <xsl:when xmlns:p="http://docs.rackspace.com/event/identity/user"
                                     test="$event/p:product">
                              <xsl:variable name="product" select="$event/p:product"/>
                              <xsl:choose>
                                 <xsl:when test="$product[@version = '2']">
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type = 'UPDATE') then $product/@updatedAttributes else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is UPDATE, the updatedAttributes attribute is required.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:choose>
                                       <xsl:when test="if ($event/@type != 'UPDATE') then not($product/@updatedAttributes) else true()"/>
                                       <xsl:otherwise>
                                          <xsl:message terminate="yes">For version 2 and type is other than UPDATE, the updatedAttributes attribute should not be used.</xsl:message>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </xsl:when>
                              </xsl:choose>
                           </xsl:when>
                        </xsl:choose>
                        <xsl:copy>
                           <xsl:apply-templates/>
                        </xsl:copy>
                     </xsl:template>
                  </xsl:transform>
               </rax:preprocess>
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:bar/@junk"/>
                </representation>
            </request>
        </method>
    </resource_type>
</application>
      When ("The wadl is translated")
      val thrown = intercept[WADLException] {
        builder.build (inWADL, stdConfig)
      }
      Then("An expetion should be thrown referencing the misplaced param")
      assert(thrown.getMessage().contains("param"))
    }
  }
}
