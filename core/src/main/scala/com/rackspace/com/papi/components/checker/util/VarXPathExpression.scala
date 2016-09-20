/***
 *   Copyright 2016 Rackspace US, Inc.
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

import javax.xml.namespace.QName
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathVariableResolver

import org.xml.sax.InputSource

object VarXPathExpression {
  def compile (xpath : XPath, expression : String,
               vars : Map[QName, Object] = Map[QName, Object]()) : VarXPathExpression
  = new VarXPathExpression(xpath, expression, vars)
}


class VarXPathExpression private (private val xpath : XPath, private val expression : String,
                                  var vars : Map[QName, Object]) extends XPathExpression with XPathVariableResolver {

  val xpathExpression : XPathExpression = {
    xpath.setXPathVariableResolver(this)
    xpath.compile(expression)
  }

  //
  // XPathExpresion Impl
  //
  override def evaluate (source : InputSource) : String = xpathExpression.evaluate(source)
  override def evaluate (source : InputSource, returnType : QName) : Object = xpathExpression.evaluate(source, returnType)
  override def evaluate (source : Object) : String = xpathExpression.evaluate(source)
  override def evaluate (source : Object, returnType : QName) : Object = xpathExpression.evaluate(source, returnType)

  //
  // XPathVariableResolver Impl
  //
  override def resolveVariable (variableName : QName) : Object = vars.getOrElse(variableName, null)

  //
  //  Extended evaluate calls with variables set
  //
  def evaluate (source : InputSource, vars : Map[QName, Object]) : String = {
    this.vars = vars
    xpathExpression.evaluate(source)
  }

  def evaluate (source : InputSource, returnType : QName, vars : Map[QName, Object]) : Object = {
    this.vars = vars
    xpathExpression.evaluate(source, returnType)
  }

  def evaluate (source : Object, vars : Map[QName, Object]) : String = {
    this.vars = vars
    xpathExpression.evaluate(source)
  }

  def evaluate (source : Object, returnType : QName, vars : Map[QName, Object]) : Object = {
    this.vars = vars
    xpathExpression.evaluate(source, returnType)
  }

}
