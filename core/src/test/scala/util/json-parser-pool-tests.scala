package com.rackspace.com.papi.components.checker.util

import org.json.simple.parser.JSONParser

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class JSONParserPoolSuite extends FunSuite {

  test("The parser pool should successfully create a parser") {
    var parser : JSONParser = null
    try {
      parser = JSONParserPool.borrowParser
      assert (parser != null)
    }finally {
      if (parser != null) JSONParserPool.returnParser(parser)
    }
  }

  test("NumIdle should not be zero soon after returning a parser") {
    var parser : JSONParser = null
    try {
      parser = JSONParserPool.borrowParser
    }finally {
      if (parser != null) JSONParserPool.returnParser(parser)
      assert (JSONParserPool.numIdle != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new parsers") {
    val NUM_INCREASE = 5

    val initActive = JSONParserPool.numActive
    val initIdle   = JSONParserPool.numIdle

    val parsers = new Array[JSONParser](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      parsers(i) = JSONParserPool.borrowParser
    }

    assert (JSONParserPool.numActive >= initActive+NUM_INCREASE)

    val fullActive = JSONParserPool.numActive

    for (i <- 0 to NUM_INCREASE-1) {
      JSONParserPool.returnParser (parsers(i))
    }

    assert (JSONParserPool.numActive <= fullActive-NUM_INCREASE)
  }
}
