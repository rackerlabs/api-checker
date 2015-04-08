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

import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.{MultiFailResult, ErrorResult, Result}

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
