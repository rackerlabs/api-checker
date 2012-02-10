package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.Reader

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.validation._

import org.xml.sax.XMLReader
import org.xml.sax.InputSource

import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.Converters._

/**
 * An exception when transating the WADL into a checker.
 */
class WADLException(private val msg : String, private val cause : Throwable) extends Throwable(msg, cause) {}

class WADLCheckerBuilder(private var wadl : WADLNormalizer) {

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)

  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

  //
  //  Enable CTA full XPath2.0 checking in XSD 1.1
  //
  schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

  val checkerSchema = schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/checker.xsd")))

  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/builder.xsl")))
  val dupsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/removeDups.xsl")))

  def build (in : Source, out: Result, removeDups : Boolean, validateChecker : Boolean) : Unit = {
    try {
      val transformer = wadl.newTransformer(TREE, XSD11, false, KEEP)
      val buildHandler = wadl.saxTransformerFactory.newTransformerHandler(buildTemplates)

      var output = out;

      if (validateChecker) {
        val outHandler = wadl.saxTransformerFactory.newTransformerHandler();
        outHandler.setResult(output)

        val schemaHandler = checkerSchema.newValidatorHandler()
        schemaHandler.setContentHandler(outHandler)

        output = new SAXResult(schemaHandler)
      }

      if (removeDups) {
        val dupsHandler = wadl.saxTransformerFactory.newTransformerHandler(dupsTemplates)
        buildHandler.setResult (new SAXResult (dupsHandler))
        dupsHandler.setResult(output)
      } else {
        buildHandler.setResult (output)
      }
      transformer.transform (in, new SAXResult(buildHandler))
    } catch {
      case e => throw new WADLException ("WADL Processing Error", e)
    }
  }

  def build(in : (String, InputStream), out: Result, removeDups : Boolean, validateChecker : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    build(new SAXSource(xmlReader, inputSource), out, removeDups, validateChecker)
  }

  def build(in : InputStream, out: Result, removeDups : Boolean, validateChecker : Boolean) : Unit = {
    build (("",in), out, removeDups, validateChecker)
  }

  def build(in : Reader, out: Result, removeDups : Boolean, validateChecker : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, removeDups, validateChecker)
  }

  def build(in : String, out: Result, removeDups : Boolean, validateChecker : Boolean) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, removeDups, validateChecker)
  }

  def build (in : (String, NodeSeq), removeDups : Boolean, validateChecker : Boolean) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut), removeDups, validateChecker)
    XML.loadString (bytesOut.toString())
  }

  def build (in: NodeSeq, removeDups : Boolean = false, validateChecker : Boolean = true) : NodeSeq = {
    build (("",in), removeDups, validateChecker)
  }
}

