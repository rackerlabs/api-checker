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
import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource
import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.com.papi.components.checker.Converters._

import com.rackspace.com.papi.components.checker.servlet.{CheckerServletResponse, CheckerServletRequest}
import com.rackspace.com.papi.components.checker.step.StepHandler
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool
import com.rackspace.com.papi.components.checker.{BaseValidatorSuite, TestConfig, Validator}
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.test.appender.ListAppender
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar

class ApiCoverageHandlerTest extends BaseValidatorSuite with MockitoSugar with BeforeAndAfter {
  val apiCoverageHandler = new ApiCoverageHandler
  val checkerXml =
    <checker xmlns="http://www.rackspace.com/repose/wadl/checker" xmlns:json="http://json-schema.org/schema#"
             xmlns:util="http://www.rackspace.com/repose/wadl/checker/util" xmlns:atom="http://www.w3.org/2005/Atom">
      <step id="S0" type="START" next="d59e4 d59e54 SE1 d67e2u"/>
      <step id="SA" type="ACCEPT" priority="100006"/>
      <step type="REQ_TYPE_FAIL" id="d59e133rqt" notMatch="(?i)(application/atom\+xml)(;.*)?" priority="30005"/>
      <step type="REQ_TYPE_FAIL" id="d59e107rqt" notMatch="(?i)(application/atom\+xml)(;.*)?" priority="30005"/>
      <step type="METHOD_FAIL" id="d67e4m" notMatch="POST" priority="20053"/>
      <step id="SE0" type="URL_FAIL" priority="10003"/>
      <step id="SE1" type="METHOD_FAIL" priority="20002"/>
      <step type="URL_FAIL" id="d67e3u" notMatch="entries" priority="10052"/>
      <step type="REQ_TYPE_FAIL" id="d59e163rqt" notMatch="(?i)(application/atom\+xml)(;.*)?" priority="30005"/>
      <step type="METHOD_FAIL" id="d67e9m" notMatch="POST" priority="20053"/>
      <step type="URL_FAIL" id="d67e8u" notMatch="entries" priority="10052"/>
      <step type="URL_FAIL" id="d67e2u" notMatch="nova|servers" priority="10101"/>
      <step type="URL" id="d59e4" match="nova" next="d59e5 SE1 d67e3u"/>
      <step type="URL" id="d59e5" match="entries" next="POST_d59e5 d67e4m SE0"/>
      <step type="METHOD" id="POST_d59e5" match="POST" label="ε" next="d59e133 d59e107"/>
      <step type="METHOD" id="d59e133" match="POST" label="addCloudServersOpenStackEntry" next="d59e137 d59e133rqt"/>
      <step type="REQ_TYPE" id="d59e137" match="(?i)(application/atom\+xml)(;.*)?" next="SA"/>
      <step type="METHOD" id="d59e107" match="POST" label="addRHELEntry" next="d59e111 d59e107rqt"/>
      <step type="REQ_TYPE" id="d59e111" match="(?i)(application/atom\+xml)(;.*)?" next="SA"/>
      <step type="URL" id="d59e54" match="servers" next="d59e55 SE1 d67e8u"/>
      <step type="URL" id="d59e55" match="entries" next="POST_d59e55 d67e9m SE0"/>
      <step type="METHOD" id="POST_d59e55" match="POST" label="ε" next="d59e163 d59e107"/>
      <step type="METHOD" id="d59e163" match="POST" label="addCloudServersEntry" next="d59e167 d59e163rqt"/>
      <step type="REQ_TYPE" id="d59e167" match="(?i)(application/atom\+xml)(;.*)?" next="SA"/>
    </checker>

  val handlerConfig = {
    val cnfg = TestConfig()
    val handler = new DispatchResultHandler(List[ResultHandler](apiCoverageHandler))
    cnfg.resultHandler = handler
    cnfg
  }

  val steps = {
    var transf: Transformer = null
    val stepHandler = new StepHandler(null, handlerConfig)
    try {
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new StreamSource(checkerXml), new SAXResult(stepHandler))
      stepHandler.step
    } finally {
      if (transf != null) IdentityTransformPool.returnTransformer(transf)
    }
  }

  val validator = Validator("MyInstTestValidator", steps, handlerConfig)

  //
  //  Reinitialize the handlers so that we can connect the state
  //  machine with the checker format manually.
  //

  handlerConfig.resultHandler.destroy
  handlerConfig.resultHandler.init(validator, Some(checkerXml))

  var apiCoverageLogger: ListAppender = _

  val req = Mockito.mock(classOf[CheckerServletRequest])
  val resp = Mockito.mock(classOf[CheckerServletResponse])
  val chn = Mockito.mock(classOf[FilterChain])
  val res = Mockito.mock(classOf[Result])

  before {
    val ctx = LogManager.getContext(false).asInstanceOf[LoggerContext]
    apiCoverageLogger = ctx.getConfiguration.getAppender("api-coverage-listAppender").asInstanceOf[ListAppender].clear

    reset(req, resp, chn, res)
  }

  List(("POST", "/nova/entries", "application/atom+xml", """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e133","d59e137","SA"]}"""),
       ("POST", "/nova/entries", "application/json", """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e133","d59e133rqt"]}""")
    ).foreach { case (method, path, content, result) =>
    test(s"For method: $method, path: $path, and context type: $content the result: $result is logged to api-coverage-logger") {
      val request = Mockito.mock(classOf[CheckerServletRequest])
      when(request.getMethod).thenReturn(method)
      when(request.getRequestURI).thenReturn(path)
      when(request.getContentType).thenReturn(content)

      validator.validate(request, resp, chn)

      val logEvents = apiCoverageLogger.getEvents
      assert(logEvents.size == 1)
      assert(result == logEvents.get(0).getMessage.getFormattedMessage)
    }
  }
}

