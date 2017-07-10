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
package com.rackspace.com.papi.components.checker

import java.io.{ByteArrayInputStream, File}
import java.util.Enumeration
import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.xml.parsers.DocumentBuilder

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.rackspace.com.papi.components.checker.servlet.ByteArrayServletInputStream
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._
import com.rackspace.com.papi.components.checker.step.results.ErrorResult
import com.rackspace.com.papi.components.checker.util.{ObjectMapperPool, XMLParserPool}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException

import scala.collection.JavaConversions._
import scala.collection.immutable.TreeMap
import scala.collection.mutable.HashMap
import scala.language.implicitConversions
import scala.xml._

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
           "firstName" : "RRRRR",
           "stuff" : {
             "thing" : true,
             "string" : "A String",
             "array" : [ 1, 2, 3, 4],
             "array2" : [ 1.2, 2.3, 3.4, 4.5],
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

  val goodXML_XSD2 = <tst:a xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
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

  val goodJSON_Schema3 = """
    {
         "firstName" : "Britten",
         "lastName" : "Williams",
         "age" : 2
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
    reqAttribs += (CONTENT_ERROR_PRIORITY -> null)

    when(req.getCharacterEncoding).thenReturn("UTF-8")
    when(req.getMethod).thenReturn(method)
    when(req.getRequestURI).thenReturn(url)
    when(req.getAttribute(anyString())).thenAnswer(new Answer[Object] {
      val attribs = reqAttribs

      override def answer(invocation : InvocationOnMock) : Object  = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        attribs.getOrElse(key, null)
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

    req
  }

  def request(method : String, url : String, contentType : String) : HttpServletRequest = {

    val req = request(method, url)
    when(req.getContentType).thenReturn(contentType)

    req
  }

  def request(method : String, url : String, contentType : String, content : String) : HttpServletRequest = {
    val req = request(method, url, contentType)

    if (content == null) {
      when(req.getInputStream).thenReturn(null)
    } else {
      when(req.getInputStream).thenReturn(new ByteArrayServletInputStream(content))
    }

    req
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq) : HttpServletRequest = {
    val req = request(method, url,contentType)

    when(req.getInputStream).thenReturn(new ByteArrayServletInputStream(content))

    req
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
            req.setAttribute (PARSED_XML, xmlParser.parse(new ByteArrayInputStream(content.getBytes)))
          case "application/json" =>
            jsonParser = ObjectMapperPool.borrowParser
            req.setAttribute (PARSED_JSON, jsonParser.readValue(content,classOf[JsonNode]))
        }
      }
    } finally {
      if (xmlParser != null) XMLParserPool.returnParser(xmlParser)
      if (jsonParser != null) ObjectMapperPool.returnParser(jsonParser)
    }

    req
  }

  def request(method : String, url : String, contentType : String, content : String, parseContent : Boolean, headers : Map[String, List[String]]) : HttpServletRequest = {
    val req = request(method, url, contentType, content, parseContent)

    val caseInSensitiveHeaders = new TreeMap[String, List[String]]()(Ordering.by(_.toLowerCase)) ++ headers
    when(req.getHeader(anyString())).thenAnswer(new Answer[String] {
      override def answer(invocation : InvocationOnMock) : String = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        caseInSensitiveHeaders.getOrElse(key, List(null))(0)
      }
    })

    when(req.getHeaders(anyString())).thenAnswer(new Answer[Enumeration[String]] {
      override def answer(invocation : InvocationOnMock) : Enumeration[String] = {
        val key = invocation.getArguments()(0).asInstanceOf[String]
        caseInSensitiveHeaders.getOrElse(key, List()).iterator
      }
    })

    when(req.getHeaderNames).thenAnswer(new Answer[Enumeration[String]] {
      override def answer(invocation : InvocationOnMock) : Enumeration[String] = {
        caseInSensitiveHeaders.keySet.iterator
      }
    })

    req
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean, headers : Map[String, List[String]]) : HttpServletRequest = {
    request (method, url, contentType, content.toString, parseContent, headers)
  }

  def request(method : String, url : String, contentType : String, content : NodeSeq, parseContent : Boolean) : HttpServletRequest = {
    request (method, url, contentType, content.toString, parseContent)
  }

  def response : HttpServletResponse = mock(classOf[HttpServletResponse])

  def chain : FilterChain = mock(classOf[FilterChain])

  def assertResultFailed(f : => Any) : Option[ResultFailedException] = {
    val expectMsg = "Expected validation exception caused by ResultFailed"
    val result : Option[ResultFailedException] = try {
      f
      None
    } catch {
      case v : ValidatorException =>
        val cause = v.getCause
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
    if (result.isEmpty) {
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
    assert(result.context == head.context, "First item in allResults list did not match main Result" )
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
      if (!result.message.toUpperCase().contains(m.toUpperCase())) {
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
