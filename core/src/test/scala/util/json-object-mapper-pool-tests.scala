package com.rackspace.com.papi.components.checker.util

import com.fasterxml.jackson.databind.ObjectMapper

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class JSONObjectMapperPoolSuite extends FunSuite {

  test("The parser pool should successfully create a parser") {
    var parser : ObjectMapper = null
    try {
      parser = ObjectMapperPool.borrowParser
      assert (parser != null)
    }finally {
      if (parser != null) ObjectMapperPool.returnParser(parser)
    }
  }
}
