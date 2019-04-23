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
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MappedRoles
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.NilMappedRoles

import com.rackspace.com.papi.components.checker.servlet.RequestAttributes.MAP_ROLES
import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatestplus.junit.JUnitRunner

import java.io.InputStreamReader
import java.io.BufferedReader

import org.apache.logging.log4j.Level

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class RequestResponseSuite extends BaseValidatorSuite with LogAssertions {

  test ("Ensure wrapper does not parse commas on getHeader") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")))
    val wrap = new CheckerServletRequest(req)
    assert(wrap.getHeader("User-Agent")=="Bla, bla bla")
  }

  test ("Ensure wrapper does not parse commas on getHeaders") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")))
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
    wrap.addHeader("a", "2")
    wrap.addHeader("b", "2")
    wrap.addHeader("b", "3")

    val headerValuesA = wrap.getHeaders("a").asScala.toSet
    val headerValuesB = wrap.getHeaders("b").asScala.toSet
    assert(headerValuesA.contains("1"))
    assert(headerValuesA.contains("2"))
    assert(headerValuesB.contains("2"))
    assert(headerValuesB.contains("3"))
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

  test("Ensure getDateHeader can parse a supported date format") {
    val req = request("GET","/foo","application/XML","",false,Map("Last-Modified"->List("Wed, 01 Jan 1970 00:00:01 GMT")))
    val wrap = new CheckerServletRequest(req)

    assert(wrap.getDateHeader("Last-Modified")==1000)
  }

  test("Ensure getDateHeader throws an IllegalArgumentException when an unsupported date format is provided") {
    val req = request("GET","/foo","application/XML","",false,Map("Last-Modified"->List("01/01/1970/00/00/00 GMT")))
    val wrap = new CheckerServletRequest(req)

    intercept[IllegalArgumentException] {
      wrap.getDateHeader("Last-Modified")
    }
  }

  test("Ensure getHeaders retrieves the new headers if the original request does not have any") {
    val emptyMap: Map[String, List[String]] = Map()
    val req = request("GET","/foo","application/XML","",false,emptyMap)
    val wrapper = new CheckerServletRequest(req)
    wrapper.addHeader("Foo", "Bar")
    wrapper.addHeader("Foo", "Baz")
    val headers = wrapper.getHeaders("Foo")
    assert(headers.nextElement() == "Bar")
    assert(headers.nextElement() == "Baz")
  }

  test("Ensure getHeaders continues to send null if the original request sends null because of security") {
    val req = request("GET","/foo")
    val wrapper = new CheckerServletRequest(req)
    wrapper.addHeader("Foo", "Bar")
    wrapper.addHeader("Foo", "Baz")
    val headers = wrapper.getHeaders("Foo")
    assert(headers == null)
  }

  test("Ensure getHeaderNames retrieves the header names if the original request does not have any") {
    val emptyMap: Map[String, List[String]] = Map()
    val req = request("GET","/foo","application/XML","",false,emptyMap)
    val wrapper = new CheckerServletRequest(req)
    wrapper.addHeader("Foo", "Bar")
    wrapper.addHeader("Foo", "Baz")
    wrapper.addHeader("Moo", "Baz")
    val headers = wrapper.getHeaderNames
    assert(headers.nextElement() == "foo")
    assert(headers.nextElement() == "moo")
  }

  test("Ensure getHeaderNames continues to send null if the original request sends null because of security") {
    val req = request("GET","/foo")
    val wrapper = new CheckerServletRequest(req)
    wrapper.addHeader("Foo", "Bar")
    wrapper.addHeader("Foo", "Baz")
    wrapper.addHeader("Moo", "Baz")
    val headers = wrapper.getHeaderNames
    assert(headers == null)
  }

  test("Ensure that MAP_ROLES_HEADER is correctly parsed") {
    val headerValue = b64Encode("""
      {
         "tenant1" : ["admin","foo","bar"],
         "tenant2" : ["admin"],
         "tenant3" : ["foo", "bar", "biz", "booz"]
      }
    """)
    val req = request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(headerValue)))
    val wrap = new CheckerServletRequest(req)

    def mappedAsserts (mr : MappedRoles) {
      assert(mr.size == 3)
      assert(mr("tenant1").size == 3)
      assert(mr("tenant2").size == 1)
      assert(mr("tenant3").size == 4)
      List("admin","foo","bar").foreach (r => assert(mr("tenant1").contains(r)))
      List("admin").foreach (r  => assert(mr("tenant2").contains(r)))
      List("foo","bar","biz","booz").foreach(r => assert(mr("tenant3").contains(r)))
    }

    //
    //  Assertions should hold when calling mappedRoles directly.
    //
    mappedAsserts(wrap.mappedRoles)

    //
    //  After the first call they should hold when retrieving the
    //  request parameter.
    //
    mappedAsserts(wrap.getAttribute(MAP_ROLES).asInstanceOf[MappedRoles])
  }

  test("Ensure we correctly handle missing MAP_ROLES_HEADER") {
    val req = request("GET","/foo", "application/XML", "", false, Map("foo"->List("bar")))
    val wrap = new CheckerServletRequest(req)

    //
    // Should get an empty map.
    //
    assert(wrap.mappedRoles.isEmpty)

    //
    //  After the first call, request param should also contain an empty map
    //
    assert(wrap.getAttribute(MAP_ROLES).asInstanceOf[MappedRoles].isEmpty)
  }

  test("Ensure we correctly handle null MAP_ROLES_HEADER, this should not be flagged as an error") {
    val headerValue = b64Encode("null")
    val req = request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(headerValue)))
    val wrap = new CheckerServletRequest(req)

    //
    // Should get an empty map.
    //
    val mapLog = log(Level.ERROR) {
      assert(wrap.mappedRoles.isEmpty)
    }

    //
    //  After the first call, request param should also contain an empty map
    //
    assert(wrap.getAttribute(MAP_ROLES).asInstanceOf[MappedRoles].isEmpty)

    //
    //  We should not have gotten an error message
    //
    assertEmpty(mapLog)
  }

  //
  //  Improper MAP_ROLE headers sholud be correctly handled.
  //
  val badMapHeaders = Map("Improper JSON"->b64Encode("""
      {
         "tenant1" : ["admin","foo","bar"],
         "tenant2" : ["admin",
         "tenant3" : ["foo", "bar", "biz", "booz"]
      }
    """), "Bad JSON type (array)"-> b64Encode("""
     ["admin","foo","bar"]
    """), "Bad JSON type (int)"-> b64Encode("""
     42
    """), "Bad JSON type (string)"-> b64Encode("""
     "admin"
    """), "Non encoded data"-> """
      {
         "tenant1" : ["admin","foo","bar"],
         "tenant2" : ["admin"],
         "tenant3" : ["foo", "bar", "biz", "booz"]
      }
    """
  )

  for ((desc, headerValue) <- badMapHeaders) {
    test(s"If the MAP_ROLES_HEADER contains $desc  an error should be logged and an empty map should be set") {
      val req = request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(headerValue)))
      val wrap = new CheckerServletRequest(req)

      //
      // Should get an empty map.
      //
      val mapLog = log(Level.ERROR) {
        assert(wrap.mappedRoles.isEmpty)
      }

      //
      //  After the first call, request param should also contain an empty map
      //
      assert(wrap.getAttribute(MAP_ROLES).asInstanceOf[MappedRoles].isEmpty)

      //
      //  Assert that the error log contains the correct message.
      //
      assert(mapLog, s"$MAP_ROLES_HEADER could not be parsed.  Ignoring map roles!")
    }
  }

  test("Ensure that cached XML is invalidated if getInputStream is called") {
    val req = request("POST", "/foo", "application/xml", "<foo />", true)
    val wrapper = new CheckerServletRequest(req)

    //
    //  First we assert that XML has been cached correctly...
    //
    assert(wrapper.parsedXML != null)

    //
    //  We assert it's actually the XML we input
    //
    assert(wrapper.parsedXML.getElementsByTagName("foo").getLength() == 1)

    //
    //  We now call getInputStream on the wrapper, and ensure we can
    //  serialize the XML.
    //
    val builder = new StringBuilder
    val reader = new BufferedReader( new InputStreamReader(wrapper.getInputStream()))
    var read : String = null
    do {
      read = reader.readLine()
      if (read != null) {
        builder.append(read)
      }
    } while (read != null)
    reader.close()
    assert(builder.toString.contains("foo"))

    //
    // Finally, assert we killed the cached XML cus we called getInputStream
    //
    assert(wrapper.parsedXML == null)
  }

  test("Ensure that cached JSON is invalidated if getInputStream is called") {
    val req = request("POST", "/foo", "application/json", "{\"foo\" : \"bar\"}", true);
    val wrapper = new CheckerServletRequest(req)

    //
    // First we assert that the JSON has been cached correctly...
    //
    assert(wrapper.parsedJSON != null)

    //
    // We assert it's actually the JSON we input
    //
    assert(wrapper.parsedJSON.findValue("foo").asText() == "bar")

    //
    //  We now call getInputStream on the wrapper, and ensure we can
    //  serialize the JSON.
    //
    val builder = new StringBuilder
    val reader = new BufferedReader( new InputStreamReader(wrapper.getInputStream()))
    var read : String = null
    do {
      read = reader.readLine()
      if (read != null) {
        builder.append(read)
      }
    } while (read != null)
    reader.close()
    assert(builder.toString.contains("foo"))

    //
    //  Finally, assert we killed the cached JSON cus we called getInputStream
    //
    assert(wrapper.parsedJSON == null)
  }
}
