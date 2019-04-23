/***
 *   Copyright 2017 Rackspace US, Inc.
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

package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.servlet.HttpServletRequestMap
import com.rackspace.com.papi.components.checker.util.VarXPathExpression
import com.rackspace.com.papi.components.checker.util.XPathExpressionPool._
import com.rackspace.com.papi.components.checker.util.XMLParserPool._
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext

import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatestplus.junit.JUnitRunner

import java.io.InputStreamReader
import java.io.BufferedReader

import javax.xml.namespace.QName
import javax.xml.xpath.XPathConstants

import net.sf.saxon.s9api.XdmMap

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class HttpServletRequestMapSuite extends BaseValidatorSuite {
  val XPATH_VERSION = 31
  val REQUEST_VAR = new QName("request")
  val defaultContext = ImmutableNamespaceContext(Map[String,String]())
  val emptyDoc = {
    val parser = borrowParser
    try {
      parser.newDocument
    } finally {
      if (parser != null) returnParser(parser)
    }
  }


  test("Method should be accesible in XPath") {
    val req1 = XdmMap.makeMap(new HttpServletRequestMap(request("GET","/")))
    val req2 = XdmMap.makeMap(new HttpServletRequestMap(request("DELETE","/")))
    val req3 = XdmMap.makeMap(new HttpServletRequestMap(request("PUT", "/")))

    val xpath1 = borrowExpression("$request?method='GET'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]
    val xpath2 = borrowExpression("$request?method='DELETE'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]
    val xpath3 = borrowExpression("$request?method='PUT'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]

    try {
      //
      //  Only GET is set in request 1
      //
      assert(xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])
      assert(!xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])


      //
      //  Only DELETE is set in request 2
      //
      assert(xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])
      assert(!xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])


      //
      //  Only PUT is set in request 3
      //
      assert(xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])
      assert(!xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])

    } finally {
      if (xpath1 != null) returnExpression("$request?method='GET'", defaultContext, XPATH_VERSION, xpath1)
      if (xpath2 != null) returnExpression("$request?method='DELETE'", defaultContext, XPATH_VERSION, xpath2)
      if (xpath3 != null) returnExpression("$request?method='PUT'", defaultContext, XPATH_VERSION, xpath3)
    }
  }

  test("URI should be accessible in XPath") {
    val req1 = XdmMap.makeMap(new HttpServletRequestMap(request("GET","/path/to/foo")))
    val req2 = XdmMap.makeMap(new HttpServletRequestMap(request("DELETE","/path/to/bar")))
    val req3 = XdmMap.makeMap(new HttpServletRequestMap(request("PUT", "/yet/another/path.txt")))


    val xpath1 = borrowExpression("$request?uri='/path/to/foo'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]
    val xpath2 = borrowExpression("$request?uri='/path/to/bar'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]
    val xpath3 = borrowExpression("$request?uri='/yet/another/path.txt'", defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]

    try {
      //
      //  URI shoud be /path/to/foo only in req1
      //
      assert(xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])
      assert(!xpath1.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])


      //
      //  URI should be /path/to/bar only in req 2
      //
      assert(xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])
      assert(!xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath2.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])


      //
      // URI should be /yet/another/path.txt only in req 3
      //
      assert(xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])
      assert(!xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])
      assert(!xpath3.evaluate (emptyDoc, XPathConstants.BOOLEAN,Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])

    } finally {
      if (xpath1 != null) returnExpression("$request?uri='/path/to/foo'", defaultContext, XPATH_VERSION, xpath1)
      if (xpath2 != null) returnExpression("$request?uri='/path/to/bar'", defaultContext, XPATH_VERSION, xpath2)
      if (xpath3 != null) returnExpression("$request?uri='/yet/another/path.txt'", defaultContext, XPATH_VERSION, xpath3)
    }
  }

  def getVarPath (xpath : String, retType : QName, vars : Map[QName,Object]) : Object = {
    val xpathExp = borrowExpression(xpath, defaultContext, XPATH_VERSION).asInstanceOf[VarXPathExpression]
    try {
      xpathExp.evaluate (emptyDoc, retType, vars)
    } finally {
      if (xpathExp != null) returnExpression(xpath, defaultContext, XPATH_VERSION, xpathExp)
    }
  }

  test("Headers should be accessible, though in a (lower) case sensitive manner") {
    var req1 = XdmMap.makeMap(new HttpServletRequestMap(request("GET", "/path/to/foo", "application/json", "null", false,
                                                 Map[String,List[String]]("foo"->List("bar"),
                                                                          "bIz"->List("baz","boom","bit"),
                                                                          "x"->List("y")))))
    var req2 = XdmMap.makeMap(new HttpServletRequestMap(request("DELETE", "/path/to/bar", "application/json", "null", false,
                                                 Map[String,List[String]]("FoO"->List("baz")))))

    var req3 = XdmMap.makeMap(new HttpServletRequestMap(request("PUT", "/yet/another/path.txt", "application/json", "null", false,
                                                 Map[String,List[String]]())))

    var req4 = XdmMap.makeMap(new HttpServletRequestMap(request("POST", "/no/headers/allowed")))


    assert (getVarPath ("""
                         (count($request?headers?foo) = 1) and
                         (count($request?headers?biz) = 3) and
                         (count($request?headers?x) = 1)
                        """, XPathConstants.BOOLEAN, Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])

    assert (getVarPath ("""
                         (every $v in $request?headers?foo satisfies $v='bar') and
                         (every $v in $request?headers?biz satisfies $v=('baz','boom','bit')) and
                         (every $v in $request?headers?x satisfies $v='y')
                        """, XPathConstants.BOOLEAN, Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])

    assert(getVarPath("count($request?headers?foo) = 1",
                      XPathConstants.BOOLEAN, Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])

    assert(getVarPath("$request?headers?foo = 'baz'",
                      XPathConstants.BOOLEAN, Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])

    assert(getVarPath("empty($request?headers?*)",
                       XPathConstants.BOOLEAN, Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])

    assert(getVarPath("empty($request?headers?*)",
                       XPathConstants.BOOLEAN, Map(REQUEST_VAR->req4)).asInstanceOf[Boolean])


  }

  test("Mix access method, uri, headers") {
    var req1 = XdmMap.makeMap(new HttpServletRequestMap(request("GET", "/path/to/foo", "application/json", "null", false,
                                                 Map[String,List[String]]("foo"->List("bar"),
                                                                          "bIz"->List("baz","boom","bit"),
                                                                          "x"->List("y")))))
    var req2 = XdmMap.makeMap(new HttpServletRequestMap(request("DELETE", "/path/to/bar", "application/json", "null", false,
                                                 Map[String,List[String]]("FoO"->List("baz")))))

    var req3 = XdmMap.makeMap(new HttpServletRequestMap(request("PUT", "/yet/another/path.txt", "application/json", "null", false,
                                                 Map[String,List[String]]())))

    assert(getVarPath("""($request?method='GET') and ($request?uri='/path/to/foo') and ($request?headers?x = 'y')""",
                      XPathConstants.BOOLEAN, Map(REQUEST_VAR->req1)).asInstanceOf[Boolean])

    assert(getVarPath("""($request?method='DELETE') and ($request?uri='/path/to/bar') and ($request?headers?foo = 'baz')""",
                      XPathConstants.BOOLEAN, Map(REQUEST_VAR->req2)).asInstanceOf[Boolean])

    assert(getVarPath("""($request?method='PUT') and ($request?uri='/yet/another/path.txt') and (empty($request?headers?*))""",
                      XPathConstants.BOOLEAN, Map(REQUEST_VAR->req3)).asInstanceOf[Boolean])

  }
}
