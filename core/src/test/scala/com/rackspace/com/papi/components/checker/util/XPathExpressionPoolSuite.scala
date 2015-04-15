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

import javax.xml.xpath.{XPathExpression, XPathExpressionException}

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class XPathExpressionPoolSuite extends FunSuite {

  val XPATH_VERSION_1 = 1
  val XPATH_VERSION_2 = 2

  test ("The pool should successfully create an xpath expression") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }
  }

  test ("The same xpath expression should be returned when we ask to borrow the same expression after returning it") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val nsContext2 = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    val expression2 = "/ns:root"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression2, nsContext2, XPATH_VERSION_1)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression2, nsContext2, XPATH_VERSION_1, xpath2)
    }

    assert(xpath == xpath2)
    assert(xpath.hashCode == xpath2.hashCode)
  }

  test ("The same xpath expression with different namespace contexts should return different XPath expression objects") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val nsContext2 = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace/v2"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression, nsContext2, XPATH_VERSION_1)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression, nsContext2, XPATH_VERSION_1, xpath2)
    }

    assert(xpath != xpath2)
    assert(xpath.hashCode != xpath2.hashCode)
  }

  test ("The different xpath expressions with the same namespace context should return different XPath expressions objects") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    val expression2 = "/ns:root/ns:bar"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression2, nsContext, XPATH_VERSION_1)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression2, nsContext, XPATH_VERSION_1, xpath2)
    }

    assert(xpath != xpath2)
    assert(xpath.hashCode != xpath2.hashCode)
  }

  test ("The pool should fail to create an xpath expression if there's an error in the expression") {
    val nsContext = ImmutableNamespaceContext(Map[String,String]())
    val expression = "/ns:root()"
    var xpath : XPathExpression = null
    try {
      intercept[XPathExpressionException] {
        xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      }
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }
  }

  test ("The pool should fail to create an xpath expression if a valid XPath 2 expression is passed with version == 1") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "if (/ns:root) then true() else false()"
    var xpath : XPathExpression = null
    try {
      intercept[XPathExpressionException] {
        xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
      }
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
    }
  }

  test ("NumIdle should not be zero soon after returning an xpath expression") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, xpath)
      assert (XPathExpressionPool.numIdle(expression, nsContext, XPATH_VERSION_1) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new xpath expressions") {
    val NUM_INCREASE = 5
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"

    val initActive = XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_1)
    val initIdle   = XPathExpressionPool.numIdle(expression, nsContext, XPATH_VERSION_1)

    val builders = new Array[XPathExpression](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1)
    }

    assert (XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_1) >= initActive+NUM_INCREASE)

    val fullActive = XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_1)

    for (i <- 0 to NUM_INCREASE-1) {
      XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, builders(i))
    }

    assert (XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_1) <= fullActive-NUM_INCREASE)
  }

  test ("The pool should successfully create an xpath expression (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
    }
  }

  test ("The same xpath expression should be returned when we ask to borrow the same expression after returning it (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val nsContext2 = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    val expression2 = "/ns:root"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression2, nsContext2, XPATH_VERSION_2)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression2, nsContext2, XPATH_VERSION_2, xpath2)
    }

    assert(xpath == xpath2)
    assert(xpath.hashCode == xpath2.hashCode)
  }

  test ("The same xpath expression with different namespace contexts should return different XPath expression objects (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val nsContext2 = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace/v2"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression, nsContext2, XPATH_VERSION_2)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression, nsContext2, XPATH_VERSION_2, xpath2)
    }

    assert(xpath != xpath2)
    assert(xpath.hashCode != xpath2.hashCode)
  }

  test ("The different xpath expressions with the same namespace context should return different XPath expressions objects (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    val expression2 = "/ns:root/ns:bar"
    var xpath : XPathExpression = null
    var xpath2 : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
      assert (xpath != null)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
    }

    try {
      xpath2 = XPathExpressionPool.borrowExpression(expression2, nsContext, XPATH_VERSION_2)
      assert (xpath2 != null)
    } finally {
      if (xpath2 != null) XPathExpressionPool.returnExpression(expression2, nsContext, XPATH_VERSION_2, xpath2)
    }

    assert(xpath != xpath2)
    assert(xpath.hashCode != xpath2.hashCode)
  }

  test ("The pool should fail to create an xpath expression if there's an error in the expression (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map[String,String]())
    val expression = "/ns:root()"
    var xpath : XPathExpression = null
    try {
      intercept[XPathExpressionException] {
        xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
      }
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
    }
  }

  test ("NumIdle should not be zero soon after returning an xpath expression (XPath 2)") {
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"
    var xpath : XPathExpression = null
    try {
      xpath = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
    } finally {
      if (xpath != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, xpath)
      assert (XPathExpressionPool.numIdle(expression, nsContext, XPATH_VERSION_2) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new xpath expressions (XPath 2)") {
    val NUM_INCREASE = 5
    val nsContext = ImmutableNamespaceContext(Map("ns" -> "http://my/namespace"))
    val expression = "/ns:root"

    val initActive = XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_2)
    val initIdle   = XPathExpressionPool.numIdle(expression, nsContext, XPATH_VERSION_2)

    val builders = new Array[XPathExpression](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2)
    }

    assert (XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_2) >= initActive+NUM_INCREASE)

    val fullActive = XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_2)

    for (i <- 0 to NUM_INCREASE-1) {
      XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, builders(i))
    }

    assert (XPathExpressionPool.numActive(expression, nsContext, XPATH_VERSION_2) <= fullActive-NUM_INCREASE)
  }

}
