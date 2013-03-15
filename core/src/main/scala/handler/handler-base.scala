package com.rackspace.com.papi.components.checker.handler

import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.servlet._

import com.rackspace.com.papi.components.checker.Validator

import javax.servlet.FilterChain

import org.w3c.dom.Document

abstract class ResultHandler {
  def init(validator : Validator, checker : Option[Document]) : Unit
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result) : Unit
  def destroy : Unit = {}
}

class NullHandler extends ResultHandler {
  def init(validator : Validator, checker : Option[Document]) : Unit = {}
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result) : Unit = {}
}
