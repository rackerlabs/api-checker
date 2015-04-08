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

import com.rackspace.com.papi.components.checker.BaseValidatorSuite
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.{AcceptResult, ErrorResult, StepContext}
import org.junit.runner.RunWith
import org.mockito.Mockito.{mock, never, verify}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DelegationHandlerTest extends BaseValidatorSuite {

  val delegationHandler = new DelegationHandler(.5)

  test("Delegation header should be added to request on error result") {
    val res = new ErrorResult("forbidden", 403, StepContext(-1), "-1", -1)

    val req = mock(classOf[CheckerServletRequest])
    val resp = mock(classOf[CheckerServletResponse])

    delegationHandler.handle(req, resp, chain, res)

    verify(req).addHeader("X-Delegated", "status_code=403`component=api-checker`message=forbidden;q=0.5")
  }

  test("Request should be forwarded on an error result") {
    val res = new ErrorResult("forbidden", 403, StepContext(-1), "-1", -1)

    val req = mock(classOf[CheckerServletRequest])
    val resp = mock(classOf[CheckerServletResponse])
    val chn = mock(classOf[FilterChain])

    delegationHandler.handle(req, resp, chn, res)

    verify(chn).doFilter(req, resp)
  }

  test("Request should not be forwarded on accept result") {
    val res = new AcceptResult("ok", StepContext(-1), "-1", -1)

    val req = mock(classOf[CheckerServletRequest])
    val resp = mock(classOf[CheckerServletResponse])
    val chn = mock(classOf[FilterChain])

    delegationHandler.handle(req, resp, chn, res)

    verify(chn, never()).doFilter(req, resp)
  }
}
