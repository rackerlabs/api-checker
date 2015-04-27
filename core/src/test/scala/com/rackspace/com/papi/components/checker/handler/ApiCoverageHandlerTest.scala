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
             xmlns:util="http://www.rackspace.com/repose/wadl/checker/util">
      <step id="S0" type="START" next="d57e4 SE1 d65e2u"/>
      <step id="SA" type="ACCEPT" priority="100004"/>
      <step type="REQ_TYPE_FAIL" id="d57e5rqt" notMatch="(?i)(application/xml)(;.*)?|(?i)(application/json)(;.*)?"
            priority="30003"/>
      <step type="METHOD_FAIL" id="d65e3m" notMatch="GET" priority="20052"/>
      <step id="SE0" type="URL_FAIL" priority="10002"/>
      <step id="SE1" type="METHOD_FAIL" priority="20001"/>
      <step type="URL_FAIL" id="d65e2u" notMatch="path" priority="10051"/>
      <step type="URL" id="d57e4" match="path" next="d57e5 d65e3m SE0"/>
      <step type="METHOD" id="d57e5" match="GET" next="d57e9 d57e11 d57e5rqt"/>
      <step type="REQ_TYPE" id="d57e9" match="(?i)(application/xml)(;.*)?" next="SA"/>
      <step type="REQ_TYPE" id="d57e11" match="(?i)(application/json)(;.*)?" next="SA"/>
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

  List(("GET", "/path", "application/xml", """{"steps":["S0","d57e4","d57e5","d57e9","SA"]}"""),
       ("GET", "/path", "application/json", """{"steps":["S0","d57e4","d57e5","d57e11","SA"]}"""),
       ("GET", "/path", "text/plain", """{"steps":["S0","d57e4","d57e5","d57e5rqt"]}"""),
       ("POST", "/path", "application/xml", """{"steps":["S0","d57e4","d65e3m"]}"""),
       ("GET", "/notapath", "application/xml", """{"steps":["S0","d65e2u"]}"""),
       ("POST", "/", "application/xml", """{"steps":["S0","SE1"]}"""),
       ("GET", "/", "application/xml", """{"steps":["S0","SE1"]}""")).foreach { case (method, path, content, result) =>
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

