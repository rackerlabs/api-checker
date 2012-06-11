package com.rackspace.com.papi.components.checker

import scala.xml._

import java.io.ByteArrayInputStream

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream
import javax.servlet.FilterChain

import scala.collection.mutable.HashMap

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._

import org.scalatest.FunSuite
import org.scalatest.TestFailedException

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

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
 * A Byte Array Servlet input stream
 *
 */
class ByteArrayServletInputStream(val content : String) extends ServletInputStream {
  val bais = new ByteArrayInputStream (content.getBytes())

  def this(n : NodeSeq) {
    this(n.toString())
  }

  override def available : Int = bais.available
  override def close : Unit = bais.close
  override def mark (readLimit : Int) : Unit = bais.mark(readLimit)
  override def markSupported : Boolean = bais.markSupported
  override def read : Int = bais.read
  override def read (b : Array[Byte]) : Int = bais.read(b)
  override def read (b : Array[Byte], o : Int, l : Int) : Int = bais.read(b,o,l)
  override def reset : Unit = bais.reset
  override def skip (n : Long) : Long = bais.skip(n)
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
    val reqAttribs = new HashMap[String, Object]

    //
    //  Set null for expected attributes
    //
    reqAttribs += (PARSED_XML -> null)
    reqAttribs += (CONTENT_ERROR -> null)

    when(req.getMethod()).thenReturn(method)
    when(req.getRequestURI()).thenReturn(url)
    when(req.getAttribute(anyString())).thenAnswer(new Answer[Object] {
      val attribs = reqAttribs

      override def answer(invocation : InvocationOnMock) : Object  = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        attribs(key)
      }
    })
    doAnswer(new Answer[Object] {
      val attribs = reqAttribs

      override def answer(invocation : InvocationOnMock) : Object = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        val obj = invocation.getArguments()(1).asInstanceOf[Object]

        attribs += (key -> obj)
        null
      }
    } ).when(req).setAttribute(anyString(), anyObject())

    return req
  }

  def request(method : String, url : String, contentType : String) : HttpServletRequest = {

    val req = request(method, url)
    when(req.getContentType()).thenReturn(contentType)

    return req
  }

  def request(method : String, url : String, contentType : String, content : String) : HttpServletRequest = {
    val req = request(method, url, contentType)

    when(req.getInputStream()).thenReturn(new ByteArrayServletInputStream(content))

    return req
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq) : HttpServletRequest = {
    val req = request(method, url,contentType)

    when(req.getInputStream()).thenReturn(new ByteArrayServletInputStream(content))

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
