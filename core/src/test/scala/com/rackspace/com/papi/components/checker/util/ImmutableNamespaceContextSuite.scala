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

import javax.xml.XMLConstants._

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._
import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class ImmutableNamespaceContextSuite extends FunSuite {

  test("xmlns and xml prefixes should always be defined") {
    val ns = Map[String,String]()
    val context = ImmutableNamespaceContext(ns)

    //
    //  Check prefix
    //
    assert (context.getNamespaceURI("xml") == "http://www.w3.org/XML/1998/namespace")
    assert (context.getNamespaceURI("xmlns") == "http://www.w3.org/2000/xmlns/")

    //
    //  Check URIs
    //
    assert(context.getPrefix("http://www.w3.org/XML/1998/namespace") == "xml")
    assert(context.getPrefix("http://www.w3.org/2000/xmlns/") == "xmlns")

    //
    //  Check prefixes
    //
    context.getPrefixes ("http://www.w3.org/XML/1998/namespace").foreach (p => assert(p == "xml"))
    context.getPrefixes ("http://www.w3.org/2000/xmlns/").foreach (p => assert(p == "xmlns"))
  }

  test("Namespaces should be maintained") {
    val ns = Map("tst" -> "http://test.org/test",
                 "ns2" -> "http://test.org/test/ns1")
    val context = ImmutableNamespaceContext(ns)

    //
    //  Check prefix
    //
    assert (context.getNamespaceURI("tst") == "http://test.org/test")
    assert (context.getNamespaceURI("ns2") == "http://test.org/test/ns1")

    //
    //  Check URIs
    //
    assert(context.getPrefix("http://test.org/test") == "tst")
    assert(context.getPrefix("http://test.org/test/ns1") == "ns2")

    //
    //  Check prefixes
    //
    context.getPrefixes ("http://test.org/test").foreach (p => assert(p == "tst"))
    context.getPrefixes ("http://test.org/test/ns1").foreach (p => assert(p == "ns2"))
  }

  test("Namespaces should be maintained, even if the mutable map changes") {
    val ns = Map("tst" -> "http://test.org/test",
                 "ns2" -> "http://test.org/test/ns1")
    val context = ImmutableNamespaceContext(ns)

    ns += ("tst" -> "http://bla")
    ns += ("ns2" -> "http://bla/bla")

    assert (ns("tst") == "http://bla")
    assert (ns("ns2") == "http://bla/bla")

    //
    //  Check prefix
    //
    assert (context.getNamespaceURI("tst") == "http://test.org/test")
    assert (context.getNamespaceURI("ns2") == "http://test.org/test/ns1")

    //
    //  Check URIs
    //
    assert(context.getPrefix("http://test.org/test") == "tst")
    assert(context.getPrefix("http://test.org/test/ns1") == "ns2")

    //
    //  Check prefixes
    //
    context.getPrefixes ("http://test.org/test").foreach (p => assert(p == "tst"))
    context.getPrefixes ("http://test.org/test/ns1").foreach (p => assert(p == "ns2"))
  }

  test("Mulitple prefixes may be assigned to a namespace URI") {
    val ns = Map("tst" -> "http://test.org/test",
                 "TST" -> "http://test.org/test")
    val context = ImmutableNamespaceContext(ns)

    //
    //  Check prefix
    //
    assert (context.getNamespaceURI("tst") == "http://test.org/test")
    assert (context.getNamespaceURI("TST") == "http://test.org/test")

    //
    //  At least one of the prefixes should be returned.
    //
    val prefix = context.getPrefix("http://test.org/test")
    assert((prefix == "tst") || (prefix == "TST"))

    //
    //  Both prefixs should be queriable
    //
    val prefixes = context.getPrefixes("http://test.org/test").toSet
    assert (prefixes.contains("tst"))
    assert (prefixes.contains("TST"))
    assert (prefixes.size == 2)
  }

  test("Null input should throw an IllegalArgumentException") {
    val ns = Map[String,String]()
    val context = ImmutableNamespaceContext(ns)

    intercept[IllegalArgumentException] {
      context.getNamespaceURI(null)
    }

    intercept[IllegalArgumentException] {
      context.getPrefix(null)
    }

    intercept[IllegalArgumentException] {
      context.getPrefixes(null)
    }
  }

  test("Unbound prefixes and URIs") {
    val ns = Map[String,String]()
    val context = ImmutableNamespaceContext(ns)

    assert (context.getNamespaceURI("mis") == NULL_NS_URI)
    assert (context.getPrefix("http://test.org/missing") == null)

    val prefixes = context.getPrefixes("http://test.org/missing").toSet
    assert (prefixes.size == 0)
  }
}
