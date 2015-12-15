/** *
  * Copyright 2014 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.Converters._
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import org.w3c.dom.Document

abstract class MultiSchemaElementsBaseSuite extends BaseValidatorSuite {
  val wadl_SimpleSame =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:tstOne="http://www.rackspace.com/repose/wadl/simple/one/test">
      <grammars>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                targetNamespace="http://www.rackspace.com/repose/wadl/simple/one/test">
          <simpleType name="Progress">
            <restriction base="xsd:integer">
              <minInclusive value="0"/>
              <maxInclusive value="100"/>
            </restriction>
          </simpleType>
        </schema>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                targetNamespace="http://www.rackspace.com/repose/wadl/simple/one/test">
          <simpleType name="UUID">
            <restriction base="xsd:string">
              <length value="36" fixed="true"/>
              <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
            </restriction>
          </simpleType>
        </schema>
      </grammars>
      <resources base="https://test.rackspace.com">
        <resource id="progress" path="test/progress/{progress}">
          <param name="progress" style="template" type="tstOne:Progress"/>
          <method href="#getMethod"/>
        </resource>
        <resource id="uuid" path="test/uuid/{uuid}">
          <param name="uuid" style="template" type="tstOne:UUID"/>
          <method href="#getMethod"/>
        </resource>
      </resources>
      <method id="getMethod" name="GET">
        <response status="200 203"/>
      </method>
    </application>

  val wadl_SimpleDiff =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:tstOne="http://www.rackspace.com/repose/wadl/simple/one/test"
                 xmlns:tstTwo="http://www.rackspace.com/repose/wadl/simple/two/test">
      <grammars>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                targetNamespace="http://www.rackspace.com/repose/wadl/simple/one/test">
          <simpleType name="Progress">
            <restriction base="xsd:integer">
              <minInclusive value="0"/>
              <maxInclusive value="100"/>
            </restriction>
          </simpleType>
        </schema>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                targetNamespace="http://www.rackspace.com/repose/wadl/simple/two/test">
          <simpleType name="UUID">
            <restriction base="xsd:string">
              <length value="36" fixed="true"/>
              <pattern value="[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"/>
            </restriction>
          </simpleType>
        </schema>
      </grammars>
      <resources base="https://test.rackspace.com">
        <resource id="progress" path="test/progress/{progress}">
          <param name="progress" style="template" type="tstOne:Progress"/>
          <method href="#getMethod"/>
        </resource>
        <resource id="uuid" path="test/uuid/{uuid}">
          <param name="uuid" style="template" type="tstTwo:UUID"/>
          <method href="#getMethod"/>
        </resource>
      </resources>
      <method id="getMethod" name="GET">
        <response status="200 203"/>
      </method>
    </application>

  val wadl_ElementSame =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:tstOne="http://www.rackspace.com/repose/wadl/element/one/test">
      <grammars>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tstOne="http://www.rackspace.com/repose/wadl/element/one/test"
                targetNamespace="http://www.rackspace.com/repose/wadl/element/one/test">
          <element name="e" type="tstOne:SampleElement"/>
          <complexType name="SampleElement">
            <sequence>
              <element name="id" type="xsd:integer" minOccurs="0" default="1"/>
            </sequence>
          </complexType>
        </schema>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tstOne="http://www.rackspace.com/repose/wadl/element/one/test"
                targetNamespace="http://www.rackspace.com/repose/wadl/element/one/test">
          <element name="a" type="tstOne:SampleAttribute"/>
          <complexType name="SampleAttribute">
            <attribute name="id" type="xsd:integer" use="optional" default="2"/>
          </complexType>
        </schema>
      </grammars>
      <resources base="https://test.rackspace.com">
        <resource path="/test">
          <method name="PUT">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="POST">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>

  val wadl_ElementDiff =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:tstOne="http://www.rackspace.com/repose/wadl/element/one/test"
                 xmlns:tstTwo="http://www.rackspace.com/repose/wadl/element/two/test">
      <grammars>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tstOne="http://www.rackspace.com/repose/wadl/element/one/test"
                targetNamespace="http://www.rackspace.com/repose/wadl/element/one/test">
          <element name="e" type="tstOne:SampleElement"/>
          <complexType name="SampleElement">
            <sequence>
              <element name="id" type="xsd:integer" minOccurs="0" default="1"/>
            </sequence>
          </complexType>
        </schema>
        <schema elementFormDefault="qualified"
                attributeFormDefault="unqualified"
                xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tstTwo="http://www.rackspace.com/repose/wadl/element/two/test"
                targetNamespace="http://www.rackspace.com/repose/wadl/element/two/test">
          <element name="a" type="tstTwo:SampleAttribute"/>
          <complexType name="SampleAttribute">
            <attribute name="id" type="xsd:integer" use="optional" default="2"/>
          </complexType>
        </schema>
      </grammars>
      <resources base="https://test.rackspace.com">
        <resource path="/test">
          <method name="PUT">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
          <method name="POST">
            <request>
              <representation mediaType="application/xml"/>
            </request>
          </method>
        </resource>
      </resources>
    </application>

  def sameDiff(same: Boolean): String = {
    if(same) {
      "Same"
    }else {
      "Diff"
    }
  }

  def createConfigWithSaxonEE(enabled: Boolean): Config = {
    val config = new Config

    if (enabled) {
      config.xsdEngine = "SaxonEE"
    }
    config.removeDups            = true // -d Wadl2Checker default is different from Config default.
    config.checkWellFormed       = true // -w
    config.checkXSDGrammar       = true // -x
    config.checkPlainParams      = true // -p
    config.doXSDGrammarTransform = true // -g
    config.joinXPathChecks       = true // -j
    config.checkHeaders          = true // -H
    config.validateChecker       = true // !-D

    config.resultHandler = TestConfig.assertHandler

    config
  }

  def assertions_Simple(validator: Validator, same: Boolean, useSaxon: Boolean) {
    test("GET with valid Progress should succeed on Simple-" + sameDiff(same)) {
      validator.validate(request("GET", "/test/progress/100"), response, chain)
    }

    test("GET with invalid Progress should fail on Simple-" + sameDiff(same)) {
      assertResultFailed(validator.validate(request("GET", "/test/progress/101"), response, chain), 404)
    }

    test("GET with illegal Progress should fail on Simple-" + sameDiff(same)) {
      assertResultFailed(validator.validate(request("GET", "/test/progress/hello"), response, chain), 404)
    }

    // NOTE: This is a bug that was identified in Xerces and is being reported, but until it is fixed will cause
    // failures when multiple Schema elements are present in the same targetNamespace.
    if(useSaxon || !same) {
      test("GET with valid UUID should succeed on Simple-" + sameDiff(same)) {
        validator.validate(request("GET", "/test/uuid/bbe10c88-6477-11e1-84cf-979e24b1498f"), response, chain)
      }
    }

    test("GET with invalid UUID should fail on Simple-" + sameDiff(same)) {
      assertResultFailed(validator.validate(request("GET", "/test/uuid/bbe10c88-6477-11e1-84cf-979e24b1498z"), response, chain), 404)
    }

    test("GET with illegal UUID should fail on Simple-" + sameDiff(same)) {
      assertResultFailed(validator.validate(request("GET", "/test/uuid/bbe10c88-6477-11e1-84cf-979e24b1498"), response, chain), 404)
    }
  }

  def assertions_Element(validator: Validator, same: Boolean, useSaxon: Boolean) {
    test("PUT with valid XML One should succeed on Element-" + sameDiff(same)) {
      val req = request("PUT", "/test", "application/xml",
        <e xmlns="http://www.rackspace.com/repose/wadl/element/one/test">
          <id>10</id>
        </e>
      )
      validator.validate(req, response, chain)
      val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
      assert((dom \ "id").text == "10")
    }

    // NOTE: This is a bug that was identified in Xerces and is being reported, but until it is fixed will cause
    // failures when multiple Schema elements are present in the same targetNamespace.
    if(useSaxon || !same) {
      test("PUT with valid XML Two should succeed on Element-" + sameDiff(same)) {
        val req = if (same) {
          request("PUT", "/test", "application/xml",
              <a xmlns="http://www.rackspace.com/repose/wadl/element/one/test" id="20"/>
          )
        } else {
          request("PUT", "/test", "application/xml",
              <a xmlns="http://www.rackspace.com/repose/wadl/element/two/test" id="20"/>
          )
        }
        validator.validate(req, response, chain)
        val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
        assert((dom \ "@id").text == "20")
      }
    }

    test("PUT with invalid XML should fail on Element-" + sameDiff(same)) {
      assertResultFailed(validator.validate(request("PUT", "/test", "application/xml",
        <e xmlns="http://www.rackspace.com/repose/wadl/element/one/test">
          <junk/>
        </e>
      ), response, chain), 400)
    }

    test("POST with valid XML One should succeed and default values should be filled in on Element-" + sameDiff(same)) {
      val req = request("POST", "/test", "application/xml",
        <e xmlns="http://www.rackspace.com/repose/wadl/element/one/test">
          <id/>
        </e>
      )
      validator.validate(req, response, chain)
      val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
      assert((dom \ "id").text == "1")
    }

    // NOTE: This is a bug that was identified in Xerces and is being reported, but until it is fixed will cause
    // failures when multiple Schema elements are present in the same targetNamespace.
    if(useSaxon || !same) {
      test("POST with valid XML Two should succeed and default values should be filled in on Element-" + sameDiff(same)) {
        val req = if (same) {
          request("POST", "/test", "application/xml",
              <a xmlns="http://www.rackspace.com/repose/wadl/element/one/test"/>
          )
        } else {
          request("POST", "/test", "application/xml",
              <a xmlns="http://www.rackspace.com/repose/wadl/element/two/test"/>
          )
        }
        validator.validate(req, response, chain)
        val dom = req.getAttribute(PARSED_XML).asInstanceOf[Document]
        assert((dom \ "@id").text == "2")
      }
    }
  }
}
