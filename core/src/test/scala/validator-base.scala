package com.rackspace.com.papi.components.checker

import scala.xml._

import java.io.ByteArrayInputStream
import java.io.StringWriter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream
import javax.servlet.FilterChain

import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.json.simple.parser.JSONParser

import scala.collection.mutable.HashMap

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._

import com.rackspace.com.papi.components.checker.util.XMLParserPool
import com.rackspace.com.papi.components.checker.util.JSONParserPool
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool

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

object Converters {
  //
  //  Convert a W3C dom node to a node seq.
  //
  implicit def doc2NodeSeq (doc : Document) : NodeSeq = {
    var transf : Transformer = null
    val swriter = new StringWriter()
    try {
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new DOMSource(doc), new StreamResult(swriter))
      XML.loadString (swriter.toString())
    } finally {
      if (transf != null) IdentityTransformPool.returnTransformer(transf)
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

object TestConfig {
  val assertHandler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(), 
                                                                    new AssertResultHandler(),
                                                                    new ServletResultHandler()))


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String, 
             joinXPathChecks : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine)

    config.joinXPathChecks = joinXPathChecks

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension)

    config.xslEngine = xslEngine

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion, checkPlainParams, doXSDGrammarTransform)

    config.enablePreProcessExtension = enablePreProcessExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion, checkPlainParams)

    config.doXSDGrammarTransform = doXSDGrammarTransform

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion)
    config.checkPlainParams = checkPlainParams

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements)
    config.xpathVersion = xpathVersion

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean, checkXSDGrammar : Boolean, checkElements : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar)
    config.checkElements = checkElements

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean, checkXSDGrammar : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed)
    config.checkXSDGrammar = checkXSDGrammar

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean) : Config = {
    val config = apply(saxoneeValidation, wellFormed)
    config.removeDups = removeDups

    config
  }

  def apply (saxoneeValidation : Boolean, wellFormed : Boolean) : Config = {
    val config = new Config
    config.resultHandler = assertHandler
    config.useSaxonEEValidation = saxoneeValidation
    config.checkWellFormed = wellFormed

    config
  }

  def apply() : Config = {
    apply(false, false)
  }
}

class BaseValidatorSuite extends FunSuite {

  val assertConfig = TestConfig()
  val assertConfigSaxonEE = TestConfig(true, false)

  def request(method : String, url : String) : HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])
    val reqAttribs = new HashMap[String, Object]

    //
    //  Set null for expected attributes
    //
    reqAttribs += (PARSED_XML -> null)
    reqAttribs += (PARSED_JSON -> null)
    reqAttribs += (CONTENT_ERROR -> null)

    when(req.getCharacterEncoding()).thenReturn("UTF-8")
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

  def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean) : HttpServletRequest = {
    val req = request (method, url, contentType, content)
    var xmlParser : DocumentBuilder = null
    var jsonParser : JSONParser = null

    try {
      if (parseContent) {
        contentType match {
          case "application/xml"  =>
            xmlParser = XMLParserPool.borrowParser
            req.setAttribute (PARSED_XML, xmlParser.parse(new ByteArrayInputStream(content.getBytes())))
          case "application/json" =>
            jsonParser = JSONParserPool.borrowParser
            req.setAttribute (PARSED_JSON, jsonParser.parse(content))
        }
      }
    } finally {
      if (xmlParser != null) XMLParserPool.returnParser(xmlParser)
      if (jsonParser != null) JSONParserPool.returnParser(jsonParser)
    }

    return req
  }

  def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean, headers : Map[String, String]) : HttpServletRequest = {
    val req = request(method, url, contentType, content, parseContent)

    when(req.getHeader(anyString())).thenAnswer(new Answer[String] {
      val h = headers

      override def answer(invocation : InvocationOnMock) : String = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        headers.getOrElse(key, null)
      }
    })

    return req
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean, headers : Map[String, String]) : HttpServletRequest = {
    request (method, url, contentType, content.toString(), parseContent, headers)
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean) : HttpServletRequest = {
    request (method, url, contentType, content.toString(), parseContent)
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
