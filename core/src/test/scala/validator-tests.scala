package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.step._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorSuite extends BaseValidatorSuite {
  //
  // validator_AB allows a GET on /a/b. The validator is used in the
  // following tests.
  //
  val validator_AB = new Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val b = new URI("b","b", "b".r, Array(get, urlFail, methodFail))
    val a = new URI("a","a", "a".r, Array(b, urlFail, methodFail))
    val start = new Start("START", "Start", Array(a, urlFail, methodFail))
    start
  }, assertHandler)

  test ("GET on /a/b should succeed") {
    validator_AB.validate(request("GET","/a/b"),response)
  }

  test ("GET on /a/b/ should succeed") {
    validator_AB.validate(request("GET","/a/b/"),response)
  }

  test ("GET on a/b/ should succeed") {
    validator_AB.validate(request("GET","a/b/"),response)
  }

  test ("GET on / should fail") {
    assertResultFailed(validator_AB.validate(request("GET","/"),response))
  }

  test ("an empty GET should fail") {
    assertResultFailed(validator_AB.validate(request("GET",""),response))
  }

  test ("GET on /a should fail") {
    assertResultFailed(validator_AB.validate(request("GET","/a"),response))
  }

  test ("GET on /a/b/c/d should fail") {
    assertResultFailed(validator_AB.validate(request("GET","/a/b/c/d"),response))
  }

  test ("POST on /a/b should fail") {
    assertResultFailed(validator_AB.validate(request("POST","/a/b"),response))
  }

  test ("GET on /index.html should fail") {
    assertResultFailed(validator_AB.validate(request("GET","/index.html"),response))
  }

}
