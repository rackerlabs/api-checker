package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest

class RequestResponseSuite extends BaseValidatorSuite {

  test ("Ensure wrapper does not parse commas on getHeader") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")));
    val wrap = new CheckerServletRequest(req)
    assert(wrap.getHeader("User-Agent")=="Bla, bla bla")
  }

  test ("Ensure wrapper does not parse commas on getHeaders") {
    val req = request("POST","/foo","application/XML","",false,Map("User-Agent"->List("Bla, bla bla")));
    val wrap = new CheckerServletRequest(req)
    assert(wrap.getHeaders("User-Agent").nextElement()=="Bla, bla bla")
  }

}
