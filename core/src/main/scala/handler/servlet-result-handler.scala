package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.step.ErrorResult
import com.rackspace.com.papi.components.checker.step.MultiFailResult

import scala.collection.JavaConversions._

import javax.servlet.FilterChain

import org.w3c.dom.Document

class ServletResultHandler extends ResultHandler {
  def init (validator : Validator, checker : Option[Document]) : Unit = {}

  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    if (!result.valid) {
      result match {
        case errorResult : ErrorResult =>
          sendError(errorResult, resp)
      }
    }
  }

  private def sendError (er : ErrorResult, resp : CheckerServletResponse) : Unit = {
    er.headers.keySet.iterator.foreach(h => resp.addHeader(h, er.headers.get(h)))
    resp.sendError(er.code, er.message)
  }
}
