package com.rackspace.com.papi.components.checker.step

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StepSuite extends BaseStepSuite {

  test("Method should return method fail result if the URI level has been exceeded") {
    val mf  = new MethodFail("mf", "mf")
    val res = mf.check (request("GET", "/a/b"), response, 2)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MethodFailResult])
    val res2 = mf.check (request("GET", "/a/b"), response, 3)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[MethodFailResult])
  }

  test("Method should return None when URI level is not exceeded") {
    val mf  = new MethodFail("mf", "mf")
    val res = mf.check (request("GET", "/a/b"), response, 0)
    assert (res == None)
    val res2 = mf.check (request("GET", "/a/b"), response, 1)
    assert (res2 == None)
  }

}
