/** *
  * Copyright 2014 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.rackspace.com.papi.components.checker.handler

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.Validator
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.{StepContext, Step, Result}
import org.w3c.dom.{Document, NodeList}

class MethodLabelHandler extends ResultHandler  {
  val MethodLabelHeaderName: String = "X-METHOD-LABEL"

  var methodMap : Map[String, String] = Map()

  override def init(validator: Validator, checker: Option[Document]): Unit = {
    checker.map { doc =>
      val steps: NodeList = doc.getElementsByTagName("step")
      for {
        i <- 0 until steps.getLength
        item <- Option(steps.item(i))
        attributes <- Option(item.getAttributes)
        attType <- Option(attributes.getNamedItem("type"))
        id <- Option(attributes.getNamedItem("id")) if "METHOD".equalsIgnoreCase(attType.getTextContent)
        label <- Option(attributes.getNamedItem("label")) if "METHOD".equalsIgnoreCase(attType.getTextContent)
        idText <- Option(id.getTextContent)
        labelText <- Option(label.getTextContent)
      } yield {
        methodMap = methodMap ++ Map(idText -> labelText)
      }
    }
  }

  override def handle(req: CheckerServletRequest, resp: CheckerServletResponse, chain: FilterChain, result: Result): Unit = {
    for  {
      id <- result.stepIDs if methodMap.contains(id)
      m <- methodMap.get(id)
    } yield {
      req.addHeader(MethodLabelHeaderName, m)
    }
  }

  override def inStep (currentStep: Step, req: CheckerServletRequest, resp : CheckerServletResponse, context: StepContext) : StepContext = {
      methodMap.get(currentStep.id).map(context.requestHeaders.addHeader(MethodLabelHeaderName,_)).map(headers => context.copy(requestHeaders = headers)).getOrElse(context)
  }
}
