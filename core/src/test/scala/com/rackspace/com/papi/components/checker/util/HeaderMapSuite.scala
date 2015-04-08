/***
 *   Copyright 2015 Rackspace US, Inc.
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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HeaderMapSuite extends FunSuite {
  test("HeaderMaps sholud be case insensitive [addHeader]"){
    val map = new HeaderMap().addHeader("foo","bar").addHeader("baz","biz")

    assert(map("foo") == List("bar"))
    assert(map("Foo") == List("bar"))
    assert(map("fOo") == List("bar"))
    assert(map("foO") == List("bar"))
    assert(map("FOO") == List("bar"))
    assert(map("baz") == List("biz"))
    assert(map("baZ") == List("biz"))
    assert(map("bAZ") == List("biz"))
    assert(map("bAz") == List("biz"))
  }

  test("HeaderMaps list of values should be preserved [addHeaders]") {
    val map = new HeaderMap().addHeaders("foo", List("bar","Bar", "BAZ"))

    assert(map("foo") == List("bar","Bar", "BAZ"))
    assert(map("foo") != List("Bar","bar", "BAZ"))
    assert(map("foo") != List("BAZ","Bar", "bar"))
    assert(map("foo") != List("bar"))
  }

  test("HeaderMaps list of values should be preserved [addHeaders, get]") {
    val map = new HeaderMap().addHeaders("foo", List("bar","Bar", "BAZ"))

    assert(map.get("foo").get == List("bar", "Bar", "BAZ"))
    assert(map.get("bar") == None)
    assert(map.get("baz") == None)
  }

  test("Headers may be combined [addHeaders]") {
    val map1 = new HeaderMap().addHeader("foo", "bar")
    val map2 = new HeaderMap().addHeader("baz", "biz")
    val map  = map1.addHeaders(map2)

    assert(map("foo") == List("bar"))
    assert(map("Foo") == List("bar"))
    assert(map("fOo") == List("bar"))
    assert(map("foO") == List("bar"))
    assert(map("FOO") == List("bar"))
    assert(map("baz") == List("biz"))
    assert(map("baZ") == List("biz"))
    assert(map("bAZ") == List("biz"))
    assert(map("bAz") == List("biz"))
  }

  test("Headers may be combined [addHeaders, iterator]") {
    val map1 = new HeaderMap().addHeader("foo", "bar")
    val map2 = new HeaderMap().addHeader("baz", "biz")
    val map  = map1.addHeaders(map2)

    map.foreach(m => m._1 match {
      case "foo" => assert (m._2 == List("bar"))
      case "baz" => assert (m._2 == List("biz"))
    })
  }

  test("Headers may be combined [+]") {
    val map1 : Map[String, List[String]] = new HeaderMap() + ("foo" -> List("bar"))
    val map : Map[String, List[String]] = map1 + ("baz" -> List("biz"))

    assert(map("foo") == List("bar"))
    assert(map("Foo") == List("bar"))
    assert(map("fOo") == List("bar"))
    assert(map("foO") == List("bar"))
    assert(map("FOO") == List("bar"))
    assert(map("baz") == List("biz"))
    assert(map("baZ") == List("biz"))
    assert(map("bAZ") == List("biz"))
    assert(map("bAz") == List("biz"))
  }

  test("A header value may be removed [removeHeader]") {
    val map1 = new HeaderMap().addHeaders("foo", List("bar","Bar", "BAZ"))
    val map = map1.removeHeader("foo", "BAZ")

    assert(map("foo") == List("bar","Bar"))
    assert(map("foo") != List("Bar","bar"))
    assert(map("foo") != List("bar","Bar", "BAZ"))
    assert(map("foo") != List("Bar","bar", "BAZ"))
    assert(map("foo") != List("BAZ","Bar", "bar"))
    assert(map("foo") != List("bar"))
  }

  test("A header value may be removed [removeHeader, single value]") {
    val map1 = new HeaderMap().addHeaders("foo", List("bar"))
    val map = map1.removeHeader("foo", "bar")

    intercept[NoSuchElementException] {
      map("foo")
    }
  }

  test("A header may be removed [removeHeaders]") {
    val map1 = new HeaderMap().addHeaders("foo", List("bar","Bar", "BAZ"))
    val map = map1.removeHeaders("foo")

    intercept[NoSuchElementException] {
      map("foo")
    }
  }

  test("A header may be removed [-]") {
    val map1 = new HeaderMap().addHeaders("foo", List("bar","Bar", "BAZ"))
    val map = map1 - "foo"

    intercept[NoSuchElementException] {
      map("foo")
    }
  }
}
