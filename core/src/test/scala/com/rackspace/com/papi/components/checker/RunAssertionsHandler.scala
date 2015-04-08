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
package com.rackspace.com.papi.components.checker

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step._
import org.w3c.dom.Document

import scala.language.implicitConversions

/**
 * The RunAssertionsHandler allows running assertions associated with a
 * particular request.
 *
 */
object RunAssertionsHandler {
  val ASSERT_FUNCTION = "com.rackspace.com.papi.components.checker.test.assertion.function"
}
class RunAssertionsHandler extends ResultHandler {
  type AssertFunction = (CheckerServletRequest, CheckerServletResponse, Result) => Unit

  def init(validator : Validator, checker : Option[Document]) : Unit = {}
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit =
    req.getAttribute(RunAssertionsHandler.ASSERT_FUNCTION) match {
      case f : AssertFunction => f(req,resp,result)
      case null => {}
      case _ => throw new ResultFailedException("Expecting an AssertFunction for ASSERT_ATTRIBUTE", req,resp,chain,result)
    }
}
