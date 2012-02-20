package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.step.Step
import com.rackspace.com.papi.components.checker.step.Result

import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.handler.NullHandler

import com.rackspace.com.papi.components.checker.servlet._

class ValidatorException(msg : String, cause : Throwable) extends Throwable(msg, cause) {}

class Validator(val startStep : Step, val resultHandler : ResultHandler = new NullHandler) {
  def validate (req : HttpServletRequest, res : HttpServletResponse) : Result = {
    try {
      val creq = new CheckerServletRequest (req)
      val cres = new CheckerServletResponse(res)
      val result = startStep.check (creq, cres, 0).get
      resultHandler.handle(creq, cres, result)
      result
    } catch {
      case v : ValidatorException => throw v
      case e => throw new ValidatorException("Error while validating request", e)
    }
  }
}
