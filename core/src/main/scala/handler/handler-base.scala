package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.servlet._

import org.w3c.dom.Document

abstract class ResultHandler {
  def init(checker : Option[Document]) : Unit
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result) : Unit
}

class NullHandler extends ResultHandler {
  def init(checker : Option[Document]) : Unit = {}
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result) : Unit = {}
}
