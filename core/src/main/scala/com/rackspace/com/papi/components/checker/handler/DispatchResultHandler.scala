/***
 *   Copyright 2014 Rackspace US, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rackspace.com.papi.components.checker.handler

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.Validator
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results.Result
import org.w3c.dom.Document

import scala.collection.immutable.List

class DispatchResultHandler(private[this] var handlers : List[ResultHandler] = List[ResultHandler]())
      extends ResultHandler {

  def addHandler(rh : ResultHandler) : Unit = handlers = handlers :+ rh
  def removeHandler(rh : ResultHandler) : Unit = handlers = handlers.filterNot(r => rh == r)

  def init(validator : Validator, checker : Option[Document]) : Unit = {
    handlers.foreach(h => h.init(validator, checker))
  }
  def handle (req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, result : Result)  : Unit = {
    handlers.foreach(h => h.handle(req,resp,chain,result))
  }
  override def inStep (currentStep: Step, req: CheckerServletRequest, resp : HttpServletResponse, context: StepContext) : StepContext = {
    handlers.foldLeft(context)((context : StepContext, handler : ResultHandler) => handler.inStep(currentStep, req, resp, context))
  }
  override def destroy : Unit = {
    handlers.foreach(h => h.destroy)
  }
}
