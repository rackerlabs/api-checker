package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import org.scalatest.TestFailedException

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

import com.rackspace.com.papi.components.checker.Config

class BaseCheckerSpec extends BaseWADLSpec {
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

  def stepsWithMatch (checker : NodeSeq, nodeMatch  : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@match").text == nodeMatch)
  }

  def stepsWithLabel (checker : NodeSeq, labelMatch  : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@label").text == labelMatch)
  }

  def stepsWithURLMatch (checker : NodeSeq, urlMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, urlMatch).filter(n => (n \ "@type").text == "URL")
  }

  def stepsWithURLXSDMatch (checker : NodeSeq, urlMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, urlMatch).filter(n => (n \ "@type").text == "URLXSD")
  }

  def stepsWithMethodMatch (checker : NodeSeq, methodMatch  : String) : NodeSeq = {
    stepsWithMatch (checker, methodMatch).filter(n => (n \ "@type").text == "METHOD")
  }

  def stepsWithReqTypeMatch (checker : NodeSeq, reqTypeMatch : String) : NodeSeq = {
    stepsWithMatch (checker, reqTypeMatch).filter(n => (n \ "@type").text == "REQ_TYPE")
  }

  def Start : (NodeSeq) => NodeSeq = stepsWithType(_, "START")
  def Accept : (NodeSeq) => NodeSeq = stepsWithType(_, "ACCEPT")
  def URLFail : (NodeSeq) => NodeSeq = stepsWithType(_, "URL_FAIL")
  def MethodFail : (NodeSeq) => NodeSeq = stepsWithType(_, "METHOD_FAIL")
  def ReqTypeFail : (NodeSeq) => NodeSeq = stepsWithType(_, "REQ_TYPE_FAIL")
  def WellXML : (NodeSeq) => NodeSeq = stepsWithType(_, "WELL_XML")
  def WellJSON : (NodeSeq) => NodeSeq = stepsWithType(_, "WELL_JSON")
  def ContentFail : (NodeSeq) => NodeSeq = stepsWithType(_, "CONTENT_FAIL")
  def URL(url : String) : (NodeSeq) => NodeSeq = stepsWithURLMatch(_, url)
  def URLXSD(url : String) : (NodeSeq) => NodeSeq = stepsWithURLXSDMatch(_, url)
  def Label(label : String) : (NodeSeq) => NodeSeq = stepsWithLabel(_, label)
  def Method(method : String) : (NodeSeq) => NodeSeq = stepsWithMethodMatch(_, method)
  def ReqType(reqType : String) : (NodeSeq) => NodeSeq = stepsWithReqTypeMatch (_, "(?i)"+reqType)

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
