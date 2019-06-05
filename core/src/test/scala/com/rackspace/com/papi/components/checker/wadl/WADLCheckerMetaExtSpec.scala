/***
 *   Copyright 2016 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.wadl

import javax.xml.transform.Templates
import javax.xml.transform.dom.{DOMResult, DOMSource}
import javax.xml.transform.stream.StreamSource

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.com.papi.components.checker.{Config, LogAssertions}
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WADLCheckerMetaExtSpec extends BaseCheckerSpec with LogAssertions {
  scenario("A WADL with metadata extension enabled should produce a valid WADL, even in the presens of existing schemas") {
    Given("A WADL with existing schemas an template params")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:meta="http://docs.rackspace.com/metadata/api"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}" rax:useMetadata="myMetadata">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <rax:metadata id="myMetadata">
               <rax:metaRole name="admin" pattern="*"/>
               <rax:metaRole name="billing:role" pattern="billing:"/>
           </rax:metadata>
        </application>
    register("test://app/xsd/simple.xsd",
             <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/a">
                   <simpleType name="yesno">
                       <restriction base="xsd:string">
                           <enumeration value="yes"/>
                           <enumeration value="no"/>
                       </restriction>
                   </simpleType>
                </schema>)
    When("The WADL is ran by the Metadata extension XSL")
    val wadl = new WADLNormalizer
    val raxMetaTransformTemplates: Templates =
      wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/meta-transform.xsl").toString))
    val handler = wadl.saxTransformerFactory.newTransformerHandler(raxMetaTransformTemplates)
    val transformer = handler.getTransformer
    val res = new DOMResult
    val src = new StreamSource(inWADL)
    src.setSystemId("test://app/myWADL.wadl")
    transformer.transform (src, res)
    Then("The WADL should be one that is loaded correctly by API checker")
    val result2 = new DOMResult
    val src2 = new DOMSource(res.getNode)
    src2.setSystemId("test://app/myWADL.wadl")

    val config = new Config
    config.enableRaxRolesExtension=true
    config.maskRaxRoles403=true
    builder.build(src2, result2, config)
  }
}
