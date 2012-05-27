package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._

import org.scalatest.FunSuite
import org.scalatest.TestFailedException

import org.mockito.Mockito._

import org.w3c.dom.Document

/**
 * The assert handler throws an ValidationFailedExecption whenever
 * the request in invalid.
 */
class AssertResultHandler extends ResultHandler {
  def init(checker : Option[Document]) : Unit = {}
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {
    if (!result.valid) {
      throw new ResultFailedException("Validation failed",req,resp,chain,result)
    }
  }
}

/**
 * Exception thrown by the assert result handler,
 * if a request fails to validate
 */
class ResultFailedException(val msg : String, val req : CheckerServletRequest,
                            val resp : CheckerServletResponse, val chain : FilterChain, val result : Result)
   extends Exception(msg){}

class BaseValidatorSuite extends FunSuite {

  val assertHandler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(), 
                                                                    new AssertResultHandler(),
                                                                    new ServletResultHandler()))

  val assertConfig = new Config
  assertConfig.resultHandler = assertHandler

  val assertConfigSaxonEE = new Config
  assertConfigSaxonEE.resultHandler = assertHandler
  assertConfigSaxonEE.useSaxonEEValidation  = true


  def request(method : String, url : String) : HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])

    when(req.getMethod()).thenReturn(method)
    when(req.getRequestURI()).thenReturn(url)
    return req
  }

  def response : HttpServletResponse = mock(classOf[HttpServletResponse]);

  def chain : FilterChain = mock(classOf[FilterChain])

  def assertResultFailed(f : => Any) : Option[ResultFailedException] = {
    val expectMsg = "Expected validation exception caused by ResultFailed"
    val result : Option[ResultFailedException] = try {
      f
      None
    } catch {
      case v : ValidatorException =>
        val cause = v.getCause()
        if (cause == null) {
          throw new TestFailedException(Some(expectMsg+" but got null cause"), None, 4)
        } else if (!cause.isInstanceOf[ResultFailedException]) {
          throw new TestFailedException(Some(expectMsg+" but got "+cause), Some(cause), 4)
        } else {
          Some(cause.asInstanceOf[ResultFailedException])
        }
      case t : Throwable =>
        throw new TestFailedException(Some(expectMsg+" but got "+t), Some(t), 4)
    }
    if (result == None) {
      throw new TestFailedException(Some(expectMsg+" but got no exception."), None, 4)
    }
    result
  }

  def assertResultFailed(f : => Any, code : Int) : Unit = {
    var result : ErrorResult = null
    assertResultFailed(f).get.result match {
      case mfr : MultiFailResult =>
        result = mfr.reduce.get.asInstanceOf[ErrorResult]
      case other : ErrorResult =>
        result = other
    }
    if (result.code != code) {
      throw new TestFailedException(Some("Expected error code "+code+" but got "+result.code), None, 4)
    }
  }
}
