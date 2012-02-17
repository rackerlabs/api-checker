package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

import org.mockito.Mockito._


@RunWith(classOf[JUnitRunner])
class ValidatorSuite extends BaseValidatorSuite {

  def simpleAB : Step = {
    val accept = new Accept("A0", "Accept")
    val urlFail = new URLFail("UF", "URLFail")
    val methodFail = new MethodFail ("MF", "MethodFail")
    val get = new Method("GET", "GET", "GET".r, Array (accept))
    val b = new URI("b","b", "b".r, Array(get, urlFail, methodFail))
    val a = new URI("a","a", "a".r, Array(b, urlFail, methodFail))
    val start = new Start("START", "Start", Array(a, urlFail, methodFail))
    start
  }

  test ("validate a simple GET on a/b") {
    val validator = new Validator(simpleAB, new ConsoleResultHandler())
    validator.validate(request("GET","/a/b"),response)
    validator.validate(request("GET","/a"),response)
    validator.validate(request("GET","/a/b/c/d"),response)
    validator.validate(request("POST", "/a/b"),response)
    validator.validate(request("GET", "/index.html"), response)
  }
}
