package com.rackspace.com.papi.components.checker.util

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.LinkedList

import javax.xml.namespace.NamespaceContext

import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathFactory._

import com.yammer.metrics.core.Gauge
import com.yammer.metrics.scala.Instrumented

object XPathExpressionPool extends Instrumented {
  private val xpathExpressions : Map[String, SoftReferenceObjectPool[XPathExpression]] = new HashMap[String, SoftReferenceObjectPool[XPathExpression]]
  private val xpath2Expressions : Map[String, SoftReferenceObjectPool[XPathExpression]] = new HashMap[String, SoftReferenceObjectPool[XPathExpression]]

  private val activeGauges = new LinkedList[Gauge[Int]]
  private val idleGauges = new LinkedList[Gauge[Int]]

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
      case 1 => new XPathExpressionFactory(expression, nc).makeObject
      case 2 => new XPath2ExpressionFactory(expression, nc).makeObject
    }
  }

  def returnExpression(expression : String, version : Int, xpathExpression : XPathExpression) : Unit = {

  }

  def numActive (expression : String, version : Int) : Int = {
    version match {
      case 1 => xpathExpressions(expression).getNumActive()
      case 2 => xpath2Expressions(expression).getNumActive()
    }
  }

  def numIdle (expression : String, version : Int) : Int = {
    version match {
      case 1 => xpathExpressions(expression).getNumIdle()
      case 2 => xpath2Expressions(expression).getNumIdle()
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
