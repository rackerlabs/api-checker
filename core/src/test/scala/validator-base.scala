package com.rackspace.com.papi.components.checker

import scala.xml._

import java.io.File
import java.io.ByteArrayInputStream
import java.io.StringWriter

import java.util.Enumeration

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream
import javax.servlet.FilterChain

import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

import scala.collection.mutable.HashMap

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._

import com.rackspace.com.papi.components.checker.util.XMLParserPool
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool

import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

import scala.language.implicitConversions
import scala.collection.JavaConversions._

import org.w3c.dom.Document

/**
 * The assert handler throws an ValidationFailedExecption whenever
 * the request in invalid.
 */
class AssertResultHandler extends ResultHandler {
  def init(validator : Validator, checker : Option[Document]) : Unit = {}
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

  implicit def nodeSeq2Doc (n : NodeSeq) : Document = {
    var transf : Transformer = null
    try {
      val result = new DOMResult()
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new StreamSource(new ByteArrayInputStream(n.toString().getBytes())),
                       result)
      result.getNode.asInstanceOf[Document]
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
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean, preserveRequestBody : Boolean,
             maskRaxRoles403 : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension, enableRaxRolesExtension, preserveRequestBody)

    config.maskRaxRoles403 = maskRaxRoles403

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean, preserveRequestBody : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension, enableRaxRolesExtension)

    config.preserveRequestBody = preserveRequestBody

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension)

    config.enableRaxRolesExtension = enableRaxRolesExtension

    config
  }


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
                       checkJSONGrammar)

    config.enableIgnoreJSONSchemaExtension = enableIgnoreJSONSchemaExtension

    config
  }


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension)

    config.checkJSONGrammar = checkJSONGrammar

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension)

    config.enableMessageExtension = enableMessageExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders)

    config.enableIgnoreXSDExtension = enableIgnoreXSDExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks)

    config.checkHeaders = checkHeaders

    config
  }

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
    config.setXSDEngine( if ( saxoneeValidation ) { "SaxonEE" } else { "Xerces" } )
    config.checkWellFormed = wellFormed

    config
  }

  def apply() : Config = {
    apply(false, false)
  }
}

class BaseValidatorSuite extends FunSuite {

  //
  //  Common test vars
  //

  val goodXML = <some_xml att='1' xmlns='test.org'>
                  <an_element>
                    <another_element />
                  </an_element>
                </some_xml>

  val goodJSON = """
       {
           "stuff" : {
             "thing" : true,
             "string" : "A String",
             "array" : [ 1, 2, 3, 4],
             "obj" : {
               "a" : "A",
               "b" : "B"
             },
            "null" : null
           }
       }
  """

  val goodXML_XSD1 = <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                        <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType>URL_FAIL</stepType>
                        <even>22</even>
                     </e>

  val goodXML_XSD2 = <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                        id="21f1fcf6-bf38-11e1-878e-133ab65fcec3"
                        stepType="ACCEPT"
                        even="22"/>

  val goodJSON_Schema1 = """
    {
         "firstName" : "Jorge",
         "lastName" : "Williams",
         "age" : 38
    }
  """

  val goodJSON_Schema2 = """
    {
         "firstName" : "Rachel",
         "lastName" : "Kraft",
         "age" : 32
    }
  """

  val localWADLURI = (new File(System.getProperty("user.dir"),"mywadl.wadl")).toURI.toString



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
    var jsonParser : ObjectMapper = null

    when(req.getHeaders(anyString())).thenAnswer(new Answer[Enumeration[String]] {

      class EnumResult() extends Enumeration[String] {
        override def hasMoreElements = false
        override def nextElement = null
      }
      override def answer(invocation : InvocationOnMock) : Enumeration[String] = new EnumResult()
    })

