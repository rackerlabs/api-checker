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

import javax.xml.xpath.{XPathExpression, XPathExpressionException, XPathConstants}
import javax.xml.namespace.QName

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class VarXPathExpressionSuite extends FunSuite {
  val XPATH_VERSION_1   = 10
  val XPATH_VERSION_2   = 20
  val XPATH_VERSION_3   = 30
  val XPATH_VERSION_3_1 = 31

  val inDoc = {
    val parser = XMLParserPool.borrowParser
    try {
      parser.newDocument
    } finally {
      XMLParserPool.returnParser(parser)
    }
  }

  val nsContext = ImmutableNamespaceContext(Map[String,String]())

  test ("XPath 1.0 variable should change between differnt evaluations") {
    var varExpression : VarXPathExpression = null
    val expression = "concat('Hello ',$name)" // Good'ol XPath 1.0 concat string
    try {
      varExpression = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_1).asInstanceOf[VarXPathExpression]
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Jorge")) == "Hello Jorge")
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Rachel")) == "Hello Rachel")
    } finally {
      if (varExpression != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_1, varExpression)
    }
  }

  test ("XPath 2.0 variable should change between differnt evaluations") {
    var varExpression : VarXPathExpression = null
    val expression = "upper-case($name)" // upper-case function introduced in 2.0
    try {
      varExpression = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_2).asInstanceOf[VarXPathExpression]
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Jorge")) == "JORGE")
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Rachel")) == "RACHEL")
    } finally {
      if (varExpression != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_2, varExpression)
    }
  }

  test ("XPath 3.0 variable should change between differnt evaluations") {
    var varExpression : VarXPathExpression = null
    val expression = "'Hello ' || $name" // This is how you conact strings in 3.0
    try {
      varExpression = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_3).asInstanceOf[VarXPathExpression]
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Jorge")) == "Hello Jorge")
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING, Map[QName, Object](new QName("name") -> "Rachel")) == "Hello Rachel")
    } finally {
      if (varExpression != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_3, varExpression)
    }
  }

  test ("XPath 3.1 variable should change between differnt evaluations") {
    var varExpression : VarXPathExpression = null
    val expression = "$person?firstName || ' ' || $person?lastName" // 3.1 introduced maps
    try {
      varExpression = XPathExpressionPool.borrowExpression(expression, nsContext, XPATH_VERSION_3_1).asInstanceOf[VarXPathExpression]
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING,
                                      Map[QName, Object](new QName("person") -> mapAsJavaMap(Map[String,String]("firstName"->"Jorge", "lastName"->"Williams")))) == "Jorge Williams")
      assert (varExpression.evaluate (inDoc, XPathConstants.STRING,
                                      Map[QName, Object](new QName("person") -> mapAsJavaMap(Map[String,String]("firstName"->"Rachel", "lastName"->"Kraft")))) == "Rachel Kraft")
    } finally {
      if (varExpression != null) XPathExpressionPool.returnExpression(expression, nsContext, XPATH_VERSION_3_1, varExpression)
    }
  }

}
