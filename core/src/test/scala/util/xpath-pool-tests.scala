package com.rackspace.com.papi.components.checker.util

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

import javax.xml.xpath.XPathExpression

import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class XPathExpressionPoolSuite extends FunSuite {

  val XPATH_VERSION_1 = 1
  val XPATH_VERSION_2 = 2

  test ("The pool should successfully create an xpath expression") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_1)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, XPATH_VERSION_1, xpath)
    }
  }

  test ("NumIdle should not be zero soon after returning an xpath expression") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_1)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, XPATH_VERSION_1, xpath)
      assert (XPathExpressionPool.numIdle(expression, XPATH_VERSION_1) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new xpath expressions") {
    val NUM_INCREASE = 5
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"

    val initActive = XPathExpressionPool.numActive(expression, XPATH_VERSION_1)
    val initIdle   = XPathExpressionPool.numIdle(expression, XPATH_VERSION_1)

    val builders = new Array[XPathExpression](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_1)
    }

    assert (XPathExpressionPool.numActive(expression, XPATH_VERSION_1) >= initActive+NUM_INCREASE)

    val fullActive = XPathExpressionPool.numActive(expression, XPATH_VERSION_1)

    for (i <- 0 to NUM_INCREASE-1) {
      XPathExpressionPool.returnExpression(expression, XPATH_VERSION_1, builders(i))
    }

    assert (XPathExpressionPool.numActive(expression, XPATH_VERSION_1) <= fullActive-NUM_INCREASE)
  }

  test ("The pool should successfully create an xpath expression (XPath 2)") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_2)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, XPATH_VERSION_2, xpath)
    }
  }

  test ("NumIdle should not be zero soon after returning an xpath expression (XPath 2)") {
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_2)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, XPATH_VERSION_2, xpath)
      assert (XPathExpressionPool.numIdle(expression, XPATH_VERSION_2) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new xpath expressions (XPath 2)") {
    val NUM_INCREASE = 5
    val ns = Map("ns" -> "http://my/namespace")
    val expression = "/ns:root"

    val initActive = XPathExpressionPool.numActive(expression, XPATH_VERSION_2)
    val initIdle   = XPathExpressionPool.numIdle(expression, XPATH_VERSION_2)

    val builders = new Array[XPathExpression](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XPathExpressionPool.borrowExpression(expression, ImmutableNamespaceContext(ns), XPATH_VERSION_2)
    }

    assert (XPathExpressionPool.numActive(expression, XPATH_VERSION_2) >= initActive+NUM_INCREASE)

    val fullActive = XPathExpressionPool.numActive(expression, XPATH_VERSION_2)

    for (i <- 0 to NUM_INCREASE-1) {
      XPathExpressionPool.returnExpression(expression, XPATH_VERSION_2, builders(i))
    }

    assert (XPathExpressionPool.numActive(expression, XPATH_VERSION_2) <= fullActive-NUM_INCREASE)
  }

}
