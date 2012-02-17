package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._

import org.scalatest.FunSuite
import org.scalatest.TestFailedException

import org.mockito.Mockito._

/**
 * The assert handler throws an ValidationFailedExecption whenever
 * the request in invalid.
 */
class AssertResultHandler extends ResultHandler {
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, result : Result)  : Unit = {
    if (!result.valid) {
      throw new ResultFailedException("Validation failed",req,resp,result)
    }
  }
}

/**
 * Exception thrown by the assert result handler,
 * if a request fails to validate
 */
class ResultFailedException(val msg : String, val req : CheckerServletRequest,
                            val resp : CheckerServletResponse, val result : Result)
   extends Exception(msg){}

class BaseValidatorSuite extends FunSuite {

  val assertHandler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(), 
                                                                    new AssertResultHandler()))

  def request(method : String, url : String) : HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])

    when(req.getMethod()).thenReturn(method)
    when(req.getRequestURI()).thenReturn(url)
    return req
  }

  def response : HttpServletResponse = mock(classOf[HttpServletResponse]);

  def assertResultFailed(f : => Any) : Unit = {
    val expectMsg = "Expected validation exception caused by ResultFailed"
    val result = try {
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
          Some(v)
        }
      case t : Throwable =>
        throw new TestFailedException(Some(expectMsg+" but got "+t), Some(t), 4)
    }
    if (result == None) {
      throw new TestFailedException(Some(expectMsg+" but got no exception."), None, 4)
    }
  }
}
