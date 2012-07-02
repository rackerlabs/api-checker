package com.rackspace.com.papi.components.checker.util

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

import javax.xml.namespace.NamespaceContext

import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathFactory._

object XPathExpressionPool {
  private val xpathExpressions : Map[String, SoftReferenceObjectPool[XPathExpression]] = new HashMap[String, SoftReferenceObjectPool[XPathExpression]]
  private val xpath2Expressions : Map[String, SoftReferenceObjectPool[XPathExpression]] = new HashMap[String, SoftReferenceObjectPool[XPathExpression]]

  def borrowExpression(expression : String, nc : NamespaceContext, version : Int) : XPathExpression = {
    version match {
      case 1 =>
        xpathExpressions.getOrElseUpdate(expression, new SoftReferenceObjectPool[XPathExpression](new XPathExpressionFactory(expression, nc))).borrowObject()
      case 2 =>
        xpath2Expressions.getOrElseUpdate(expression, new SoftReferenceObjectPool[XPathExpression](new XPath2ExpressionFactory(expression, nc))).borrowObject()
    }
  }

  def returnExpression(expression : String, version : Int, xpathExpression : XPathExpression) : Unit = {
    version match {
      case 1 => xpathExpressions(expression).returnObject(xpathExpression)
      case 2 => xpath2Expressions(expression).returnObject(xpathExpression)
    }
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
