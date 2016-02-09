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
package com.rackspace.com.papi.components.checker.util

import javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}

import com.rackspace.com.papi.components.checker.Instrumented
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.util.Try

object XMLParserPool extends Instrumented with LazyLogging {
  private val pool = new SoftReferenceObjectPool[DocumentBuilder](new XMLParserFactory)
  Try {
    metrics.gauge("Active")(numActive)
  } recover {
    case e: RuntimeException => logger.info("Problem adding new Active gauge metric.", e)
  }
  Try {
    metrics.gauge("Idle")(numIdle)
  } recover {
    case e: RuntimeException => logger.info("Problem adding new Idle gauge metric.", e)
  }

  def borrowParser : DocumentBuilder = pool.borrowObject()
  def returnParser (builder : DocumentBuilder) : Unit = pool.returnObject(builder)
  def numActive : Int = pool.getNumActive()
  def numIdle : Int = pool.getNumIdle()
}

private class XMLParserFactory extends PoolableObjectFactory[DocumentBuilder] {
  val builderFactory = DocumentBuilderFactory.newInstance ("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", this.getClass.getClassLoader)

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
