package com.rackspace.com.papi.components.checker.util

import javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import com.yammer.metrics.scala.Instrumented

object XMLParserPool extends Instrumented {
  private val pool = new SoftReferenceObjectPool[DocumentBuilder](new XMLParserFactory)
  private val activeGauge = metrics.gauge("Active")(numActive)
  private val idleGauge = metrics.gauge("Idle")(numIdle)

  def borrowParser : DocumentBuilder = pool.borrowObject()
  def returnParser (builder : DocumentBuilder) : Unit = pool.returnObject(builder)
  def numActive : Int = pool.getNumActive()
  def numIdle : Int = pool.getNumIdle()
}

private class XMLParserFactory extends PoolableObjectFactory[DocumentBuilder] {
  val builderFactory = DocumentBuilderFactory.newInstance ("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", null)

  //
  //  Setup the builder factory so that it works within the security
  //  constraints of a webservice.
  //
  builderFactory.setCoalescing(true)
  builderFactory.setIgnoringComments(true)
  builderFactory.setNamespaceAware(true)
  builderFactory.setValidating(false)
  builderFactory.setXIncludeAware(false)
  builderFactory.setExpandEntityReferences (false)
  builderFactory.setFeature (FEATURE_SECURE_PROCESSING, true)

  def makeObject = builderFactory.newDocumentBuilder()

  def activateObject (builder : DocumentBuilder) : Unit = {
    //
    //  No need to activate the parser should be ready to go.
    //
  }

  def validateObject (builder : DocumentBuilder) : Boolean = builder != null

  def passivateObject (builder : DocumentBuilder) : Unit = {
    builder.reset()
  }

  def destroyObject (builder : DocumentBuilder) : Unit = {
    //
    //  Not needed...
    //
  }

}
