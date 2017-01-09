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

import javax.xml.namespace.NamespaceContext
import javax.xml.xpath.XPathExpression

import com.codahale.metrics.{Gauge, MetricRegistry}
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.{HashMap, MutableList, Map}

object XPathExpressionPool extends Instrumented {
  private val xpathExpressions : Map[(String, NamespaceContext), SoftReferenceObjectPool[XPathExpression]] = new HashMap[(String, NamespaceContext), 
                                                                                                                         SoftReferenceObjectPool[XPathExpression]]
  private val xpathNExpressions : Map[(String, NamespaceContext, Int), SoftReferenceObjectPool[XPathExpression]] = new HashMap[(String, NamespaceContext, Int),
                                                                                                                          SoftReferenceObjectPool[XPathExpression]]

  private val activeGauges = new MutableList[Gauge[Int]]
  private val idleGauges = new MutableList[Gauge[Int]]

  private def addXPathPool(expression : String, nc : NamespaceContext, version : Int) : SoftReferenceObjectPool[XPathExpression] = {
    val pool = new SoftReferenceObjectPool[XPathExpression](version match { case 10 => new XPathExpressionFactory(expression, nc)
                                                                            case v : Int => new XPathNExpressionFactory(expression, nc, v)
                                                                         })
    val registryClassName = getRegistryClassName(getClass)
    val metricName = s"$expression ($version)"
    activeGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Active", metricName))(pool.getNumActive)
    idleGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Idle", metricName))(pool.getNumIdle)
    pool
  }

  def borrowExpression(expression : String, nc : NamespaceContext, version : Int) : XPathExpression = {
    version match {
      case 10 =>
        xpathExpressions.getOrElseUpdate((expression, nc), addXPathPool(expression, nc, version)).borrowObject()
      case _ =>
        xpathNExpressions.getOrElseUpdate((expression, nc, version), addXPathPool(expression,nc,version)).borrowObject()
    }
  }

  def returnExpression(expression : String, nc : NamespaceContext, version : Int, xpathExpression : XPathExpression) : Unit = {
    version match {
      case 10 => xpathExpressions((expression, nc)).returnObject(xpathExpression)
      case _ => xpathNExpressions((expression, nc, version)).returnObject(xpathExpression)
    }
  }

  def numActive (expression : String, nc : NamespaceContext, version : Int) : Int = {
    version match {
      case 10 => xpathExpressions((expression, nc)).getNumActive
      case _ => xpathNExpressions((expression,nc, version)).getNumActive
    }
  }

  def numIdle (expression : String, nc : NamespaceContext, version : Int) : Int = {
    version match {
      case 10 => xpathExpressions((expression, nc)).getNumIdle
      case _ => xpathNExpressions((expression, nc, version)).getNumIdle
    }
  }
}

private class XPathExpressionFactory(private val expression : String, private val nc : NamespaceContext) extends PoolableObjectFactory[XPathExpression] {
  def makeObject = {
    val xpath = (new org.apache.xpath.jaxp.XPathFactoryImpl()).newXPath()
    xpath.setNamespaceContext(nc)
    VarXPathExpression.compile (xpath, expression)
  }

  def validateObject (xpath : XPathExpression) : Boolean = xpath != null

  def activateObject (xpath : XPathExpression) : Unit = {
    //
    //  Not needed...
    //
  }

  def passivateObject (xpath : XPathExpression) : Unit = {
    //
    //  Not needed...
    //
  }

  def destroyObject (xpath : XPathExpression) : Unit = {
    //
    //  Not needed...
    //
  }
}


private class XPathNExpressionFactory(private val expression : String, private val nc : NamespaceContext, private val version : Int) extends XPathExpressionFactory(expression, nc) {
  override def makeObject = {
    val xpath = (new net.sf.saxon.xpath.XPathFactoryImpl()).newXPath()
    xpath.asInstanceOf[net.sf.saxon.xpath.XPathEvaluator].getStaticContext().setXPathLanguageLevel(version)
    xpath.setNamespaceContext(nc)
    VarXPathExpression.compile (xpath, expression)
  }
}
