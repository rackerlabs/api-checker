package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.Reader

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import org.xml.sax.InputSource

import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.Converters._

import com.rackspace.com.papi.components.checker.Config

object Checker2DotXSLParams {
  val IGNORE_SINKS = "ignoreSinks"
  val NFA_MODE     = "nfaMode"
}

import Checker2DotXSLParams._

class WADLDotBuilder(protected[wadl] var wadl : WADLNormalizer) {

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  val dotTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/checker2dot.xsl")))

  def buildFromChecker (in : Source, out: Result, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val dotHandler = wadl.saxTransformerFactory.newTransformerHandler(dotTemplates)
    val transformer = dotHandler.getTransformer()
    transformer.setParameter (IGNORE_SINKS, ignoreSinks)
    transformer.setParameter (NFA_MODE, nfaMode)
    transformer.transform (in, out)
  }

  def build (in : Source, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val dotHandler = wadl.saxTransformerFactory.newTransformerHandler(dotTemplates)
    val transformer = dotHandler.getTransformer()
    dotHandler.setResult(out)
    transformer.setParameter (IGNORE_SINKS, ignoreSinks)
    transformer.setParameter (NFA_MODE, nfaMode)

    checkerBuilder.build(in, new SAXResult(dotHandler), config)
  }

  def build(in : (String, InputStream), out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    build(new SAXSource(xmlReader, inputSource), out, config, ignoreSinks, nfaMode)
  }

  def build(in : InputStream, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build (("test://mywadl.wadl",in), out, config, ignoreSinks, nfaMode)
  }

  def build(in : Reader, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, config, ignoreSinks, nfaMode)
  }

  def build(in : String, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, config, ignoreSinks, nfaMode)
  }

  def build (in : (String, NodeSeq), config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : String = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut), config, ignoreSinks, nfaMode)
    bytesOut.toString()
  }

  def build (in: NodeSeq, config : Config = null,
             ignoreSinks: Boolean = true,
             nfaMode: Boolean = true) : String = {
    build (("test://mywadl.wadl",in), config, ignoreSinks, nfaMode)
  }
}
