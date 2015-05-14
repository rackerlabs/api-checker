/** *
  * Copyright 2015 Rackspace US, Inc.
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
import com.rackspace.com.papi.components.checker.step.results.{MultiFailResult, Result}
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.slf4j.LoggerFactory
import org.w3c.dom.Document

class ApiCoverageHandler extends ResultHandler with LazyLogging {
  val coverageLogger = LoggerFactory.getLogger("api-coverage-logger")

  override def init(validator: Validator, checker: Option[Document]): Unit = {}

  override def handle(req: CheckerServletRequest, resp: CheckerServletResponse, chain: FilterChain, result: Result): Unit = {
    val stepList: List[String] = if (result.valid) {
      result.allResults.head.stepIDs
    } else result match {
      case result2: MultiFailResult =>
        val result1: Result = result2.fails.head
        List(result.stepId) ++ result1.stepIDs ++ List(result.allResults.head.stepId)
      case _ =>
        result.allResults.head.stepIDs
      //      result.stepIDs ++ List(result.allResults.head.stepId)
    }
    coverageLogger.info(stepList.foldLeft("{\"steps\":[")({ (b, a) => b + "\"" + a + "\"," }).dropRight(1) + "]}")
  }
}
