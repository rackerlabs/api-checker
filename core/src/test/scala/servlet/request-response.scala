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
package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import org.mockito.Mockito.when

import scala.collection.JavaConverters._

class RequestResponseSuite extends BaseValidatorSuite {

  test ("Ensure wrapper does not parse commas on getHeader") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")));
    val wrap = new CheckerServletRequest(req)
    assert(wrap.getHeader("User-Agent")=="Bla, bla bla")
  }

  test ("Ensure wrapper does not parse commas on getHeaders") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")));
    val wrap = new CheckerServletRequest(req)
    assert(wrap.getHeaders("User-Agent").nextElement()=="Bla, bla bla")
  }

  test("Ensure headers added via addHeader are accessible via getHeader") {
    val req = request("GET","/foo","application/XML","",false,Map("a"->List("1")))
    val wrap = new CheckerServletRequest(req)
    wrap.addHeader("b", "2")
    assert(wrap.getHeader("b")=="2")
  }

  test("Ensure headers added via addHeader are accessible via getHeader in a case-insensitive manner") {
    val req = request("GET","/foo","application/XML","",false,Map("a"->List("1")))
    val wrap = new CheckerServletRequest(req)
    wrap.addHeader("b", "2")
    assert(wrap.getHeader("B")=="2")
  }

  test("Ensure headers added via addHeader are accessible via getHeaders") {
    val req = request("GET","/foo","application/XML","",false,Map("a"->List("1")))
    val wrap = new CheckerServletRequest(req)
    wrap.addHeader("b", "2")
    wrap.addHeader("b", "3")

    val headerValues = wrap.getHeaders("b").asScala.toSet
    assert(headerValues.contains("2"))
    assert(headerValues.contains("3"))
  }

  test("Ensure getHeaderNames returns all header names") {
    val req = request("GET","/foo","application/XML","",false,Map("a"->List("1")))
    when(req.getHeaderNames).thenReturn(Iterator("a").asJavaEnumeration)

    val wrap = new CheckerServletRequest(req)
    wrap.addHeader("b", "2")

    val headerNameSet = wrap.getHeaderNames.asScala.toSet
    assert(headerNameSet.contains("a"))
    assert(headerNameSet.contains("b"))
  }

  test("Ensure getIntHeader returns the Int value of a header value") {
    val req = request("GET","/foo","application/XML","",false,Map("a"->List("1")))
    when(req.getIntHeader("a")).thenReturn(1)

    val wrap = new CheckerServletRequest(req)
    wrap.addHeader("b", "2")

    assert(wrap.getIntHeader("a")==1)
    assert(wrap.getIntHeader("b")==2)
  }
}
