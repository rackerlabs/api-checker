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
import javax.servlet.http.HttpServletResponse
import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.com.papi.components.checker.Converters._
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.step.base.StepContext
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.com.papi.components.checker.step.startend.Accept
import com.rackspace.com.papi.components.checker.util.{HeaderMap, IdentityTransformPool}
import com.rackspace.com.papi.components.checker.{AssertResultHandler, BaseValidatorSuite, TestConfig, Validator}
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Matchers, Mockito}
import org.scalatest.BeforeAndAfter
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.collection.JavaConverters._


@RunWith(classOf[JUnitRunner])
class MethodLabelHandlerTest extends BaseValidatorSuite with BeforeAndAfter with MockitoSugar {
  val methodNameLoggerHandler = new MethodLabelHandler

  val handlerConfig = {
    val cnfg = TestConfig()
    val handler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(),
      methodNameLoggerHandler,
      new AssertResultHandler(),
      new ServletResultHandler()))
    cnfg.resultHandler = handler
    cnfg
  }

  val xmlChecker =
    <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
      <step id="S0" type="START" next="a badAURL badMethod"/>
      <step id="a" type="URL" match="a" next="GET PUT DELETE POST-XML POST-JSON badMethodNotGetOrPostorPut badURL"/>
      <step id="GET" type="METHOD" match="GET" next="Accept" label="some-specific-method"/>
      <step id="PUT" type="METHOD" match="PUT" next="Header badHeader" label="put-specific-method"/>
      <step id="POST-XML" type="METHOD" match="POST" next="REQ_TYPE_XML REQ_TYPE_XML_FAIL" label="post-xml-method"/>
      <step id="POST-JSON" type="METHOD" match="POST" next="REQ_TYPE_JSON REQ_TYPE_JSON_FAIL" label="post-json-method"/>
      <step id="DELETE" type="METHOD" match="DELETE" next="Accept"/>
      <step id="REQ_TYPE_XML" type="REQ_TYPE" next="Accept" match="(?i)(application/xml)(;.*)?"/>
      <step id="REQ_TYPE_JSON" type="REQ_TYPE" next="Accept" match="(?i)(application/json)(;.*)?"/>
      <step id="REQ_TYPE_XML_FAIL" type="REQ_TYPE_FAIL" notMatch="(?i)(application/xml)(;.*)?"/>
      <step id="REQ_TYPE_JSON_FAIL" type="REQ_TYPE_FAIL" notMatch="(?i)(application/json)(;.*)?"/>
      <step id="Header" type="HEADER_ANY" name="foo" match="bar" next="Accept"/>
      <step id="Accept" type="ACCEPT" priority="100000"/>
      <step id="badAURL" type="URL_FAIL" notMatch="a" priority="1000"/>
      <step id="badMethodNotGetOrPostorPut" type="METHOD_FAIL" notMatch="GET|POST|PUT|DELETE" priority="2000"/>
      <step id="badMethod" type="METHOD_FAIL" priority="2000"/>
      <step id="badURL" type="URL_FAIL" priority="1000"/>
      <step id="badHeader" type="CONTENT_FAIL" priority="1000"/>
    </checker>

  val headers: HeaderMap = new HeaderMap().addHeaders("foo", List("bar", "baz"))

  val steps = {
    var transf: Transformer = null
    val stepHandler = new StepHandler(null, handlerConfig)
    try {
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new StreamSource(xmlChecker), new SAXResult(stepHandler))
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
  handlerConfig.resultHandler.init(validator, Some(xmlChecker))

  val req = Mockito.mock(classOf[CheckerServletRequest])
  val resp = Mockito.mock(classOf[HttpServletResponse])
  val chn = Mockito.mock(classOf[FilterChain])
  val res = Mockito.mock(classOf[Result])

  before {
    reset(req, resp, chn, res)
  }

  //INSTEP METHOD
  test("when the validator finds a method with a label on /a should add the label header to the context") {
    val request = Mockito.mock(classOf[CheckerServletRequest])
    when(request.getMethod).thenReturn("POST")
    when(request.getRequestURI).thenReturn("/a")
    when(request.getContentType).thenReturn("application/xml")
    when(request.getHeaders("X-METHOD-LABEL")).thenReturn(List[String]().toIterator.asJavaEnumeration)

    validator.validate(request, resp, chn)

    val newRequest: ArgumentCaptor[CheckerServletRequest] = ArgumentCaptor.forClass(classOf[CheckerServletRequest])
    verify(chn).doFilter(newRequest.capture(), Matchers.any(classOf[HttpServletResponse]))
    assert(newRequest.getValue.getHeaders("X-METHOD-LABEL").nextElement() == "post-xml-method")
  }

  test("when a validator finds a method without a label on /a should not add the label header to the context2") {
    val request = Mockito.mock(classOf[CheckerServletRequest])
    when(request.getMethod).thenReturn("DELETE")
    when(request.getRequestURI).thenReturn("/a")
    when(request.getContentType).thenReturn("application/xml")
    when(request.getHeaders("X-METHOD-LABEL")).thenReturn(List[String]().toIterator.asJavaEnumeration)

    validator.validate(request, resp, chn)

    val newRequest: ArgumentCaptor[CheckerServletRequest] = ArgumentCaptor.forClass(classOf[CheckerServletRequest])
    verify(chn).doFilter(newRequest.capture(), Matchers.any(classOf[HttpServletResponse]))
    assert(!newRequest.getValue.getHeaders("X-METHOD-LABEL").hasMoreElements)
  }

  test("a method with a label on /a should add the label header to the context") {
    val getStep = new Method("GET", "label", "GET".r, Array())
    val putStep = new Method("PUT", "label", "PUT".r, Array())

    val newCtx = methodNameLoggerHandler.inStep(putStep, req, resp, methodNameLoggerHandler.inStep(getStep, req, resp, StepContext(requestHeaders = headers)))

    assert(newCtx.requestHeaders.get("foo").get == headers.get("foo").get)
    assert(newCtx.requestHeaders.get("X-METHOD-LABEL").get == List("some-specific-method", "put-specific-method"))
  }

  test("a method without a label on /a should not add the label header to the context") {
    val step = new Method("POST", "label", "POST".r, Array())

    val newCtx = methodNameLoggerHandler.inStep(step, req, resp, StepContext(requestHeaders = headers))

    assert(newCtx.requestHeaders.get("foo").get == headers.get("foo").get)
    assert(newCtx.requestHeaders.get("X-METHOD-LABEL").isEmpty)
  }

  test("a step not in the map should not add the label header to the context") {
    val step = new Accept("foo", "label", 1)

    val newCtx = methodNameLoggerHandler.inStep(step, req, resp, StepContext(requestHeaders = headers))

    assert(newCtx.requestHeaders.get("foo").get == headers.get("foo").get)
    assert(newCtx.requestHeaders.get("X-METHOD-LABEL").isEmpty)
  }

  test("a checker which has failed to be created results in no label header to the context") {
    val methodNameLoggerHandler = new MethodLabelHandler
    methodNameLoggerHandler.init(Mockito.mock(classOf[Validator]), None)
    val step = new Method("POST", "label", "POST".r, Array())

    val newCtx = methodNameLoggerHandler.inStep(step, req, resp, StepContext(requestHeaders = headers))

    assert(newCtx.requestHeaders.get("foo").get == headers.get("foo").get)
    assert(newCtx.requestHeaders.get("X-METHOD-LABEL").isEmpty)
  }

  //HANDLE METHOD

  test("a method with a label on /a should set a header on the request") {
    when(res.stepIDs).thenReturn(List("S0", "a", "PUT", "badHeader"))

    methodNameLoggerHandler.handle(req, resp, chn, res)

    verify(req).addHeader("X-METHOD-LABEL", "put-specific-method")
  }

  test("a method without a label on /a should not set a label on the request") {
    when(res.stepIDs).thenReturn(List("S0", "a", "POST", "Accept"))

    methodNameLoggerHandler.handle(req, resp, chn, res)

    verify(req, never()).addHeader(anyString(), anyString())
  }

  test("a failed result before getting to a method should not add a header to the request") {
    when(res.stepIDs).thenReturn(List("S0", "badAURL"))

    methodNameLoggerHandler.handle(req, resp, chn, res)

    verify(req, never()).addHeader(anyString(), anyString())
  }

  test("a checker which has failed to be created results in no header set on the request") {
    val methodNameLoggerHandler = new MethodLabelHandler
    methodNameLoggerHandler.init(Mockito.mock(classOf[Validator]), None)
    when(res.stepIDs).thenReturn(List())

    methodNameLoggerHandler.handle(req, resp, chn, res)

    verify(req, never()).addHeader(anyString(), anyString())
  }
}
