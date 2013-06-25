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

/**
 *  XSL Transformer parameters.
 *
 *  {@see javax.xml.transform.Transformer}
 */
object BuilderXSLParams {
  val ENABLE_WELL_FORM = "enableWellFormCheck"
  val ENABLE_XSD       = "enableXSDContentCheck"
  val ENABLE_XSD_TRANSFORM = "enableXSDTransform"
  val ENABLE_ELEMENT   = "enableElementCheck"
  val ENABLE_PLAIN_PARAM = "enablePlainParamCheck"
  val ENABLE_PRE_PROCESS_EXT = "enablePreProcessExtension"
  val ENABLE_XSD_IGNORE_EXT  = "enableIgnoreXSDExtension"
  val ENABLE_MESSAGE_EXT    = "enableMessageExtension"
  val ENABLE_HEADER         = "enableHeaderCheck"
}

/**
 *  XSL TransformerHandler parameters.
 *
 *  {@see javax.xml.transform.sax.TransformerHandler}
 */
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

  checkerSchemaSource(0) = new StreamSource(getClass().getResource("/xsd/transform.xsd").toString)
  checkerSchemaSource(1) = new StreamSource(getClass().getResource("/xsd/checker.xsd").toString)

  val checkerSchema = schemaFactory.newSchema(checkerSchemaSource)

  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/builder.xsl").toString))
  val dupsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/removeDups.xsl").toString))
  val joinTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/commonJoin.xsl").toString))
  val joinHeaderTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/headerJoin.xsl").toString))
  val joinXPathTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/xpathJoin.xsl").toString))

  def build (in : Source, out: Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config()
    }

    try {
      val buildHandler = wadl.saxTransformerFactory.newTransformerHandler(buildTemplates)

      buildHandler.getTransformer().setParameter (ENABLE_WELL_FORM, c.checkWellFormed)
      buildHandler.getTransformer().setParameter (ENABLE_XSD, c.checkXSDGrammar)
      buildHandler.getTransformer().setParameter (ENABLE_XSD_TRANSFORM, c.doXSDGrammarTransform)
      buildHandler.getTransformer().setParameter (ENABLE_ELEMENT, c.checkElements)
      buildHandler.getTransformer().setParameter (ENABLE_PLAIN_PARAM, c.checkPlainParams)
      buildHandler.getTransformer().setParameter (ENABLE_PRE_PROCESS_EXT, c.enablePreProcessExtension)
      buildHandler.getTransformer().setParameter (ENABLE_XSD_IGNORE_EXT, c.enableIgnoreXSDExtension)
      buildHandler.getTransformer().setParameter (ENABLE_MESSAGE_EXT, c.enableMessageExtension)
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
        val joinHeaderHandler = wadl.saxTransformerFactory.newTransformerHandler(joinHeaderTemplates)

        buildHandler.setResult (new SAXResult (dupsHandler))
        dupsHandler.setResult(new SAXResult(joinHandler))
        joinHandler.setResult(new SAXResult (joinHeaderHandler))

        if (c.joinXPathChecks) {
          val xpathHandler = wadl.saxTransformerFactory.newTransformerHandler(joinXPathTemplates)

          xpathHandler.getTransformer().setParameter(DEFAULT_XPATH_VERSION, c.xpathVersion)

          joinHeaderHandler.setResult(new SAXResult(xpathHandler))
          xpathHandler.setResult(output)
        } else {
          joinHeaderHandler.setResult(output)
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
    build(new StreamSource(in._2, in._1), out, config)
  }

  def build(in : InputStream, out: Result, config : Config) : Unit = {
    build (("test://app/mywadl.wadl",in), out, config)
  }

  def build(in : Reader, out: Result, config : Config) : Unit = {
    build(new StreamSource(in), out, config)
  }

  def build(in : String, out: Result, config : Config) : Unit = {
    build(new StreamSource(in), out, config)
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

