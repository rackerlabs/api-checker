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

import javax.xml.parsers.DocumentBuilder

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class XMLParserPoolSuite extends FunSuite {

  test("The parser pool should successfully create a parser") {
    var builder : DocumentBuilder = null
    try {
      builder = XMLParserPool.borrowParser
      assert (builder != null)
    }finally {
      if (builder != null) XMLParserPool.returnParser(builder)
    }
  }

  test("NumIdle should not be zero soon after returning a parser") {
    var builder : DocumentBuilder = null
    try {
      builder = XMLParserPool.borrowParser
    }finally {
      if (builder != null) XMLParserPool.returnParser(builder)
      assert (XMLParserPool.numIdle != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new parsers") {
    val NUM_INCREASE = 5

    val initActive = XMLParserPool.numActive
    val initIdle   = XMLParserPool.numIdle

    val builders = new Array[DocumentBuilder](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = XMLParserPool.borrowParser
    }

    assert (XMLParserPool.numActive >= initActive+NUM_INCREASE)

    val fullActive = XMLParserPool.numActive

    for (i <- 0 to NUM_INCREASE-1) {
      XMLParserPool.returnParser (builders(i))
    }

    assert (XMLParserPool.numActive <= fullActive-NUM_INCREASE)
  }
}
