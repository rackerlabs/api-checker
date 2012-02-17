package com.rackspace.com.papi.components.checker

import java.util.Date
import java.math.BigInteger
import scala.util.Random

import com.rackspace.com.papi.components.checker.step._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorSuite extends BaseValidatorSuite {
  //
  // validator_EMPTY does not allow ANY HTTP requests. The validator
  // is used in the following tests.
  //
  val validator_EMPTY = new Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val start = new Start("START", "Start", Array(urlFail, methodFail))
    start
  }, assertHandler)

  test ("GET on / should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/"),response))
  }

  test ("an empty GET should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET",""),response))
  }

  test ("GET on /a should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a"),response))
  }

  test ("POST on /a/b/c/hello/there should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a/b/c/hello/there"),response))
  }

  test ("a completely empty request should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("",""),response))
  }

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

  test ("GET on /a/b should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b"),response)
  }

  test ("GET on /a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b/"),response)
  }

  test ("GET on a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","a/b/"),response)
  }

  test ("GET on / should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/"),response))
  }

  test ("an empty GET should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET",""),response))
  }

  test ("GET on /a should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a"),response))
  }

  test ("GET on /a/b/c/d should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a/b/c/d"),response))
  }

  test ("POST on /a/b should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("POST","/a/b"),response))
  }

  test ("GET on /index.html should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/index.html"),response))
  }

  //
  // validator_REG1 allows a GET on /a/\-*?[0-9]+/c. That is, the 2nd
  // URI component MUST be a (possibly negative) integer of
  // unspecified size. The validator is used in the following tests.
  //
  val validator_REG1 = new Validator({
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val c = new URI("c","c", "c".r, Array(get, urlFail, methodFail))
    val digit = new URI("digit","digit", """\-*[0-9]+""".r, Array(c, urlFail, methodFail))
    val a = new URI("a","a", "a".r, Array(digit, urlFail, methodFail))
    val start = new Start("START", "Start", Array(a, urlFail, methodFail))
    start
  }, assertHandler)

  test ("GET on /a/7/c should succeed on validator_REG1") {
    validator_REG1.validate(request("GET","/a/7/c"),response)
  }

  test ("GET on /a/-7/c should succeed on validator_REG1") {
    validator_REG1.validate(request("GET","/a/-7/c"),response)
  }

  test ("GET on /a/<randomLong>/c should succeed on validator_REG1") {
    val rl = new Random(new Date().getTime()).nextLong()
    validator_REG1.validate(request("GET","/a/"+rl+"/c"),response)
  }

  test ("GET on /a/<bigInt>/c should succeed on validator_REG1") {
    val bi = new BigInteger(1024, new Random(new Date().getTime()).self)
    validator_REG1.validate(request("GET","/a/"+bi+"/c"),response)
  }

  test ("GET on /a/<randomDouble>/c should fail validator_REG1") {
    val rf = new Random(new Date().getTime()).nextDouble()
    assertResultFailed(validator_REG1.validate(request("GET","/a/"+rf+"/c"),response))
  }

  test ("GET on /a//c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a//c"),response))
  }

  test ("GET on /a/b/c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a/b/c"),response))
  }

  test ("GET on /a should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a"),response))
  }

  test ("GET on /a/+7/c should fail validator_REG1") {
    assertResultFailed(validator_REG1.validate(request("GET","/a/+7/c"),response))
  }
}
