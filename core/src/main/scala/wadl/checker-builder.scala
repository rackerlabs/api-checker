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

import com.rackspace.com.papi.components.checker.Config

object BuilderXSLParams {
  val ENABLE_WELL_FORM = "enableWellFormCheck"
  val ENABLE_XSD       = "enableXSDContentCheck"
  val ENABLE_XSD_TRANSFORM = "enableXSDTransform"
  val ENABLE_ELEMENT   = "enableElementCheck"
  val ENABLE_PLAIN_PARAM = "enablePlainParamCheck"
  val ENABLE_PRE_PROCESS_EXT = "enablePreProcessExtension"
  val ENABLE_HEADER         = "enableHeaderCheck"
}

object XPathJoinParams {
  val DEFAULT_XPATH_VERSION = "defaultXPathVersion"
}

import BuilderXSLParams._
import XPathJoinParams._

/**
 * An exception when transating the WADL into a checker.
 */
class WADLException(private val msg : String, private val cause : Throwable) extends Throwable(msg, cause) {}

class WADLCheckerBuilder(protected[wadl] var wadl : WADLNormalizer) {

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)

  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

  //
  //  Enable CTA full XPath2.0 checking in XSD 1.1
  //
  schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

  val checkerSchemaSource  = new Array[Source](2);

  checkerSchemaSource(0) = new StreamSource(getClass().getResourceAsStream("/xsd/transform.xsd"))
  checkerSchemaSource(1) = new StreamSource(getClass().getResourceAsStream("/xsd/checker.xsd"))

  val checkerSchema = schemaFactory.newSchema(checkerSchemaSource)

  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/builder.xsl")))
  val dupsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/removeDups.xsl")))
  val joinTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/join.xsl")))
  val joinXPathTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/xpathJoin.xsl")))

  def build (in : Source, out: Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config
    }

    try {
      val buildHandler = wadl.saxTransformerFactory.newTransformerHandler(buildTemplates)

      buildHandler.getTransformer().setParameter (ENABLE_WELL_FORM, c.checkWellFormed)
      buildHandler.getTransformer().setParameter (ENABLE_XSD, c.checkXSDGrammar)
      buildHandler.getTransformer().setParameter (ENABLE_XSD_TRANSFORM, c.doXSDGrammarTransform)
      buildHandler.getTransformer().setParameter (ENABLE_ELEMENT, c.checkElements)
      buildHandler.getTransformer().setParameter (ENABLE_PLAIN_PARAM, c.checkPlainParams)
      buildHandler.getTransformer().setParameter (ENABLE_PRE_PROCESS_EXT, c.enablePreProcessExtension)
      buildHandler.getTransformer().setParameter (ENABLE_HEADER, c.checkHeaders)

      var output = out;

      if (c.validateChecker) {
        val outHandler = wadl.saxTransformerFactory.newTransformerHandler();
        outHandler.setResult(output)

        val schemaHandler = checkerSchema.newValidatorHandler()
        schemaHandler.setContentHandler(outHandler)

        output = new SAXResult(schemaHandler)
      }

      if (c.removeDups || c.joinXPathChecks) {
        val dupsHandler = wadl.saxTransformerFactory.newTransformerHandler(dupsTemplates)
        val joinHandler = wadl.saxTransformerFactory.newTransformerHandler(joinTemplates)

        buildHandler.setResult (new SAXResult (dupsHandler))
        dupsHandler.setResult(new SAXResult(joinHandler))

        if (c.joinXPathChecks) {
          val xpathHandler = wadl.saxTransformerFactory.newTransformerHandler(joinXPathTemplates)

          xpathHandler.getTransformer().setParameter(DEFAULT_XPATH_VERSION, c.xpathVersion)

          joinHandler.setResult(new SAXResult(xpathHandler))
          xpathHandler.setResult(output)
        } else {
          joinHandler.setResult(output)
        }
      } else {
        buildHandler.setResult (output)
      }
      wadl.normalize (in, new SAXResult(buildHandler), TREE, XSD11, false, KEEP)
    } catch {
      case e => throw new WADLException ("WADL Processing Error: "+e.getMessage(), e)
    }
  }

  def build(in : (String, InputStream), out: Result, config : Config) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    build(new SAXSource(xmlReader, inputSource), out, config)
  }

  def build(in : InputStream, out: Result, config : Config) : Unit = {
    build (("test://app/mywadl.wadl",in), out, config)
  }

  def build(in : Reader, out: Result, config : Config) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, config)
  }

  def build(in : String, out: Result, config : Config) : Unit = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), out, config)
  }

  def build (in : (String, NodeSeq), config : Config) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut), config)
    XML.loadString (bytesOut.toString())
  }

  def build (in: NodeSeq, config : Config = null) : NodeSeq = {
    build (("test://app/mywadl.wadl",in), config)
  }
}

