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

  def borrowExpression(expression : String, nc : NamespaceContext) : XPathExpression =
    xpathExpressions.getOrElseUpdate(expression, new SoftReferenceObjectPool[XPathExpression](new XPathExpressionFactory(expression, nc))).borrowObject()
  def returnExpression(expression : String, xpathExpression : XPathExpression) : Unit = xpathExpressions(expression).returnObject(xpathExpression)
  def numActive (expression : String) : Int = xpathExpressions(expression).getNumActive()
  def numIdle (expression : String) : Int = xpathExpressions(expression).getNumIdle()
}


private class XPathExpressionFactory(private val expression : String, private val nc : NamespaceContext) extends PoolableObjectFactory[XPathExpression] {
  def makeObject = {
    val xpath = XPathFactory.newInstance(DEFAULT_OBJECT_MODEL_URI).newXPath()
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
