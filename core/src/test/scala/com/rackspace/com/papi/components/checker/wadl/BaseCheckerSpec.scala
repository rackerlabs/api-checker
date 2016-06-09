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
package com.rackspace.com.papi.components.checker.wadl

import java.io.{ByteArrayOutputStream, File}
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec
import com.rackspace.com.papi.components.checker.Config
import org.scalatest.exceptions.TestFailedException

import scala.xml._

class BaseCheckerSpec extends BaseWADLSpec {
  val localWADLURI = (new File(System.getProperty("user.dir"),"mywadl.wadl")).toURI.toString

  val builder = new WADLCheckerBuilder(wadl)

  val stdConfig = new Config
  val dupConfig = new Config

  stdConfig.removeDups = false
  dupConfig.removeDups = true

  private val pathNodesTemplates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResourceAsStream("/xsl/path-nodes.xsl")))

  def allStepsFromStep (checker : NodeSeq, id : String, max : Int = 0) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    val transformer = pathNodesTemplates.newTransformer
    transformer.setParameter("sid",id)
    if (max > 0) {
      transformer.setParameter("max",max)
    }
    transformer.transform (new StreamSource(checker), new StreamResult(bytesOut))
    XML.loadString(bytesOut.toString)
  }

  def allStepsFromStep (checker : NodeSeq, step_fun : (NodeSeq) => NodeSeq, max : Int) : NodeSeq = {
    allStepsFromStep (checker, (step_fun(checker)(0) \ "@id").text, max)
  }

  def allStepsFromStart (checker : NodeSeq) : NodeSeq = {
    allStepsFromStep (checker, (stepsWithType(checker,"START")(0) \ "@id").text)
  }

  def stepsWithType (checker : NodeSeq, nodeType : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@type").text == nodeType)
  }

  def stepsWithNameMatch(checker : NodeSeq, nodeMatch : String, nodeName : String) : NodeSeq = {
    stepsWithMatch(checker, nodeMatch).filter (n => (n \ "@name").text == nodeName)
  }

  def stepsWithMatch (checker : NodeSeq, nodeMatch  : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@match").text == nodeMatch)
  }

  def stepsWithLabel (checker : NodeSeq, labelMatch  : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@label").text == labelMatch)
  }

  def stepsWithMethodFailNotMatch (checker : NodeSeq, notMatch : String) : NodeSeq = {
    stepsWithType(checker,"METHOD_FAIL").filter(n => (n \ "@notMatch").text == notMatch)
  }

  def stepsWithURLFailNotMatch (checker : NodeSeq, notMatch : String) : NodeSeq = {
    stepsWithType(checker,"URL_FAIL").filter(n => (n \ "@notMatch").text == notMatch)
  }

  def stepsWithURLFailNotTypes (checker : NodeSeq, notTypes : String) : NodeSeq = {
    stepsWithType(checker,"URL_FAIL").filter(n => (n \ "@notTypes").text == notTypes)
  }

  def stepsWithURLMatch (checker : NodeSeq, urlMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, urlMatch).filter(n => (n \ "@type").text == "URL")
  }

  def stepsWithURLMatchCapture (checker : NodeSeq, urlMatch  : String, captureHeader : String) : NodeSeq = {
    stepsWithURLMatch (checker, urlMatch).filter(n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithURLXSDMatch (checker : NodeSeq, urlMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, urlMatch).filter(n => (n \ "@type").text == "URLXSD")
  }

  def stepsWithURLXSDMatchCapture (checker : NodeSeq, urlMatch  : String, captureHeader : String) : NodeSeq = {
    stepsWithURLXSDMatch (checker, urlMatch).filter(n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithXPathMatch (checker : NodeSeq, xpathMatch : String) : NodeSeq = {
    stepsWithMatch (checker, xpathMatch).filter(n => (n \ "@type").text == "XPATH")
  }

  def stepsWithXPathMatchCapture (checker : NodeSeq, xpathMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithXPathMatch (checker, xpathMatch).filter(n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithXPathMessageMatch (checker : NodeSeq, xpathMatch : String, message : String) : NodeSeq = {
    stepsWithXPathMatch(checker, xpathMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithXPathCodeMatch (checker : NodeSeq, xpathMatch : String, code : Int) : NodeSeq = {
    stepsWithXPathMatch(checker, xpathMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithXPathMessageCodeMatch (checker : NodeSeq, xpathMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithXPathMessageMatch(checker, xpathMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithMethodMatch (checker : NodeSeq, methodMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, methodMatch).filter(n => (n \ "@type").text == "METHOD")
  }

  def stepsWithReqTypeMatch (checker : NodeSeq, reqTypeMatch : String) : NodeSeq = {
    stepsWithMatch (checker, reqTypeMatch).filter(n => (n \ "@type").text == "REQ_TYPE")
  }

  def stepsWithHeaderMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADER")
  }

  def stepsWithHeaderMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithHeaderCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderMatch(checker, name, headerMatch).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderMatch(checker, name, headerMatch).filter(n => (n \ "@message").text == message)
  }

  def stepsWithHeaderMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderMessageMatch(checker, name, headerMatch, message).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADERXSD")
  }

  def stepsWithHeaderXSDMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderXSDMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithHeaderXSDCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderXSDMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderXSDMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAnyMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADER_ANY")
  }

  def stepsWithHeaderAnyMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderAnyMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithHeaderAnyCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderAnyMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAnyMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderAnyMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderAnyMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderAnyMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDAnyMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADERXSD_ANY")
  }

  def stepsWithHeaderXSDAnyMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderXSDAnyMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithHeaderXSDAnyCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDAnyMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDAnyMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderXSDAnyMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderXSDAnyMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDAnyMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderSingleMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADER_SINGLE")
  }

  def stepsWithHeaderSingleMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderSingleMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }

  def stepsWithHeaderSingleCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderSingleMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderSingleMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderSingleMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderSingleMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderSingleMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDSingleMatch (checker : NodeSeq, name : String, headerMatch : String) : NodeSeq = {
    stepsWithNameMatch (checker, headerMatch, name).filter (n => (n \ "@type").text == "HEADERXSD_SINGLE")
  }

  def stepsWithHeaderXSDSingleMatchCapture (checker : NodeSeq, name : String, headerMatch : String, captureHeader : String) : NodeSeq = {
    stepsWithHeaderXSDSingleMatch(checker, name, headerMatch).filter (n => (n \ "@captureHeader").text == captureHeader)
  }


  def stepsWithHeaderXSDSingleCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDSingleMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDSingleMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderXSDSingleMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderXSDSingleMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDSingleMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithSetHeader (checker : NodeSeq, name : String, value : String) : NodeSeq = {
    stepsWithType(checker, "SET_HEADER").filter (n => (n \ "@name").text == name).filter (n => (n \ "@value").text == value)
  }

  def stepsWithSetHeaderAlways (checker : NodeSeq, name: String) : NodeSeq = {
    stepsWithType(checker, "SET_HEADER_ALWAYS").filter(n => (n \ "@name").text ==name)
  }

  def stepsWithSetHeaderAlways (checker : NodeSeq, name: String, value : String) : NodeSeq = {
    stepsWithType(checker, "SET_HEADER_ALWAYS").filter(n => (n \ "@name").text ==name).filter (n => (n \ "@value").text == value)
  }


  def stepsWithHeaderAll(checker : NodeSeq, name : String) : NodeSeq = {
    stepsWithType(checker, "HEADER_ALL").filter (n => (n \ "@name").text == name)
  }

  def stepsWithHeaderAllValue(checker : NodeSeq, name : String, value : String) : NodeSeq = {
    stepsWithHeaderAll(checker, name).filter (n => (n \ "@matchRegEx").text == value)
  }

  def stepsWithHeaderAllMatchTypes(checker : NodeSeq, name : String, matchTypes : String) : NodeSeq = {
    stepsWithHeaderAll(checker, name).filter (n => (n \ "@match").text == matchTypes)
  }

  def stepsWithHeaderAllValueMessage(checker : NodeSeq, name : String, value : String, message : String) : NodeSeq = {
    stepsWithHeaderAllValue(checker, name, value).filter(n => (n \ "@message").text == message)
  }

  def stepsWithHeaderAllValueMessageCode(checker : NodeSeq, name : String, value : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllValueMessage(checker, name, value, message).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllValueCode(checker : NodeSeq, name : String, value : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllValue(checker, name, value).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllMatchTypesMessage(checker : NodeSeq, name : String, matchTypes : String, message : String) : NodeSeq = {
    stepsWithHeaderAllMatchTypes(checker, name, matchTypes).filter(n => (n \ "@message").text == message)
  }

  def stepsWithHeaderAllMatchTypesMessageCode(checker : NodeSeq, name : String, matchTypes : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllMatchTypesMessage(checker, name, matchTypes, message).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllMatchTypesCode(checker : NodeSeq, name : String, matchTypes : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllMatchTypes(checker, name, matchTypes).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllValueMatchTypes(checker : NodeSeq, name : String, value : String, matchTypes : String) : NodeSeq = {
    stepsWithHeaderAllValue(checker, name, value).filter (n => (n \ "@match").text == matchTypes)
  }

  def stepsWithHeaderAllValueMatchTypesMessage(checker : NodeSeq, name : String, value : String, matchTypes : String, message : String) : NodeSeq = {
    stepsWithHeaderAllValueMatchTypes(checker, name, value, matchTypes).filter(n => (n \ "@message").text == message)
  }

  def stepsWithHeaderAllValueMatchTypesCode(checker : NodeSeq, name : String, value : String, matchTypes : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllValueMatchTypes(checker, name, value, matchTypes).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllValueMatchTypesMessageCode(checker : NodeSeq, name : String, value : String, matchTypes : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderAllValueMatchTypesMessage(checker, name, value, matchTypes, message).filter(n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderAllValueMatchTypesMessageCodeCaptureHeader(checker : NodeSeq, name : String, value : String, matchTypes : String, message : String,
    code : Int, captureHeader : String) : NodeSeq = {
    stepsWithHeaderAllValueMatchTypesMessageCode(checker, name, value, matchTypes, message, code).filter(n => (n \ "@captureHeader").text == captureHeader)
  }




  def Start : (NodeSeq) => NodeSeq = stepsWithType(_, "START")
  def Accept : (NodeSeq) => NodeSeq = stepsWithType(_, "ACCEPT")
  def URLFail : (NodeSeq) => NodeSeq = stepsWithType(_, "URL_FAIL")
  def URLFail(notMatch : String)  : (NodeSeq) => NodeSeq = stepsWithURLFailNotMatch(_, notMatch)
  def URLFailT(notTypes : String)  : (NodeSeq) => NodeSeq = stepsWithURLFailNotTypes(_, notTypes)
  def MethodFail : (NodeSeq) => NodeSeq = stepsWithType(_, "METHOD_FAIL")
  def MethodFail(notMatch : String) : (NodeSeq) => NodeSeq = stepsWithMethodFailNotMatch(_, notMatch)
  def ReqTypeFail : (NodeSeq) => NodeSeq = stepsWithType(_, "REQ_TYPE_FAIL")
  def WellXML : (NodeSeq) => NodeSeq = stepsWithType(_, "WELL_XML")
  def WellJSON : (NodeSeq) => NodeSeq = stepsWithType(_, "WELL_JSON")
  def XSD : (NodeSeq) => NodeSeq = stepsWithType(_, "XSD")
  def XSL : (NodeSeq) => NodeSeq = stepsWithType(_, "XSL")
  def JSONSchema : (NodeSeq) => NodeSeq = stepsWithType(_, "JSON_SCHEMA")
  def ContentFail : (NodeSeq) => NodeSeq = stepsWithType(_, "CONTENT_FAIL")
  def URL(url : String) : (NodeSeq) => NodeSeq = stepsWithURLMatch(_, url)
  def URLWithCapture(url : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithURLMatchCapture(_, url, captureHeader)
  def URLXSD(url : String) : (NodeSeq) => NodeSeq = stepsWithURLXSDMatch(_, url)
  def URLXSDWithCapture(url : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithURLXSDMatchCapture(_, url, captureHeader)
  def Label(label : String) : (NodeSeq) => NodeSeq = stepsWithLabel(_, label)
  def Method(method : String) : (NodeSeq) => NodeSeq = stepsWithMethodMatch(_, method)
  def XPath(expression : String) : (NodeSeq) => NodeSeq = stepsWithXPathMatch (_, expression)
  def XPathWithCapture(expression : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithXPathMatchCapture (_, expression, captureHeader)
  def XPath(expression: String, message : String) : (NodeSeq) => NodeSeq = stepsWithXPathMessageMatch(_, expression, message)
  def XPath(expression: String, code : Int) : (NodeSeq) => NodeSeq = stepsWithXPathCodeMatch(_, expression, code)
  def XPath(expression: String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithXPathMessageCodeMatch(_, expression, message, code)
  def ReqType(reqType : String) : (NodeSeq) => NodeSeq = stepsWithReqTypeMatch (_, "(?i)"+reqType)
  def AnyReqType : (NodeSeq) => NodeSeq = stepsWithReqTypeMatch (_, "(.*)()")
  def HeaderWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderMatchCapture(_, name, headerMatch, captureHeader)
  def Header(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderMatch(_, name, headerMatch)
  def Header(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderMessageMatch(_, name, headerMatch, message)
  def Header(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderCodeMatch(_, name, headerMatch, code)
  def Header(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderMessageCodeMatch(_, name, headerMatch, message, code)
  def HeaderXSDWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMatchCapture(_, name, headerMatch, captureHeader)
  def HeaderXSD(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMatch (_, name, headerMatch)
  def HeaderXSD(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMessageMatch (_, name, headerMatch, message)
  def HeaderXSD(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDCodeMatch (_, name, headerMatch, code)
  def HeaderXSD(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMessageCodeMatch (_, name, headerMatch, message, code)
  def HeaderAnyWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMatchCapture(_, name, headerMatch, captureHeader)
  def HeaderAny(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMatch(_, name, headerMatch)
  def HeaderAny(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMessageMatch(_, name, headerMatch, message)
  def HeaderAny(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyCodeMatch(_, name, headerMatch, code)
  def HeaderAny(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMessageCodeMatch(_, name, headerMatch, message, code)
  def HeaderXSDAnyWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMatchCapture(_, name, headerMatch, captureHeader)
  def HeaderXSDAny(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMatch (_, name, headerMatch)
  def HeaderXSDAny(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMessageMatch (_, name, headerMatch, message)
  def HeaderXSDAny(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyCodeMatch (_, name, headerMatch, code)
  def HeaderXSDAny(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMessageCodeMatch (_, name, headerMatch, message, code)
  def HeaderSingleWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderSingleMatchCapture(_, name, headerMatch, captureHeader)
  def HeaderSingle(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderSingleMatch(_, name, headerMatch)
  def HeaderSingle(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderSingleMessageMatch(_, name, headerMatch, message)
  def HeaderSingle(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderSingleCodeMatch(_, name, headerMatch, code)
  def HeaderSingle(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderSingleMessageCodeMatch(_, name, headerMatch, message, code)
  def HeaderXSDSingleWithCapture(name : String, headerMatch : String, captureHeader : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDSingleMatchCapture(_, name, headerMatch, captureHeader)
  def HeaderXSDSingle(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDSingleMatch (_, name, headerMatch)
  def HeaderXSDSingle(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDSingleMessageMatch (_, name, headerMatch, message)
  def HeaderXSDSingle(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDSingleCodeMatch (_, name, headerMatch, code)
  def HeaderXSDSingle(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDSingleMessageCodeMatch (_, name, headerMatch, message, code)
  def HeaderAll(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValue(_, name, headerMatch)
  def HeaderAll(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValueMessage(_, name, headerMatch, message)
  def HeaderAll(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValueCode(_, name, headerMatch, code)
  def HeaderAll(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValueMessageCode(_, name, headerMatch, message, code)
  def HeaderAllWithTypes(name : String, matchTypes : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllMatchTypes(_, name, matchTypes)
  def HeaderAllWithTypes(name : String, matchTypes : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllMatchTypesMessage(_, name, matchTypes, message)
  def HeaderAllWithTypes(name : String, matchTypes : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAllMatchTypesCode(_, name, matchTypes, code)
  def HeaderAllWithTypes(name : String, matchTypes : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAllMatchTypesMessageCode(_, name, matchTypes, message, code)
  def HeaderAllWithMatchAndTypes(name : String, headerMatch : String, matchTypes : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValueMatchTypes(_, name, headerMatch, matchTypes)
  def HeaderAllWithMatchAndTypes(name : String, headerMatch : String, matchTypes : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAllValueMatchTypesMessage(_, name, headerMatch, matchTypes, message)
  def HeaderAllWithMatchAndTypes(name : String, headerMatch : String, matchTypes : String, code : Int) : (NodeSeq) => NodeSeq =
    stepsWithHeaderAllValueMatchTypesCode(_, name, headerMatch, matchTypes, code)
  def HeaderAllWithMatchAndTypes(name : String, headerMatch : String, matchTypes : String, message : String, code : Int) : (NodeSeq) => NodeSeq =
    stepsWithHeaderAllValueMatchTypesMessageCode(_, name, headerMatch, matchTypes, message, code)

  def SetHeader(name : String, value : String) : (NodeSeq) => NodeSeq = stepsWithSetHeader(_, name, value)
  def SetHeaderAlways(name : String) : (NodeSeq) => NodeSeq = stepsWithSetHeaderAlways(_, name)
  def SetHeaderAlways(name : String, value : String) : (NodeSeq) => NodeSeq = stepsWithSetHeaderAlways(_, name, value)

  def assert (checker : NodeSeq, step_funs : ((NodeSeq) => NodeSeq)*) : Unit = {

    if (step_funs.isEmpty) throw new TestFailedException("Path assertion should contain at least one step!",4)

    def followPath(step : NodeSeq, nextSteps : Seq[NodeSeq]) : Boolean = {
      if (nextSteps.isEmpty) return true
      val next = nextSteps(0)
      step.foreach(n =>
        (n \ "@next").text.split(" ").foreach(m => next.foreach (o =>
          if ((o \ "@id").text == m) {
            if (followPath (next, nextSteps.drop(1))) return true
          })))
      false
    }

    val steps = step_funs.map (n => n(checker))

    //
    //  Check to make sure all the steps are available...
    //
    steps.foreach (s => if (s.isEmpty) throw new TestFailedException("A step in the path could not be found", 4))

    //
    //  Try to follow the path
    //
    if (!followPath(steps(0), steps.drop(1))) {
      throw new TestFailedException("Could not follow path", 4)
    }
  }
}
