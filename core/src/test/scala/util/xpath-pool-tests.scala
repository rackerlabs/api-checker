package com.rackspace.com.papi.components.checker.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

import javax.xml.xpath.XPathExpression

import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class XPathExpressionPoolSuite extends FunSuite {
  test ("The pool should successfully create an xpath expression") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns))
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, xpath)
    }
  }

  test ("NumIdle should not be zero soon after returning an xpath expression") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns))
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, xpath)
      assert (XPathExpressionPool.numIdle(expression) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new xpath expressions") {
    val NUM_INCREASE = 5
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"

    val initActive = XPathExpressionPool.numActive(expression)
    val initIdle   = XPathExpressionPool.numIdle(expression)

    val builders = new Array[XPathExpression](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns))
    }

    assert (XPathExpressionPool.numActive(expression) >= initActive+NUM_INCREASE)

    val fullActive = XPathExpressionPool.numActive(expression)

    for (i <- 0 to NUM_INCREASE-1) {
      XPathExpressionPool.returnExpression(expression,builders(i))
    }

    assert (XPathExpressionPool.numActive(expression) <= fullActive-NUM_INCREASE)
  }

}
