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

import scala.xml._

import java.io.File
import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import org.scalatest.exceptions.TestFailedException

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

import com.rackspace.com.papi.components.checker.Config

class BaseCheckerSpec extends BaseWADLSpec {
  val localWADLURI = (new File(System.getProperty("user.dir"),"mywadl.wadl")).toURI.toString

  val builder = new WADLCheckerBuilder(wadl)

  val stdConfig = new Config
  val dupConfig = new Config

  stdConfig.removeDups = false
  dupConfig.removeDups = true

  private val pathNodesTemplates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/path-nodes.xsl")))

  def allStepsFromStep (checker : NodeSeq, id : String, max : Int = 0) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    val transformer = pathNodesTemplates.newTransformer
    transformer.setParameter("sid",id)
    if (max > 0) {
      transformer.setParameter("max",max)
    }
    transformer.transform (new StreamSource(checker), new StreamResult(bytesOut))
    XML.loadString(bytesOut.toString())
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

  def stepsWithURLXSDMatch (checker : NodeSeq, urlMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, urlMatch).filter(n => (n \ "@type").text == "URLXSD")
  }

  def stepsWithXPathMatch (checker : NodeSeq, xpathMatch : String) : NodeSeq = {
    stepsWithMatch (checker, xpathMatch).filter(n => (n \ "@type").text == "XPATH")
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

  def stepsWithHeaderXSDAnyCodeMatch (checker : NodeSeq, name : String, headerMatch : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDAnyMatch (checker, name, headerMatch).filter (n => (n \ "@code").text == code.toString)
  }

  def stepsWithHeaderXSDAnyMessageMatch (checker : NodeSeq, name : String, headerMatch : String, message : String) : NodeSeq = {
    stepsWithHeaderXSDAnyMatch (checker, name, headerMatch).filter (n => (n \ "@message").text == message)
  }

  def stepsWithHeaderXSDAnyMessageCodeMatch (checker : NodeSeq, name : String, headerMatch : String, message : String, code : Int) : NodeSeq = {
    stepsWithHeaderXSDAnyMessageMatch (checker, name, headerMatch, message).filter (n => (n \ "@code").text == code.toString)
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
  def URLXSD(url : String) : (NodeSeq) => NodeSeq = stepsWithURLXSDMatch(_, url)
  def Label(label : String) : (NodeSeq) => NodeSeq = stepsWithLabel(_, label)
  def Method(method : String) : (NodeSeq) => NodeSeq = stepsWithMethodMatch(_, method)
  def XPath(expression : String) : (NodeSeq) => NodeSeq = stepsWithXPathMatch (_, expression)
  def XPath(expression: String, message : String) : (NodeSeq) => NodeSeq = stepsWithXPathMessageMatch(_, expression, message)
  def XPath(expression: String, code : Int) : (NodeSeq) => NodeSeq = stepsWithXPathCodeMatch(_, expression, code)
  def XPath(expression: String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithXPathMessageCodeMatch(_, expression, message, code)
  def ReqType(reqType : String) : (NodeSeq) => NodeSeq = stepsWithReqTypeMatch (_, "(?i)"+reqType)
  def AnyReqType : (NodeSeq) => NodeSeq = stepsWithReqTypeMatch (_, "(.*)()")
  def Header(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderMatch(_, name, headerMatch)
  def Header(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderMessageMatch(_, name, headerMatch, message)
  def Header(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderCodeMatch(_, name, headerMatch, code)
  def Header(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderMessageCodeMatch(_, name, headerMatch, message, code)
  def HeaderXSD(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMatch (_, name, headerMatch)
  def HeaderXSD(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMessageMatch (_, name, headerMatch, message)
  def HeaderXSD(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDCodeMatch (_, name, headerMatch, code)
  def HeaderXSD(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDMessageCodeMatch (_, name, headerMatch, message, code)
  def HeaderAny(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMatch(_, name, headerMatch)
  def HeaderAny(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMessageMatch(_, name, headerMatch, message)
  def HeaderAny(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyCodeMatch(_, name, headerMatch, code)
  def HeaderAny(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderAnyMessageCodeMatch(_, name, headerMatch, message, code)
  def HeaderXSDAny(name : String, headerMatch : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMatch (_, name, headerMatch)
  def HeaderXSDAny(name : String, headerMatch : String, message : String) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMessageMatch (_, name, headerMatch, message)
  def HeaderXSDAny(name : String, headerMatch : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyCodeMatch (_, name, headerMatch, code)
  def HeaderXSDAny(name : String, headerMatch : String, message : String, code : Int) : (NodeSeq) => NodeSeq = stepsWithHeaderXSDAnyMessageCodeMatch (_, name, headerMatch, message, code)

  def assert (checker : NodeSeq, step_funs : ((NodeSeq) => NodeSeq)*) : Unit = {

    if (step_funs.length == 0) throw new TestFailedException("Path assertion should contain at least one step!",4)

    def followPath(step : NodeSeq, nextSteps : Seq[NodeSeq]) : Boolean = {
      if (nextSteps.length == 0) return true
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
    steps.foreach (s => if (s.length == 0) throw new TestFailedException("A step in the path could not be found", 4))

    //
    //  Try to follow the path
    //
    if (!followPath(steps(0), steps.drop(1))) {
      throw new TestFailedException("Could not follow path", 4)
    }
  }
}
