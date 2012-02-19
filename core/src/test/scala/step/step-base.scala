package com.rackspace.com.papi.components.checker.step

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.BaseValidatorSuite

import org.mockito.Mockito._

class BaseStepSuite extends BaseValidatorSuite {

  override def request (method : String, url : String) : CheckerServletRequest = {
    new CheckerServletRequest (super.request(method, url))
  }

  override def response  : CheckerServletResponse = {
    new CheckerServletResponse (super.response)
  }
}
