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

class WADLDotBuilder(protected[wadl] var wadl : WADLNormalizer) {

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  val dotTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/checker2dot.xsl")))

  def build (in : Source, out: Result, removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val dotHandler = wadl.saxTransformerFactory.newTransformerHandler(dotTemplates)
    val transformer = dotHandler.getTransformer()
    dotHandler.setResult(out)
    transformer.setParameter ("ignoreSinks", ignoreSinks)
    transformer.setParameter ("nfaMode", nfaMode)
    checkerBuilder.build(in, new SAXResult(dotHandler), removeDups, true)
  }

  def build(in : (String, InputStream), out: Result, removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    build(new SAXSource(xmlReader, inputSource), out, removeDups, ignoreSinks, nfaMode)
  }

  def build(in : InputStream, out: Result, removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build (("",in), out, removeDups, ignoreSinks, nfaMode)
  }

  def build(in : Reader, out: Result, removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, removeDups, ignoreSinks, nfaMode)
  }

  def build(in : String, out: Result, removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, removeDups, ignoreSinks, nfaMode)
  }

  def build (in : (String, NodeSeq), removeDups : Boolean, ignoreSinks : Boolean, nfaMode : Boolean) : String = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut), removeDups, ignoreSinks, nfaMode)
    bytesOut.toString()
  }

  def build (in: NodeSeq, removeDups : Boolean = false,
             ignoreSinks: Boolean = true,
             nfaMode: Boolean = true) : String = {
    build (("",in), removeDups, ignoreSinks, nfaMode)
  }
}
