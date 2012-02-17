package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._

import org.scalatest.FunSuite

import org.mockito.Mockito._

class BaseValidatorSuite extends FunSuite {

  def request(method : String, url : String) : HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])

    when(req.getMethod()).thenReturn(method)
    when(req.getRequestURI()).thenReturn(url)
    return req
  }

  def response : HttpServletResponse = mock(classOf[HttpServletResponse]);

}
