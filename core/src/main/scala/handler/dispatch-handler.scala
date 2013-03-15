package com.rackspace.com.papi.components.checker.handler

import scala.collection.immutable.List

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result

import javax.servlet.FilterChain

import org.w3c.dom.Document

import com.rackspace.com.papi.components.checker.Validator

class DispatchResultHandler(private[this] var handlers : List[ResultHandler] = List[ResultHandler]())
      extends ResultHandler {

  def addHandler(rh : ResultHandler) : Unit = handlers = handlers :+ rh
  def removeHandler(rh : ResultHandler) : Unit = handlers = handlers.filterNot(r => rh == r)

  def init(validator : Validator, checker : Option[Document]) : Unit = {
    handlers.foreach(h => h.init(validator, checker))
  }
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    handlers.foreach(h => h.handle(req,resp,chain,result))
  }
  override def destroy : Unit = {
    handlers.foreach(h => h.destroy)
  }
}