    try {
      if (parseContent) {
        contentType match {
          case "application/xml"  =>
            xmlParser = XMLParserPool.borrowParser
            req.setAttribute (PARSED_XML, xmlParser.parse(new ByteArrayInputStream(content.getBytes())))
          case "application/json" =>
            jsonParser = ObjectMapperPool.borrowParser
            req.setAttribute (PARSED_JSON, jsonParser.readValue(content,classOf[JsonNode]))
        }
      }
    } finally {
      if (xmlParser != null) XMLParserPool.returnParser(xmlParser)
      if (jsonParser != null) ObjectMapperPool.returnParser(jsonParser)
    }

    return req
  }

  def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean, headers : Map[String, List[String]]) : HttpServletRequest = {
    val req = request(method, url, contentType, content, parseContent)

    when(req.getHeader(anyString())).thenAnswer(new Answer[String] {
      override def answer(invocation : InvocationOnMock) : String = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        headers.getOrElse(key, null)(0)
      }
    })

    when(req.getHeaders(anyString())).thenAnswer(new Answer[Enumeration[String]] {
      override def answer(invocation : InvocationOnMock) : Enumeration[String] = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        headers.getOrElse(key, List()).iterator
      }
    })

    return req
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean, headers : Map[String, List[String]]) : HttpServletRequest = {
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

  //
  // Verify the allResults method is the correct size & has
  // the correct head element
  //
  def assertAllResultsCorrect( f : => Any, size : Int ) : Unit = {
    var result : ErrorResult = null

    assertResultFailed(f).get.result match {
      case other : ErrorResult =>
        result = other
    }

    val list = result.allResults.toList

    val head = list.head match {
      case e : ErrorResult => e
    }

    assert(list.size == size, "Bad list size")
    assert(result.code == head.code, "First item in allResults list did not match main Result" )
    assert(result.message == head.message, "First item in allResults list did not match main Result" )
    assert(result.uriLevel == head.uriLevel, "First item in allResults list did not match main Result" )
  }

  def assertResultFailed(f : => Any, code : Int) : Unit = {
    var result : ErrorResult = null
    assertResultFailed(f).get.result match {
      case other : ErrorResult =>
        result = other
    }

    if (result.code != code) {
      throw new TestFailedException(Some("Expected error code "+code+" but got "+result.code), None, 4)
    }
  }

  def assertResultFailed(f : => Any, code : Int, message : String) : Unit = {
    var result : ErrorResult = null
    assertResultFailed(f).get.result match {
      case other : ErrorResult =>
        result = other
    }

    if (result.code != code) {
      throw new TestFailedException(Some("Expected error code "+code+" but got "+result.code), None, 4)
    }
    if (result.message != message) {
      throw new TestFailedException(Some("Expected error message '"+message+"' but got '"+result.message+"'"), None, 4)
    }
  }

  def assertResultFailed(f : => Any, code : Int, message : List[String]) : Unit = {
    var result : ErrorResult = null
    assertResultFailed(f).get.result match {
      case other : ErrorResult =>
        result = other
    }

    if (result.code != code) {
      throw new TestFailedException(Some("Expected error code "+code+" but got "+result.code), None, 4)
    }
    message.foreach(m => {
      if (!result.message.contains(m)) {
        throw new TestFailedException(Some("Expected error string '"+m+"' in the result message, but it didn't have one. Actual result message: '"+result.message+"'"), None, 4)
      }
    })
  }

  def assertResultFailed(f : => Any, code : Int, headers : Map[String, String]) : Unit = {
    var result : ErrorResult = null
    assertResultFailed(f).get.result match {
      case other : ErrorResult =>
        result = other
    }

    if (result.code != code) {
      throw new TestFailedException(Some("Expected error code "+code+" but got "+result.code), None, 4)
    }
    headers.keys.foreach(k => {
      val v = headers(k)
      if (!result.headers.containsKey(k)) {
        throw new TestFailedException(Some("Expected result header "+k), None, 4)
      }
      if (!result.headers.get(k).equals(v)) {
        throw new TestFailedException(Some("Expected result header "+k+" to match value '"+v+"' but instead got '"+result.headers.get(k)+"'"), None, 4)
      }
    })
  }
}
