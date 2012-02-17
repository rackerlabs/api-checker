package com.rackspace.com.papi.components.checker.handler

import scala.collection.immutable.List

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result
import com.rackspace.com.papi.components.checker.ResultHandler

class DispatchResultHandler(private[this] var handlers : List[ResultHandler] = List[ResultHandler]())
      extends ResultHandler {

  def addHandler(rh : ResultHandler) : Unit = handlers = handlers :+ rh
  def removeHandler(rh : ResultHandler) : Unit = handlers = handlers.filterNot(r => rh == r)
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result)  : Unit = {
    handlers.foreach(h => h.handle(req,resp,result))
  }

}
