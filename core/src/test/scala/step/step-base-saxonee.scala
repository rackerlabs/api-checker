package com.rackspace.com.papi.components.checker.step

import javax.xml.validation._
import javax.xml.transform.stream._

class BaseStepSuiteSaxonEE extends BaseStepSuite {
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "com.saxonica.jaxp.SchemaFactoryImpl")

  private val schemaFactorySaxon = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")

  //
  //  Enable 1.1 support in saxon
  //
  schemaFactorySaxon.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")

  val testSchemaSaxon = schemaFactorySaxon.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/test-urlxsd.xsd")))
}
