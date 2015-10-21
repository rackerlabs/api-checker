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

import com.yammer.metrics.core.Gauge
import com.yammer.metrics.scala.Instrumented
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.{HashMap, MutableList, Map}

object XPathExpressionPool extends Instrumented {
  private val xpathExpressions : Map[(String, NamespaceContext), SoftReferenceObjectPool[XPathExpression]] = new HashMap[(String, NamespaceContext),
                                                                                                                         SoftReferenceObjectPool[XPathExpression]]
  private val xpath2Expressions : Map[(String, NamespaceContext), SoftReferenceObjectPool[XPathExpression]] = new HashMap[(String, NamespaceContext),
                                                                                                                          SoftReferenceObjectPool[XPathExpression]]

  private val activeGauges = new MutableList[Gauge[Int]]
  private val idleGauges = new MutableList[Gauge[Int]]

  private def addXPathPool(expression : String, nc : NamespaceContext, version : Int) : SoftReferenceObjectPool[XPathExpression] = {
    val pool = new SoftReferenceObjectPool[XPathExpression](version match { case 1 => new XPathExpressionFactory(expression, nc)
                                                                            case 2 => new XPath2ExpressionFactory(expression, nc)
                                                                         })
    activeGauges :+ metrics.gauge("Active", expression+" ("+version+")")(pool.getNumActive)
    idleGauges :+ metrics.gauge("Idle", expression+" ("+version+")")(pool.getNumIdle)
    pool
  }

  def borrowExpression(expression : String, nc : NamespaceContext, version : Int) : XPathExpression = {
    version match {
      case 1 =>
        xpathExpressions.getOrElseUpdate((expression, nc), addXPathPool(expression, nc, version)).borrowObject()
      case 2 =>
        xpath2Expressions.getOrElseUpdate((expression, nc), addXPathPool(expression,nc,version)).borrowObject()
    }
  }

  def returnExpression(expression : String, nc : NamespaceContext, version : Int, xpathExpression : XPathExpression) : Unit = {
    version match {
      case 1 => xpathExpressions((expression, nc)).returnObject(xpathExpression)
      case 2 => xpath2Expressions((expression, nc)).returnObject(xpathExpression)
    }
  }

  def numActive (expression : String, nc : NamespaceContext, version : Int) : Int = {
    version match {
      case 1 => xpathExpressions((expression, nc)).getNumActive()
      case 2 => xpath2Expressions((expression,nc)).getNumActive()
    }
  }

  def numIdle (expression : String, nc : NamespaceContext, version : Int) : Int = {
    version match {
      case 1 => xpathExpressions((expression, nc)).getNumIdle()
      case 2 => xpath2Expressions((expression, nc)).getNumIdle()
    }
  }
}

private class XPathExpressionFactory(private val expression : String, private val nc : NamespaceContext) extends PoolableObjectFactory[XPathExpression] {
  def makeObject = {
    val xpath = (new org.apache.xpath.jaxp.XPathFactoryImpl()).newXPath()
    xpath.setNamespaceContext(nc)
    xpath.compile(expression)
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


private class XPath2ExpressionFactory(private val expression : String, private val nc : NamespaceContext) extends XPathExpressionFactory(expression, nc) {
  override def makeObject = {
    val xpath = (new net.sf.saxon.xpath.XPathFactoryImpl()).newXPath()
    xpath.setNamespaceContext(nc)
    xpath.compile(expression)
  }
}
