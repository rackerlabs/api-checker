package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

class BaseCheckerSpec extends BaseWADLSpec {
  val builder = new WADLCheckerBuilder(wadl)

  private val pathNodesTemplates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/path-nodes.xsl")))

  def allStepsFromStep (checker : NodeSeq, id : String) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    val transformer = pathNodesTemplates.newTransformer
    transformer.setParameter("sid",id)
    transformer.transform (checker, new StreamResult(bytesOut))
    XML.loadString(bytesOut.toString())
  }

  def stepsWithType (checker : NodeSeq, nodeType : String) : NodeSeq = {
    (checker \\ "step").filter(n => (n \ "@type").text == nodeType)
  }
}
