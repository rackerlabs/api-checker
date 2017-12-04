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
package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation._

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.report.{ListReportProvider, LogLevel}
import com.rackspace.com.papi.components.checker.BaseValidatorSuite
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.XMLParserPool._
import com.rackspace.com.papi.components.checker.step.results.{BadMediaTypeResult, MismatchResult, Result}
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool

import org.w3c.dom.Document

import scala.xml._

class BaseStepSuite extends BaseValidatorSuite {
  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1",
    "org.apache.xerces.jaxp.validation.XMLSchema11Factory",
    this.getClass.getClassLoader)

  private val jsonSchemaFactory = JsonSchemaFactory.newBuilder.setReportProvider(new ListReportProvider(LogLevel.WARNING, LogLevel.ERROR)).freeze

  val xsl1Factory = TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", this.getClass.getClassLoader)
  val xsl2Factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)

  val xsl1Templates = xsl1Factory.newTemplates (new StreamSource(getClass.getResourceAsStream("/xsl/testXSL1.xsl")))
  val xsl2Templates = xsl2Factory.newTemplates (new StreamSource(getClass.getResourceAsStream("/xsl/testXSL2.xsl")))

  val EMPTY_DOC = {
    val parser = borrowParser
    try {
      parser.newDocument
    } finally {
      returnParser(parser)
    }
  }

  val EMPTY_JSON = {
    var om : ObjectMapper = null
    try {
      om = ObjectMapperPool.borrowParser
      om.readTree("null")
    } finally {
      if (om != null) ObjectMapperPool.returnParser(om)
    }
  }


  //
  //  Enable CTA full XPath2.0 checking in XSD 1.1
  //
  schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

  //
  //  Test schema
  //
  val testSchema = schemaFactory.newSchema(new StreamSource(getClass.getResourceAsStream("/xsd/test-urlxsd.xsd")))

  //
  // Test json schema
  //
  val testJSONSchema = {
    var om : ObjectMapper = null
    try {
      om = ObjectMapperPool.borrowParser
      val jsonGrammar = om.readValue(getClass.getResource("/jsonSchema/test.json"), classOf[JsonNode])
      jsonSchemaFactory.getJsonSchema(jsonGrammar)
    } finally {
      if (om != null) ObjectMapperPool.returnParser(om)
    }
  }

  //cd
  // Test simple types
  //
  val stepType    = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "StepType", "tst")
  val uuidType    = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "UUID", "tst")
  val evenIntType = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "EvenInt100", "tst")

  override def request (method : String, url : String) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url))
  }

  override def request(method : String, url : String, contentType : String) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url, contentType))
  }

  override def request(method : String, url : String, contentType : String, content : String) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url, contentType, content))
  }

  override def request(method : String, url : String, contentType : String, content : NodeSeq) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url, contentType, content))
  }

  override def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean) : CheckerServletRequest = {
    new CheckerServletRequest (super.request (method, url, contentType, content, parseContent))
  }

  override def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean) : CheckerServletRequest = {
    new CheckerServletRequest (super.request (method, url, contentType, content, parseContent))
  }

  override def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean, headers : Map[String, List[String]]) : CheckerServletRequest = {
    new CheckerServletRequest (super.request (method, url, contentType, content, parseContent, headers))
  }

  override def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean, headers : Map[String, List[String]]) : CheckerServletRequest = {
    new CheckerServletRequest (super.request (method, url, contentType, content, parseContent, headers))
  }

  override def response  : CheckerServletResponse = {
    new CheckerServletResponse (super.response)
  }

  def assertMismatchResult(res :Option[Result]) : Unit = {
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MismatchResult])
  }

  def assertBadMediaType(res : Option[Result]) : Unit = {
    assert (res.isDefined)
    assert (res.get.isInstanceOf[BadMediaTypeResult])
  }
}
