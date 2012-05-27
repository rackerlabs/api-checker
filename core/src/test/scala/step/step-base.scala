package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.BaseValidatorSuite

import javax.xml.validation._
import javax.xml.transform.stream._
import javax.xml.namespace.QName

import org.mockito.Mockito._

class BaseStepSuite extends BaseValidatorSuite {
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "com.saxonica.jaxp.SchemaFactoryImpl")

  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")
  private val schemaFactorySaxon = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")

  //
  //  Enable CTA full XPath2.0 checking in XSD 1.1
  //
  schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

  //
  //  Enable 1.1 support in saxon
  //
  schemaFactorySaxon.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")

  //
  //  Test schema
  //
  val testSchema = schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/test-urlxsd.xsd")))
  val testSchemaSaxon = schemaFactorySaxon.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/test-urlxsd.xsd")))

  //cd
  // Test simple types
  //
  val stepType    = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "StepType", "tst")
  val uuidType    = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "UUID", "tst")
  val evenIntType = new QName("http://www.rackspace.com/repose/wadl/checker/step/test", "EvenInt100", "tst")

  override def request (method : String, url : String) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url))
  }

  override def response  : CheckerServletResponse = {
    new CheckerServletResponse (super.response)
  }

  def assertMismatchResult(res :Option[Result]) : Unit = {
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MismatchResult])
  }
}
