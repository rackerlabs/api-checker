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
package com.rackspace.com.papi.components.checker.step

import javax.servlet.FilterChain
import javax.xml.namespace.QName

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.exceptions.ProcessingException
import com.rackspace.com.papi.components.checker.Validator
import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results._
import com.rackspace.com.papi.components.checker.step.startend._
import com.rackspace.com.papi.components.checker.util.{HeaderMap, ImmutableNamespaceContext, ObjectMapperPool, XMLParserPool}
import org.junit.runner.RunWith
import org.mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.w3c.dom.Document
import org.xml.sax.SAXParseException

import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scala.xml._


@RunWith(classOf[JUnitRunner])
class StepSuite extends BaseStepSuite with MockitoSugar {
  class GenericStep(id: String, label: String, next: Array[Step], extraHeaders: Option[HeaderMap] = None) extends ConnectedStep(id, label, next) {
    override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] =
      Some(extraHeaders.map({headers => context.copy(requestHeaders = context.requestHeaders.addHeaders(headers))}).getOrElse(context))
  }

  test("steps should call the handlers before continuing to the next step") {
    val handler = Mockito.mock(classOf[ResultHandler])
    val stepContext = new StepContext(handler = Some(handler), uriLevel = 100)
    val step = new GenericStep("newGenericStep", "newGenericLabel", Array(new Method("GET", "Foo", "GET".r, Array(new Accept("Accept", "Test Accept", 1)))))
    val request: CheckerServletRequest = Mockito.mock(classOf[CheckerServletRequest])
    Mockito.when(request.getMethod).thenReturn("GET")
    Mockito.when(request.URISegment).thenReturn(Array[String]())
    val response: CheckerServletResponse = Mockito.mock(classOf[CheckerServletResponse])

    val headerMap = new HeaderMap().addHeaders("foo", List("bar", "baz"))
    Mockito.when(handler.inStep(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenAnswer(new Answer[StepContext](){
      override def answer(invocationOnMock: InvocationOnMock): StepContext = {
        val arguments: Array[AnyRef] = invocationOnMock.getArguments
        if(arguments(0) == step && arguments(1) == request && arguments(2) == response && arguments(3) == stepContext) {
          stepContext.copy(requestHeaders = stepContext.requestHeaders.addHeaders(headerMap))
        } else {
          arguments(3).asInstanceOf[StepContext]
        }
      }
    })
    val result = step.check(request, response, chain, stepContext)
    assert(result.get.stepIDs == List("newGenericStep", "GET", "Accept"))
    Mockito.verify(request).addHeaders(Matchers.eq(headerMap))
  }

  test("steps should pass through the new context with no handlers set") {
    val stepContext = new StepContext(handler = None)
    val headerMap = new HeaderMap().addHeaders("foo", List("bar", "baz"))
    val step = new GenericStep("newGenericStep", "newGenericLabel", Array(new Accept("Accept", "Test Accept", 1)), Some(headerMap))
    val request: CheckerServletRequest = Mockito.mock(classOf[CheckerServletRequest])

    val response: CheckerServletResponse = Mockito.mock(classOf[CheckerServletResponse])
    val result = step.check(request, response, chain, stepContext)
    result.get.stepIDs == List("newGenericStep")
    Mockito.verify(request).addHeaders(Matchers.eq(headerMap))
  }

  test("handlers should pass through the context if there isnt an inStep method defined") {
    class GenericHandler extends ResultHandler {
      def init(validator : Validator, checker : Option[Document]) : Unit = {}
      def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result) : Unit = {}
    }

    val stepContext = new StepContext(handler = Some(new GenericHandler()))
    val headerMap = new HeaderMap().addHeaders("foo", List("bar", "baz"))
    val step = new GenericStep("newGenericStep", "newGenericLabel", Array(new Accept("Accept", "Test Accept", 1)), Some(headerMap))
    val request: CheckerServletRequest = Mockito.mock(classOf[CheckerServletRequest])

    val response: CheckerServletResponse = Mockito.mock(classOf[CheckerServletResponse])
    val result = step.check(request, response, chain, stepContext)
    result.get.stepIDs == List("newGenericStep")
    Mockito.verify(request).addHeaders(Matchers.eq(headerMap))
  }

  test("Regardless of input, Accept step should always return AcceptResult") {
    val accept = new Accept("a","a", 10)
    val res2 = accept.check(request("GET", "/a/b"), response,chain, 2)
    assert(res2.isDefined)
    assert(res2.get.isInstanceOf[AcceptResult])
    val res3 = accept.check(request("XGET", "/a/b"), response,chain, 0)
    assert(res3.isDefined)
    assert(res3.get.isInstanceOf[AcceptResult])
    val res = accept.check (request("DELETE", "/a/a/a/a/a/a"), response, chain, -1)
    assert(res.isDefined)
    assert(res.get.isInstanceOf[AcceptResult])
  }

  test ("Accept step should set step context request headers") {
    val accept = new Accept("a", "a", 10)
    val req1 = request("GET", "/a/b", "", "", false, Map().asInstanceOf[Map[String,List[String]]])
    val req2 = request("GET", "/b/c", "", "", false, Map().asInstanceOf[Map[String,List[String]]])
    val req3 = request("GET", "/c/d", "", "", false, Map().asInstanceOf[Map[String,List[String]]])
    val req4 = request("GET", "/e/e", "", "", false, Map().asInstanceOf[Map[String,List[String]]])

    accept.check (req1, response, chain, StepContext(1, (new HeaderMap()).addHeader("Foo", "Bar")))
    accept.check (req2, response, chain, StepContext(2, (new HeaderMap()).addHeaders("Foo", List("Bar", "Baz", "Biz"))))
    accept.check (req3, response, chain, StepContext(3, (new HeaderMap()).addHeader("Foo", "Baz").addHeaders("Bar",List("Fe","Fi","Fo")))) 
    accept.check (req4, response, chain, StepContext(1, (new HeaderMap()).addHeader("Foo", "Bar,Baz,Biz")))

    assert (req1.getHeaders("Foo").toList == List("Bar"))
    assert (req2.getHeaders("Foo").toList == List("Bar", "Baz", "Biz"))
    assert (req3.getHeaders("Foo").toList == List("Baz"))
    assert (req3.getHeaders("Bar").toList == List("Fe", "Fi", "Fo"))
    assert (req4.getHeaders("Foo").toList == List("Bar,Baz,Biz"))
  }

  test("Start should not change URI level") {
    val start = new Start("s", "s", Array[Step]())
    assert(start.checkStep(null, null, null, -1) == -1)
    assert(start.checkStep(request("GET", "/a/b"), response,chain, 2) == 2)
    assert(start.checkStep(request("",""), response,chain, 1000) == 1000)
  }

  test("MethodFail should return method fail result if the URI level has been exceeded") {
    val mf  = new MethodFail("mf", "mf", 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MethodFailResult])
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[MethodFailResult])
  }

  test("MethodFail should return method fail result with empty allow header  if the URI level has been exceeded") {
    val mf  = new MethodFail("mf", "mf", 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    val headers = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers.containsKey("Allow"))
    assert (headers.get("Allow").equals(""))
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    val headers2 = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers2.containsKey("Allow"))
    assert (headers2.get("Allow").equals(""))
  }

  test("MethodFail should return None when URI level is not exceeded") {
    val mf  = new MethodFail("mf", "mf", 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isEmpty)
  }

  test("MethodFailMatch should return method fail result if the URI level has been exceeded and the method regex does not match") {
    val mf  = new MethodFailMatch("mf", "mf", "POST".r, 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MethodFailResult])
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[MethodFailResult])
  }

  test("MethodFailMatch should return method fail result with appropriate Allow header if the URI level has been exceeded and the method regex does not match") {
    val mf  = new MethodFailMatch("mf", "mf", "POST".r, 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    val headers = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers.containsKey("Allow"))
    assert (headers.get("Allow").equals("POST"))
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    val headers2 = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers2.containsKey("Allow"))
    assert (headers2.get("Allow").equals("POST"))
  }

  test("MethodFailMatch should return method fail result with appropriate Allow header if the URI level has been exceeded and the method regex does not match (multiple method match)") {
    val mf  = new MethodFailMatch("mf", "mf", "POST|PUT".r, 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    val headers = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers.containsKey("Allow"))
    assert (headers.get("Allow").equals("POST, PUT"))
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    val headers2 = res.get.asInstanceOf[MethodFailResult].headers
    assert (headers2.containsKey("Allow"))
    assert (headers2.get("Allow").equals("POST, PUT"))
  }

  test("MethodFailMatch should return None if the URI level has been exceeded and the method regex matches") {
    val mf  = new MethodFailMatch("mf", "mf", "GET".r, 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isEmpty)
  }

  test("MethodFailMatch should return None when URI level is not exceeded") {
    val mf  = new MethodFailMatch("mf", "mf", "GET".r, 10)
    val res = mf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isEmpty)
  }

  test("URLFail should return URL fail result if URI level has not been exceeded") {
    val uf = new URLFail("uf", "uf", 10)
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD("ufx", "ufx", Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2.isEmpty)
  }

  test("URLFailXSD should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3.isEmpty)
  }

  test("URLFailXSDMatch should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch("ufx", "ufx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/c/b"), response,chain, 4)
    assert (res3.isEmpty)
  }

  test("URLFailXSDMatch should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailXSDMatch should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3.isEmpty)
    val res4 = ufx.check (request("GET", "/c/b"), response,chain, 0)
    assert (res4.isEmpty)
  }

  test("URLFail should return None if URI level has been exceeded") {
    val uf = new URLFail("uf", "uf", 10)
    val res = uf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isEmpty)
  }

  test("URLFailMatch should return URL fail result if URI level has not been exceeded and the uri regex does not match") {
    val uf = new URLFailMatch ("ufm", "ufm", "c".r, 10)
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailMatch should return None if URI level has not been exceeded and the uri regex matches") {
    val uf = new URLFailMatch ("ufm", "ufm", "a".r, 10)
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isEmpty)
  }

  test("URLFailMatch should return None if URI level has been exceeded") {
    val uf = new URLFailMatch("uf", "uf", "a".r, 10)
    val res = uf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isEmpty)
  }

  test("URIXSD mismatch message should be the same as the QName") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assert (urixsd.mismatchMessage == stepType.toString)
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/START/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/METHOD/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/ATART/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAI2/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METHO4_FAIL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/ACCCPT/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URLL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METH0D/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/UR7XSD/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/START/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadbgff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/aoeee..x/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-901237afadb5ff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9:17b1db29daf7/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/+58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/54/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/0/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/32/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/2/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/12/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/100/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/84/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a URI match, and a captureHeader, the header should be set") {
    val accept = new Accept("accept", "Accept", 1000)
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Some("foo"), Array[Step](accept))
    val req = request("GET", "/54/b")
    assert (urixsd.check (req, response,chain, 0).isDefined)
    assert (req.getHeader("FOO") == "54")
  }


  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100, assert") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/1/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/33/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/15/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/85/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/555/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/hello/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/-99/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3tecr/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/54/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/0/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/32/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/12/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/100/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/84/b"), response,chain, 2))
  }

  test("URI mismatch message should be the same of the uri regex") {
    val uri = new URI("u", "u", "u".r, Array[Step]())
    assert (uri.mismatchMessage == "u".r.toString)
  }

  test("In a URI step, if there is a URI match, the uriLevel should increase by 1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/a/b"), response,chain, 0) == 1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 0) == 1)
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 1) == 2)
  }

  test("In a URI step, if there is a URI mismatch, the uriLevel should be -1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/c/b"), response,chain, 0) == -1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/c/d"), response,chain, 0) == -1)
    assert (uri2.checkStep (request("GET", "/c/d"), response,chain, 1) == -1)
  }

  test("In a URI step, if there is a URI match, the context should have a uriLevel increase by 1"){
    val uri = new URI("a", "a", "a".r, Array[Step]())
    val ctx1 = StepContext()
    assert (uri.checkStep (request("GET", "/a/b"), response,chain, ctx1).get.uriLevel == 1)
  }

  test("In a URI step, if there is a URI match, the context should have a uriLevel increase by 1, if a captureHeader is set it should contain the header"){
    val uri = new URI("a", "a", "a".r, Some("URI"), Array[Step]())
    val nctx = uri.checkStep (request("GET", "/a/b"), response,chain, StepContext())
    assert(nctx.get.uriLevel == 1)
    assert(nctx.get.requestHeaders("URI") == List("a"))
  }

  test("In a URI step, if there is a URI mismatch, the returned context should be None") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    val ctx1 = StepContext()
    assert (uri.checkStep (request("GET", "/c/b"), response,chain, ctx1).isEmpty)
  }

  test("In a URI step, if there is a URI match, but the URI level has been exceeded the new URI level should be -1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/a/b"), response,chain, 2) == -1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 3) == -1)
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 4) == -1)
  }

  test("Method mismatch message should be the same of the uri regex") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.mismatchMessage == "GET".r.toString)
  }

  test("In a Method step, if the uriLevel has not been exceeded, the returned URI level should be -1") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 0) == -1)
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 1) == -1)
  }

  test("In a Method step, if the uriLevel has been exceeded, and the method matches, the URI level should stay the same") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 2) == 2)
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 3) == 3)
    val method2 = new Method("GETPOST", "GET or POST", "GET|POST".r, Array[Step]())
    assert (method2.checkStep (request("GET", "/a/b"), response,chain, 2) == 2)
    assert (method2.checkStep (request("POST", "/a/b"), response,chain, 3) == 3)
  }

  test("In a Method step, if the uriLevel has been exceeded, and the method does not match, the URI level should be set to -1") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("POST", "/a/b"), response,chain, 2) == -1)
    assert (method.checkStep (request("GTB", "/a/b"), response,chain, 3) == -1)
    val method2 = new Method("GETPOST", "GET or POST", "GET|POST".r, Array[Step]())
    assert (method2.checkStep (request("PUT", "/a/b"), response,chain, 2) == -1)
    assert (method2.checkStep (request("DELETE", "/a/b"), response,chain, 3) == -1)
  }

  test("A ReqTestFail step should fail if the content type does not match with a BadMediaTypeResult") {
    val rtf = new ReqTypeFail ("XML", "XML", "application/xml|application/json".r, 10)
    assertBadMediaType (rtf.check (request("PUT", "/a/b", "*.*"), response, chain, 1))
    assertBadMediaType (rtf.check (request("POST", "/index.html", "application/XMLA"), response, chain, 0))
  }

  test("A ReqTestFail step should return None if the content type matchs") {
    val rtf = new ReqTypeFail ("XML", "XML", "application/xml|application/json".r, 10)
    assert (rtf.check (request("PUT", "/a/b", "application/json"), response, chain, 1).isEmpty)
    assert (rtf.check (request("POST", "/index.html", "application/xml"), response, chain, 0).isEmpty)
  }

  test("ReqType mismatch message should be the same of the type regex") {
    val rt = new ReqType ("XML", "XML", "(application/xml|application/json)()".r, Array[Step]())
    assert (rt.mismatchMessage == "(application/xml|application/json)()".r.toString)
  }

  test("In a ReqType step, if the content type does not match, the returned URI level should be -1") {
    val rt = new ReqType ("XML", "XML", "(application/xml|application/json)()".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b","*.*"), response,chain, 0) == -1)
    assert (rt.checkStep (request("POST", "/a/b","application/junk"), response,chain, 1) == -1)
    val rt2 = new ReqType ("XML", "XML", "text/html".r, Array[Step]())
    assert (rt2.checkStep (request("PUT", "/a/b","*.*"), response,chain, 0) == -1)
    assert (rt2.checkStep (request("POST", "/a/b","application/junk"), response,chain, 2) == -1)
  }

  test("In a ReqType step, if the content type is null, the returned URI level should be -1") {
    val rt = new ReqType ("XML", "XML", "(application/xml|application/json)()".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b", null), response,chain, 0) == -1)
  }

  test("In a ReqType step, if the content matches, the URI level should stay the same") {
    val rt = new ReqType ("XML", "XML", "(application/xml|application/json)()".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b","application/xml"), response,chain, 0) == 0)
    assert (rt.checkStep (request("POST", "/a/b","application/json"), response,chain, 1) == 1)
    val rt2 = new ReqType ("XML", "XML", "(text/html)()".r, Array[Step]())
    assert (rt2.checkStep (request("GET", "/a/b/c","text/html"), response,chain, 2) == 2)
  }

  test("In a WellFormedXML step, if the content contains well formed XML, the uriLevel should stay the same") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step]())
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>), response, chain, 0) == 0)
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"><more/></validXML>), response, chain, 1) == 1)
  }

  test("In a WellFormedXML step, the parsed DOM should be stored in the request") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])

    val req2 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"><more/></validXML>)
    assert (wfx.checkStep (req2, response, chain, 1) == 1)
    assert (req2.parsedXML != null)
    assert (req2.parsedXML.isInstanceOf[Document])
  }


  test("In a WellFormedXML step, one should be able to reparse the XML by calling getInputStream") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    val xml1 = XML.load(req1.getInputStream())
    assert (xml1 != null)

    val req2 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"><more/></validXML>)
    wfx.checkStep (req2, response, chain, 1)
    val xml2 = XML.load(req2.getInputStream())
    assert (xml2 != null)
    assert ((xml2 \ "more") != null)
  }

  test("In a WellFormedXML step, if the content is not well formed XML, the uriLevel should be -1") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step]())
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>"""), response, chain, 0) == -1)
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", """{ \"bla\" : 55 }"""), response, chain, 1) == -1)
  }

  test("In a WellFormedXML step, if the content is not well formed XML, the request should contian a SAXException") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 150, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>""")

    wfx.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentErrorPriority == 150)

    val req2 = request("PUT", "/a/b", "application/xml", """{ \"bla\" : 55 }""")

    wfx.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentErrorPriority == 150)
  }

  test("In a WellFormedXML step, XML in the same request should not be parsed twice") {
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])
    assert (req1.contentErrorPriority == -1)

    val doc = req1.parsedXML
    wfx.checkStep (req1, response, chain, 0)
    //
    //  Assert that the same document is being returned.
    //
    assert (doc == req1.parsedXML)
  }

  test("In a WellFormedXML step, on two completely different requests the XML sholud be parsed each time"){
    val wfx = new WellFormedXML("WFXML", "WFXML", 100, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    val req2 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)

    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])
    assert (req1.contentErrorPriority == -1)
    wfx.checkStep (req2, response, chain, 0)

    assert (req1.parsedXML != req2.parsedXML)
  }

  ignore ("Since WellFormedXML steps are synchornous, the parser pool should contain only a single idle parser") {
    assert (XMLParserPool.numActive == 0)
    assert (XMLParserPool.numIdle == 1)
  }

  test ("If there is no content error ContentFail should return NONE") {
    val cf  = new ContentFail ("CF", "CF", 10)
    val wfx = new WellFormedXML("WFXML", "WFXML", 10, Array[Step](cf))
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (cf.check(req1, response, chain, 0).isEmpty)
    assert (req1.contentErrorPriority == -1)
  }

  test ("If there is an error ContentFail should return BadContentResult") {
    val cf  = new ContentFail ("CF", "CF", 10)
    val wfx = new WellFormedXML("WFXML", "WFXML", 100, Array[Step](cf))
    val req1 = request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>""")
    wfx.checkStep (req1, response, chain, 0)
    val result = cf.check(req1, response, chain, 0)
    assert (result.isDefined)
    assert (result.get.isInstanceOf[BadContentResult])
    assert (req1.contentErrorPriority == 100)
  }

  test("In a WellFormedJSON step, if the content contains well formed JSON, the uriLevel should stay the same") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    assert (wfj.checkStep (request("PUT", "/a/b", "application/json", """ { "valid" : true } """), response, chain, 0) == 0)
    assert (wfj.checkStep (request("PUT", "/a/b", "application/json", """ { "valid" : [true, true, true] }"""), response, chain, 1) == 1)
  }

  test("In a WellFormedJSON step, if the content contains well formed JSON, the request should contain a JsonNode") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    wfj.checkStep (req1, response, chain, 0)
    assert(req1.parsedJSON != null)
    assert(req1.parsedJSON.isInstanceOf[JsonNode])

    val req2 = request("PUT", "/a/b", "application/json", """ { "valid" : [true, true, true] }""")
    wfj.checkStep (req2, response, chain, 1)
    assert(req2.parsedJSON != null)
    assert(req2.parsedJSON.isInstanceOf[JsonNode])
  }

  test("In a WellFormedJSON step, if the content contains well formed JSON, contentErrorPriority should be -1") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    wfj.checkStep (req1, response, chain, 0)
    assert (req1.contentErrorPriority == -1)

    val req2 = request("PUT", "/a/b", "application/json", """ { "valid" : [true, true, true] }""")
    wfj.checkStep (req2, response, chain, 1)
    assert (req2.contentErrorPriority == -1)
  }

  test("In a WellFormedJSON step, if the content contains well formed JSON, you should be able to reparse the JSON by calling getInputStream") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    val req2 = request("PUT", "/a/b", "application/json", """ { "valid" : [true, true, true] }""")
    var jparser : ObjectMapper = null

    wfj.checkStep (req1, response, chain, 0)
    wfj.checkStep (req2, response, chain, 1)

    try {
      jparser = ObjectMapperPool.borrowParser
      val j1 = jparser.readValue(req1.getInputStream(), classOf[java.util.Map[Object, Object]])
      val j2 = jparser.readValue(req2.getInputStream(), classOf[java.util.Map[Object, Object]])

      assert (j1 != null)
      assert (j2 != null)
      assert (j2.asInstanceOf[java.util.Map[Object,Object]].get("valid") != null)
    } finally {
      if (jparser != null) ObjectMapperPool.returnParser(jparser)
    }
  }

  test("In a WellFormedJSON step, if the content contains JSON that is not well-formed, the uriLevel shoud be -1") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    assert (wfj.checkStep (request("PUT", "/a/b", "application/json", """ { "valid" : ture } """), response, chain, 0) == -1)
    assert (wfj.checkStep (request("PUT", "/a/b", "application/json", """ <json /> """), response, chain, 1) == -1)
  }

  test("In a WellFormedJSON step, if the content contains JSON that is not well-formed, then the request should contain a ParseException") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : ture } """)
    wfj.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[JsonParseException])

    val req2 = request("PUT", "/a/b", "application/json", """ <json /> """)
    wfj.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[JsonParseException])
  }

  test("In a WellFormedJSON step, if the content contains JSON that is not well-formed, then the request should contain the correct contentErrorPriority") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 1000, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : ture } """)
    wfj.checkStep (req1, response, chain, 0)
    assert (req1.contentErrorPriority == 1000)

    val req2 = request("PUT", "/a/b", "application/json", """ <json /> """)
    wfj.checkStep (req2, response, chain, 1)
    assert (req2.contentErrorPriority == 1000)
  }

  test("In a WellFormedJSON step, if the content contains well formed JSON, the same request should not be parsed twice") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    wfj.checkStep (req1, response, chain, 0)
    assert(req1.parsedJSON != null)
    assert(req1.parsedJSON.isInstanceOf[JsonNode])

    val obj = req1.parsedJSON
    wfj.checkStep (req1, response, chain, 0)
    assert (obj == req1.parsedJSON)
  }

  test("In a WellFormedJSON step, on two completly differet requests the JSON should be parsed each time") {
    val wfj = new WellFormedJSON("WFJSON", "WFJSON", 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    val req2 = request("PUT", "/a/b", "application/json", """ { "valid" : true } """)
    var jparser : ObjectMapper = null

    wfj.checkStep (req1, response, chain, 0)
    assert(req1.parsedJSON != null)
    assert(req1.parsedJSON.isInstanceOf[JsonNode])
    wfj.checkStep (req2, response, chain, 0)

    try {
      jparser = ObjectMapperPool.borrowParser
      val s1 = jparser.writeValueAsString(req1.parsedJSON)
      val s2 = jparser.writeValueAsString(req2.parsedJSON)

      assert (s1 == s2)
    } finally {
      if (jparser != null) ObjectMapperPool.returnParser(jparser)
    }
  }

  test ("In an XSD test, if the content contains valid XML, the uriLevel should stay the same") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == 0)
    assert (xsd.checkStep (req2, response, chain, 1) == 1)
  }


  test ("In an XSD test, if the content contains valid XML, the contentErrorPriority should be -1") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the errorContentPriorityShould be set") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 1001, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1001)
    assert (req2.contentErrorPriority == 1001)
  }


  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                         id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }


  test ("In an XSD test, if the content contains invalid XML, the errorContentPriorityShould be set (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 1001, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                         id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1001)
    assert (req2.contentErrorPriority == 1001)
  }

  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchema, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }


  test ("In an XSD test, if the content contains valid XML, the uriLevel should stay the same (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == 0)
    assert (xsd.checkStep (req2, response, chain, 1) == 1)
  }


  test ("In an XSD test, if the content contains valid XML, the contentErrorPriority should be -1 (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD test, if the content contains valid XML1, with transform == true, then default values should be filled in") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                         <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                      </e>, true)

    xsd.checkStep (req1, response, chain, 0)

    val updatedRequest = XML.load(req1.getInputStream())
    assert ((updatedRequest \ "stepType").text == "START")
    assert ((updatedRequest \ "even").text == "50")
  }

  test ("In an XSD test, if the content contains valid XML2, with transform == true, then default values should be filled in") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"/>, true)

    xsd.checkStep (req1, response, chain, 0)

    val updatedRequest = XML.load(req1.getInputStream())
    assert ((updatedRequest \ "@stepType").text == "START")
    assert ((updatedRequest \ "@even").text == "50")
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the contentErrorPriority should be net (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 100, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 100)
    assert (req2.contentErrorPriority == 100)
  }


  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                         id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }


  test ("In an XSD test, if the content contains invalid XML, the contentErrorPriority should be set (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 1002, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                         id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1002)
    assert (req2.contentErrorPriority == 1002)
  }

  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchema, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test("In an XPath test, if the XPath resolves to true the uriLevel should stay the same") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    assert (xpath.checkStep (req1, response, chain, 0) == 0)
    assert (xpath.checkStep (req2, response, chain, 1) == 1)
  }

  test("In an XPath test, if the XPath resolves to true the uriLevel should stay the same in the context") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    assert (xpath.checkStep (req1, response, chain, StepContext()).get.uriLevel == 0)
    assert (xpath.checkStep (req2, response, chain, StepContext(1)).get.uriLevel == 1)
  }


  test("In an XPath test, if the XPath resolves to true and there is a capture header, the capture header should be set") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root/@bar", None, None, context, 1, Some("Foo"), 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test" bar="yum">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test" bar="jaz">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    val ctx1 = xpath.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = xpath.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo") == List("yum"))
    assert (ctx2.requestHeaders("Foo") == List("jaz"))
  }

  test("In an XPath test, if the XPath resolves to true and there is a capture header, the capture header should be set (multi-value)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "//@bar", None, None, context, 1, Some("Foo"), 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test" bar="yum">
                         <child attribute="value" bar="yum2"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test" bar="jaz">
                         <tst:child attribute="value" bar="jaz2"/>
                         <tst:child attribute="value2" bar="jaz3"/>
                       </tst:root>, true)
    val ctx1 = xpath.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = xpath.checkStep (req2, response, chain, StepContext(1)).get

    //
    //  Since we are coercing into a string, we should just get the
    //  first value.
    //
    assert (ctx1.requestHeaders("Foo") == List("yum"))
    assert (ctx2.requestHeaders("Foo") == List("jaz"))
  }

  test("In an XPath test, if the XPath resolves to true and there is a capture header, the capture header should be set (multi-value, version 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "string-join((//@bar), ', ')", None, None, context, 2, Some("Foo"), 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test" bar="yum">
                         <child attribute="value" bar="yum2"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test" bar="jaz">
                         <tst:child attribute="value" bar="jaz2"/>
                         <tst:child attribute="value2" bar="jaz3"/>
                       </tst:root>, true)
    val ctx1 = xpath.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = xpath.checkStep (req2, response, chain, StepContext(1)).get

    //
    //  With XPath2 we can actually get separate values -- within a single header.
    //
    assert (ctx1.requestHeaders("Foo") == List("yum, yum2"))
    assert (ctx2.requestHeaders("Foo") == List("jaz, jaz2, jaz3"))
  }

  test("In an XPath test, if the XPath resolves to true and there is a capture header, the capture header should be set (version 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root/@bar", None, None, context, 2, Some("Foo"), 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test" bar="yum">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test" bar="jaz">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    val ctx1 = xpath.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = xpath.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo") == List("yum"))
    assert (ctx2.requestHeaders("Foo") == List("jaz"))
  }

  test("In an XPath test, if the XPath does not resolve context should be None.") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root/@bar", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    assert (xpath.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (xpath.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test("In an XPath test, if the XPath resolves to true the contentErrorPriority should be -1") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    xpath.checkStep (req1, response, chain, 0)
    xpath.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test("In an XPath test, if the XPath resolves to false the uriLevel should be -1") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    assert (xpath.checkStep (req1, response, chain, 0) == -1)
    assert (xpath.checkStep (req2, response, chain, 1) == -1)
  }


  test("In an XPath test, if the XPath resolves to false the contentErrorPriority should be set") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 1000, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    xpath.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1000)
    assert (req2.contentErrorPriority == 1000)
  }


  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with a message of 'Expecting '+XPATH") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("Expecting /tst:root"))
    assert (req1.contentErrorCode == 400)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("Expecting /tst:root"))
    assert (req2.contentErrorCode == 400)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with setErrorCode") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", None, Some(401), context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("Expecting /tst:root"))
    assert (req1.contentErrorCode == 401)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("Expecting /tst:root"))
    assert (req2.contentErrorCode == 401)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", Some("/tst:root was expected"), None, context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 400)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 400)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message and set code") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", Some("/tst:root was expected"), Some(401), context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 401)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 401)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message, set code, and errorPriority") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "/tst:root", Some("/tst:root was expected"), Some(401), context, 1, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10)
  }


  test("In an XPath test, if the XPath resolves to true the uriLevel should stay the same (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    assert (xpath.checkStep (req1, response, chain, 0) == 0)
    assert (xpath.checkStep (req2, response, chain, 1) == 1)
  }


  test("In an XPath test, if the XPath resolves to true the contentErrorPriority should be -1 (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <root xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </root>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:root xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:root>, true)
    xpath.checkStep (req1, response, chain, 0)
    xpath.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test("In an XPath test, if the XPath resolves to false the uriLevel should be -1 (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    assert (xpath.checkStep (req1, response, chain, 0) == -1)
    assert (xpath.checkStep (req2, response, chain, 1) == -1)
  }

  test("In an XPath test, if the XPath resolves to false the contentErrorPriority should be set (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 101, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    xpath.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 101)
    assert (req2.contentErrorPriority == 101)
  }


  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentErrorCode == 400)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentErrorCode == 400)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with a message of 'Expecting '+XPATH (XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("Expecting if (/tst:root) then true() else false()"))
    assert (req1.contentErrorCode == 400)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("Expecting if (/tst:root) then true() else false()"))
    assert (req1.contentErrorCode == 400)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with a message of 'Expecting '+XPATH (XPath 2) with set code") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", None, Some(401), context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("Expecting if (/tst:root) then true() else false()"))
    assert (req1.contentErrorCode == 401)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("Expecting if (/tst:root) then true() else false()"))
    assert (req1.contentErrorCode == 401)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message(XPath 2)") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", Some("/tst:root was expected"), None, context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 400)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 400)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message(XPath 2) and setCode") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", Some("/tst:root was expected"), Some(401), context, 2, 10, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 401)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 401)
  }

  test("In an XPath test, if the XPath resolves to false the request should contain a SAXParseException, with set message(XPath 2), setCode, and setPriorityAssertion") {
    val context = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", "if (/tst:root) then true() else false()", Some("/tst:root was expected"), Some(401), context, 2, 101, Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml",
                       <foot xmlns="http://test.org/test">
                         <child attribute="value"/>
                       </foot>, true)
    val req2 = request("PUT", "/a/b", "application/xml",
                       <tst:foot xmlns:tst="http://test.org/test">
                         <tst:child attribute="value"/>
                         <tst:child attribute="value2"/>
                       </tst:foot>, true)
    xpath.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])
    assert (req1.contentError.getMessage.contains("/tst:root was expected"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 101)

    xpath.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
    assert (req2.contentError.getMessage.contains("/tst:root was expected"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 101)
  }

  test ("An XSL should correctly transfrom request XML (XSL 1.0)") {
    val xsl = new XSL("XSL", "XSL", xsl1Templates, 10, Array[Step]())
    val req = request("PUT", "/a/b", "application/xml",
                      <foot xmlns="http://test.org/test">
                      <child attribute="value"/>
                      </foot>, true)
    xsl.checkStep (req, response, chain, 0)
    val transXML = XML.load(req.getInputStream())
    assert (transXML.label == "success")
    assert (transXML.namespace == "http://www.rackspace.com/repose/wadl/checker/step/test")
    assert ((transXML \ "@didIt").text == "true")
  }

  test ("An XSL should correctly transfrom request XML (XSL 2.0)") {
    val xsl = new XSL("XSL", "XSL", xsl2Templates, 10, Array[Step]())
    val req = request("PUT", "/a/b", "application/xml",
                      <foot xmlns="http://test.org/test">
                      <child attribute="value"/>
                      </foot>, true)
    xsl.checkStep (req, response, chain, 0)
    val transXML = XML.load(req.getInputStream())
    assert (transXML.label == "success")
    assert (transXML.namespace == "http://www.rackspace.com/repose/wadl/checker/step/test")
    assert ((transXML \ "@didIt").text == "true")
    assert ((transXML \ "@currentTime").text.contains(":"))
  }

  test ("In a header step, if the header is available then the uri level should stay the same.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header step, if the header is available then the uri level should stay the same in the context.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    assert (header.checkStep (req1, response, chain, StepContext()).get.uriLevel == 0)
    assert (header.checkStep (req2, response, chain, StepContext(1)).get.uriLevel == 1)
  }

  test ("In a header step, if the header is available and a captureHeader is set the value should be caught in the captureheader") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))

    val ctx = header.checkStep (req1, response, chain, StepContext())
    assert(ctx.get.requestHeaders("Foo") == List("Set"))
  }

  test ("In a header step, if the header is available and a captureHeader is set the value should be caught in the captureheader (multiple values)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Sam", "Sat")))

    val ctx = header.checkStep (req1, response, chain, StepContext())
    assert(ctx.get.requestHeaders("Foo") == List("Set", "Sam", "Sat"))
  }


  test ("In a header step, if the header is not available the context should be set to None.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In a header step, if the header is available, but does not match the value...context should be set to None") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Bar")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
  }

  test ("In a header step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite", "Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet", "Standing")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header step, if the header is available then the contentErrorPriority should be -1. (Multiple Headers)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite", "Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet", "Standing")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header step, if the header is available then the uri level should stay the same. (Multiple Items in a single header)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite, Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet, Standing")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header step, if the header is available then the contentErrorPriority should be -1. (Multiple Items in a single header)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite, Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet, Standing")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header step, if a header exists, but the header does not match the regex the urilevel should be set to -1.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header step, if a header exists, but the header does not match the contentErrorPriority should be set.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 1001, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1001)
    assert (req2.contentErrorPriority == 1001)
  }

  test ("In a header step, if a header exists, but the header does not match the regex the urilevel should be set to -1. (Multiple Headers)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header step, if a header exists, but the header does not match the contentErrorPriority should be set. (Multiple Headers)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 1002, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1002)
    assert (req2.contentErrorPriority == 1002)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the urilevel should be set to -1. (Multiple Items in a single header)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the contentErrorPriority should be set. (Multiple Items in a single header)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 1003, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1003)
    assert (req2.contentErrorPriority == 1003)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Very Bad Header"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Very Bad Header"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Very Bad Header"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code, Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Very Bad Header"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Very Bad Header"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Very Bad Header"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code, Custom Message, Error Priority)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Very Bad Header"), Some(401), 100, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Very Bad Header"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 100)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Very Bad Header"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 100)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Headers)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Headers, Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Headers, Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Bad?@!"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad?@!"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad?@!"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Headers, Custom Message, Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Bad?@!"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad?@!"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad?@!"))
    assert (req2.contentErrorCode == 401)
  }


  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Headers, Custom Message, Custom Code, contentErrorPriority)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Bad?@!"), Some(401), 1011, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad?@!"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 1011)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad?@!"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 1011)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Items in a single header)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Items in a single header, Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Items in a single header, Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What?"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    assert (req1.contentError.getMessage.contains("What?"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
    assert (req2.contentError.getMessage.contains("What?"))
  }

  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Items in a single header, Custom Message, Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What?"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("What?"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("What?"))
  }


  test ("In a header step, if a header exists, but the header does not match the the regex the requst should conatin an Exception. (Multiple Items in a single header, Custom Message, Custom Code, ContentErrorPriority)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What?"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set, Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat, Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("What?"))
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("What?"))
    assert (req2.contentErrorPriority == 10)
  }

  test ("In a header step, if a header is not found, the urilevel should be set to -1.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header step, if a header is not found, the contentErrorPriority should be set") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 25, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 25)
    assert (req2.contentErrorPriority == 25)
  }

  test ("In a header step, if a header is not found, the request should contain an Exception.") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header is not foound, the request should contain an Exception. (Custom Code)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header is not foound, the request should contain an Exception. (Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What Header?"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("What Header?"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("What Header?"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header step, if a header is not foound, the request should contain an Exception. (Custom Code, Custom Message)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What Header?"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("What Header?"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("What Header?"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header step, if a header is not foound, the request should contain an Exception. (Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new Header("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("What Header?"), Some(401), 10123, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("What Header?"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10123)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("What Header?"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10123)
  }


  test ("In an XSD header step, if the header is available then the uri level should stay the same.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the uri level should stay the same in the context.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, StepContext()).get.uriLevel == 0)
    assert (header.checkStep (req2, response, chain, StepContext(1)).get.uriLevel == 1)
  }

  test ("In an XSD header step, if the header is available and a capture header is set, the value should be set in the capture header.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    val ctx1 = header.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = header.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo") == List("28d42e00-e25a-11e1-9897-efbf2fa68353"))
    assert (ctx2.requestHeaders("Foo") == List("2fbf4592-e25a-11e1-bae1-93374682bd20"))
  }

  test ("In an XSD header step, if the header is available and a capture header is set, the value should be set in the capture header (multiple values).") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "716ab810-b61e-11e4-bee7-28cfe92134e7")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20", "729a6aa0-b61e-11e4-a8e2-28cfe92134e7", "74272958-b61e-11e4-9e31-28cfe92134e7")))

    val ctx1 = header.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = header.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo") == List("28d42e00-e25a-11e1-9897-efbf2fa68353", "716ab810-b61e-11e4-bee7-28cfe92134e7"))
    assert (ctx2.requestHeaders("Foo") == List("2fbf4592-e25a-11e1-bae1-93374682bd20", "729a6aa0-b61e-11e4-a8e2-28cfe92134e7", "74272958-b61e-11e4-9e31-28cfe92134e7"))
  }


  test ("In an XSD header step, if the header is not available then the context should be set to None") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-I"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-I"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In an XSD header step, if the header is available but does not match the XSD the context should be set to None") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-FOOO-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20-998373-FFF")))

    assert(header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert(header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD header step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","ed2127f6-327b-11e2-abc3-ebcd8ddbb97b")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","f5bc95d0-327b-11e2-9f31-e79a818b84c8")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1. (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","ed2127f6-327b-11e2-abc3-ebcd8ddbb97b")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","f5bc95d0-327b-11e2-9f31-e79a818b84c8")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD header step, if the header is available then the uri level should stay the same. (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, ed2127f6-327b-11e2-abc3-ebcd8ddbb97b")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, f5bc95d0-327b-11e2-9f31-e79a818b84c8")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1. (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, ed2127f6-327b-11e2-abc3-ebcd8ddbb97b")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, f5bc95d0-327b-11e2-9f31-e79a818b84c8")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPriority should be set.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "ed2127f6-327b-11e2-abc3ebcd8ddbb97")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20", "f5bc95d0327b11e29f31e79a818b84c8")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPriority should be set(Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "ed2127f6-327b-11e2-abc3ebcd8ddbb97")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20", "f5bc95d0327b11e29f31e79a818b84c8")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, ed2127f6-327b-11e2-abc3ebcd8ddbb97")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, f5bc95d0327b11e29f31e79a818b84c8")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPriority should be set (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, ed2127f6-327b-11e2-abc3ebcd8ddbb97")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, f5bc95d0327b11e29f31e79a818b84c8")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is not available then the uri level should be -1") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is not available then the contentErrorPriority should be set") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 100, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 100)
    assert (req2.contentErrorPriority == 100)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bad XSD Header"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad XSD Header"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad XSD Header"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Message, Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bad XSD Header"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad XSD Header"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad XSD Header"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Message, Custom Code, ContentPriority)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bad XSD Header"), Some(401), 1001, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Bad XSD Header"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 1001)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Bad XSD Header"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 1001)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d","28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274","2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers, Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d","28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274","2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers, Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bste?"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d","28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274","2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    assert (req1.contentError.getMessage.contains("Bste?"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
    assert (req2.contentError.getMessage.contains("Bste?"))
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers, Custom Code, Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bste?"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d","28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274","2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("Bste?"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("Bste?"))
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers, Custom Code, Custom Message, contentErrorPriority)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Bste?"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d","28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274","2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("Bste?"))
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("Bste?"))
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header, Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header, Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Abc"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Abc"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Abc"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header, Custom Code, Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Abc"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Abc"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Abc"))
    assert (req2.contentErrorCode == 401)
  }


  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header, Custom Code, Custom Message, ContentPrirority)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Abc"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("503e5d36-327c-11e2-80d2-33e47888902d, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("5cbcd858-327c-11e2-82e9-27eb85d59274, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Abc"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Abc"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception (Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception (Custom Message)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Xyz"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Xyz"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Xyz"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception (Custom Message, Custom Code)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Xyz"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Xyz"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Xyz"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception (Custom Message, Custom Code, Content Priority)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Xyz"), Some(401), 25, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Xyz"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 25)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Xyz"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 25)
  }

  test ("In a header any step, if the header is available then the uri level should stay the same.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header any step, if the header is available then the uri level should stay the same in the context.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    assert (header.checkStep (req1, response, chain, StepContext()).get.uriLevel == 0)
    assert (header.checkStep (req2, response, chain, StepContext(1)).get.uriLevel == 1)
  }

  test ("In a header any step, if the header is available and a capture header is set, the matching values should be caught in the capture header") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Sam", "Foo", "Bar")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Baz", "Bar", "Sat")))

    val ctx1 = header.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = header.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo")  ==  List("Set", "Sam"))
    assert (ctx2.requestHeaders("Foo") ==  List("Sat"))
  }

  test ("In a header any step, if the header is available but values do not match the capture header should be set to None.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Foo", "Bar")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Baz", "Bar")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In a header any step, if the header is not available the capture header should be set to None.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Foo", "Bar")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Baz", "Bar")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In a header any step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header any step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite", "Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet", "Standing")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header any step, if the header is available then the contentErrorPriority should be -1. (Multiple Headers)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite", "Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet", "Standing")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header any step, if the header is available then the uri level should stay the same. (Multiple Items in a single header)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite, Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet, Standing")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header any step, if the header is available then the contentErrorPriority should be -1. (Multiple Items in a single header)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Suite, Station")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Sat", "Sweet, Standing")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header any step, if the header any available then the uri level should stay the same, even if other headers don't match.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Auite", "Xtation")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Xat", "Aweet", "Standing")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a header any step, if the header any available then the contentErrorPriority should be -1, even if other headers don't match.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Set", "Auite", "Xtation")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Xat", "Aweet", "Standing")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the urilevel should be set to -1.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the contentErrorPriority should be set.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the urilevel should be set to -1.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret", "RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat", "LET", "XOO!")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the contntErrorPriority should be set") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret", "RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat", "LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In a header any step, if a header is not found the urilevel should be set to -1.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Ret", "RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Rat", "LET", "XOO!")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header any step, if a header is not found the contentErrorPriority should be set.") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Ret", "RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST"->List("Rat", "LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the urilevel should be set to -1. (Multiple items in a single header)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the contentErrorPriority should be set. (Multiple items in a single header)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the requst should conatin an Exception") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("..E.."), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("..E.."))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("..E.."))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code, Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("..E.."), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("..E.."))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("..E.."))
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if a header exists, but the header does not match the the regex the requst should conatin an Exception (Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("..E.."), Some(401), 111, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("..E.."))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 111)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("..E.."))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 111)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Custom Code)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Custom Msg"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    assert (req1.contentError.getMessage.contains("Custom Msg"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
    assert (req2.contentError.getMessage.contains("Custom Msg"))
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Custom Code, Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Custom Msg"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("Custom Msg"))
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("Custom Msg"))
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("Custom Msg"), Some(401), 1123, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    assert (req1.contentError.getMessage.contains("Custom Msg"))
    assert (req1.contentErrorPriority == 1123)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
    assert (req2.contentError.getMessage.contains("Custom Msg"))
    assert (req2.contentErrorPriority == 1123)
  }

  test ("In a header any step, if the header is not found the request should conatin an Exception") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if the header is not found the request should conatin an Exception (Custom Code)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if the header is not found the request should conatin an Exception (Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMsg"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMsg"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMsg"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if the header is not found the request should conatin an Exception (Custom Code, Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMsg"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMsg"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMsg"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if the header is not found the request should conatin an Exception (Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMsg"), Some(401), 1234, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Ret","RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-HEADER"->List("Rat","LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMsg"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 1234)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMsg"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 1234)
  }


  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Multiple items in a single header)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Multiple items in a single header, Custom Code)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Multiple items in a single header, Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMessage"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMessage"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMessage"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Multiple items in a single header, Custom Code, Custom Message)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMessage"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMessage"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMessage"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In a header any step, if a header exists, but none of the headers match the the regex the requst should conatin an Exception (Multiple items in a single header, Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new HeaderAny("HEADER", "HEADER", "X-TEST-HEADER", "S.*".r, Some("CMessage"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Ret, RET", "ZOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-TEST-HEADER"->List("Rat, LET", "XOO!")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("CMessage"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("CMessage"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same in the context.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, StepContext()).get.uriLevel == 0)
    assert (header.checkStep (req2, response, chain, StepContext(1)).get.uriLevel == 1)
  }

  test ("In an XSD any header step, if the header is available and a capture header is set, the matching values should be caught in the capture header.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "FOO", "eab81b2c-b623-11e4-b090-28cfe92134e7")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("BAR", "2fbf4592-e25a-11e1-bae1-93374682bd20", "BAZ", "eab81b2c-b623-11e4-b090-28cfe9213zzz")))

    val ctx1 = header.checkStep (req1, response, chain, StepContext()).get
    val ctx2 = header.checkStep (req2, response, chain, StepContext(1)).get

    assert (ctx1.requestHeaders("Foo")  ==  List("28d42e00-e25a-11e1-9897-efbf2fa68353", "eab81b2c-b623-11e4-b090-28cfe92134e7"))
    assert (ctx2.requestHeaders("Foo")  ==  List("2fbf4592-e25a-11e1-bae1-93374682bd20"))
  }

 test ("In an XSD any header step, if the header is not available then the context should be set to None.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-I"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-I"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In an XSD any header step, if the header is available but values do not match then the context should be set to None.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, None, Some("Foo"), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("FOO")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("BAR", "BAZ")))

    assert (header.checkStep (req1, response, chain, StepContext()).isEmpty)
    assert (header.checkStep (req2, response, chain, StepContext(1)).isEmpty)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }


  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1. (Multiple Headers)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same. (Multiple Items single header)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, 9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, 9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1 (Multiple Items single header)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, 9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, 9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test ("In an XSD any header step, if the header is available then the uri level should stay the same, even if other headers don't match") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff856d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce6353256d5-11e2-a5a1b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1, even if other headers don't match") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff856d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce6353256d5-11e2-a5a1b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the contentErrorPrioirty should be set") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple headers, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353","foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20","bar")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the content error priority should be set (Multiple headers, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353","foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20","bar")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple items in single header, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the content error priority should be set (Multiple items in single header, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 100, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 100)
    assert (req2.contentErrorPriority == 100)
  }


  test ("In an XSD any header step, if the header is not available, the uri level should be -1") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("509acc44-56d8-11e2-a542-cbb2f2d12c96")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("5be28dda-56d8-11e2-9cf0-4728d4210e49")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is not available, the content error priority should be set") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 1001, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("509acc44-56d8-11e2-a542-cbb2f2d12c96")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("5be28dda-56d8-11e2-9cf0-4728d4210e49")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1001)
    assert (req2.contentErrorPriority == 1001)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Code)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Code, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (Custom Code, Custom Message, ContentErrorPriority)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 8675309, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 8675309)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 8675309)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect, Custom Code)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect, Custom Code, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect, Custom Code, Custom Message, Content Error Priority)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect, Custom Code)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect, Custom Code, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect, Custom Code, Custom Message, Priority)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 10)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception (Custom Code)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, None, Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception (Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), None, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 400)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 400)
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception (Custom Code, Custom Message)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception (Custom Code, Custom Message, Content Error Priority)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchema, Some("Custom Message"), Some(401), 101, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    assert (req1.contentError.getMessage.contains("Custom Message"))
    assert (req1.contentErrorCode == 401)
    assert (req1.contentErrorPriority == 101)
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
    assert (req2.contentError.getMessage.contains("Custom Message"))
    assert (req2.contentErrorCode == 401)
    assert (req2.contentErrorPriority == 101)
  }

  test ("In a JSON Schema test, if the content contains valid JSON, the uriLevel should stay the same.") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : 38
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Rachel",
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    assert(jsonSchema.checkStep (req1, response, chain, 0) == 0)
    assert(jsonSchema.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In a JSON Schema test, if the content contains valid JSON, the contentErrorPrirority should be -1.") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : 38
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Rachel",
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In a JSON Schema test, if the content contains invalid JSON, the uriLevel should be -1") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    assert(jsonSchema.checkStep (req1, response, chain, 0) == -1)
    assert(jsonSchema.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In a JSON Schema test, if the content contains invalid JSON, the contentErrorPriority should be set") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 100, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 100)
    assert (req2.contentErrorPriority == 100)
  }


  test ("In a JSON Schema test, if the content contains invalid JSON, the request should conatain a ProcessingException") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.getCause.isInstanceOf[ProcessingException])
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.getCause.isInstanceOf[ProcessingException])
  }

  test ("In a JSON Schema test, if the content contains invalid JSON, error messages should be concise") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (!req1.contentError.getMessage.contains("\n"))
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (!req2.contentError.getMessage.contains("\n"))
  }

  test ("In a JSON Schema test, if the content contains invalid JSON, the request should conatain a ProcessingException which refernces the invalid attribute") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.getCause.isInstanceOf[ProcessingException])
    assert (req1.contentError.getMessage().contains("/age"))
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.getCause.isInstanceOf[ProcessingException])
    assert (req2.contentError.getMessage().contains("/firstName"))
  }

  test ("In a JSON Schema test, if the content contains invalid JSON, that references an invalid attribute, the error message must be concise") {
    val jsonSchema = new JSONSchema("JSONSchema","JSONSchema", testJSONSchema, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : "38"
    }
                        """, true)
    val req2 = request ("PUT", "/a/b", "application/json", """
    {
         "firstName" : false,
         "lastName" : "Kraft",
         "age" : 32
    }
                        """, true)
    jsonSchema.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.getCause.isInstanceOf[ProcessingException])
    assert (!req1.contentError.getMessage.contains("\n"))
    jsonSchema.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.getCause.isInstanceOf[ProcessingException])
    assert (!req2.contentError.getMessage.contains("\n"))
  }

}
