package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.ErrorResult
import com.rackspace.com.papi.components.checker.step.MultiFailResult

import javax.servlet.FilterChain

import org.w3c.dom.Document

class ServletResultHandler extends ResultHandler {
  def init (checker : Option[Document]) : Unit = {}

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    if (!result.valid) {
      result match {
        case mrf : MultiFailResult =>
          val re = mrf.reduce.get.asInstanceOf[ErrorResult]
          resp.sendError(re.code, re.message)
        case errorResult : ErrorResult => resp.sendError(errorResult.code, errorResult.message)
      }
    }
  }
}
