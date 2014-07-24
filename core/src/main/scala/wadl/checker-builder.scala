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

import scala.language.reflectiveCalls

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

import com.typesafe.scalalogging.slf4j.LazyLogging

import net.sf.saxon.Controller

/**
 *  XSL Transformer parameters.
 *
 *  {@see javax.xml.transform.Transformer}
 */
object BuilderXSLParams {
  val ENABLE_WELL_FORM = "enableWellFormCheck"
  val ENABLE_XSD       = "enableXSDContentCheck"
  val ENABLE_JSON_SCHEMA = "enableJSONContentCheck"
  val ENABLE_XSD_TRANSFORM = "enableXSDTransform"
  val ENABLE_ELEMENT   = "enableElementCheck"
  val ENABLE_PLAIN_PARAM = "enablePlainParamCheck"
  val ENABLE_PRE_PROCESS_EXT = "enablePreProcessExtension"
  val ENABLE_XSD_IGNORE_EXT  = "enableIgnoreXSDExtension"
  val ENABLE_JSON_IGNORE_EXT = "enableIgnoreJSONSchemaExtension"
  val ENABLE_RAX_ROLES_EXT = "enableRaxRoles"
  val ENABLE_MESSAGE_EXT    = "enableMessageExtension"
  val ENABLE_HEADER         = "enableHeaderCheck"
  val USER = "user"
  val CREATOR = "creator"
}

/**
 *  XSL TransformerHandler parameters.
 *
 *  {@see javax.xml.transform.sax.TransformerHandler}
 */
object XPathJoinParams {
  val DEFAULT_XPATH_VERSION = "defaultXPathVersion"
  val PRESERVE_REQUEST_BODY = "preserveRequestBody"
}

import BuilderXSLParams._
import XPathJoinParams._

/**
 * An exception when transating the WADL into a checker.
 */
class WADLException(private val msg : String, private val cause : Throwable) extends Throwable(msg, cause) {}

class WADLCheckerBuilder(protected[wadl] var wadl : WADLNormalizer) extends LazyLogging {

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)

  val creatorString = {
    val title = getClass.getPackage.getImplementationTitle
    val version = getClass.getPackage.getImplementationVersion
    s"$title ($version)"
  }

  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

  //
  //  Enable CTA full XPath2.0 checking in XSD 1.1
  //
  schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

  val checkerSchemaSource  = new Array[Source](2);

  checkerSchemaSource(0) = new StreamSource(getClass().getResource("/xsd/transform.xsd").toString)
  checkerSchemaSource(1) = new StreamSource(getClass().getResource("/xsd/checker.xsd").toString)

  val checkerSchema = schemaFactory.newSchema(checkerSchemaSource)


  val raxRolesTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/raxRoles.xsl").toString))
  val raxRolesMaskTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/raxRolesMask.xsl").toString))
  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/builder.xsl").toString))
  val dupsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/removeDups.xsl").toString))
  val joinTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/commonJoin.xsl").toString))
  val joinHeaderTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/headerJoin.xsl").toString))
  val joinXPathTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/xpathJoin.xsl").toString))
  val priorityTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/priority.xsl").toString))
  val adjustNextTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/adjust-next-cont-error.xsl").toString))

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
      buildHandler.getTransformer().setParameter (ENABLE_RAX_ROLES_EXT, c.enableRaxRolesExtension)
      buildHandler.getTransformer().setParameter (ENABLE_HEADER, c.checkHeaders)
      buildHandler.getTransformer().setParameter (ENABLE_JSON_SCHEMA, c.checkJSONGrammar)
      buildHandler.getTransformer().setParameter (ENABLE_JSON_IGNORE_EXT, c.enableIgnoreJSONSchemaExtension)
      buildHandler.getTransformer().setParameter (USER, System.getProperty("user.name"))
      buildHandler.getTransformer().setParameter (CREATOR, creatorString)
      buildHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

      val output = {
        val priorityHandler = wadl.saxTransformerFactory.newTransformerHandler(priorityTemplates)
        priorityHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

        val adjustNextHandler = wadl.saxTransformerFactory.newTransformerHandler(adjustNextTemplates)
        adjustNextHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

        priorityHandler.setResult(new SAXResult(adjustNextHandler))

        if (c.validateChecker) {
          val outHandler = wadl.saxTransformerFactory.newTransformerHandler();
          outHandler.setResult(out)

          val schemaHandler = checkerSchema.newValidatorHandler()
          schemaHandler.setContentHandler(outHandler)

          adjustNextHandler.setResult(new SAXResult(schemaHandler))
        } else {
          adjustNextHandler.setResult(out)
        }
        new SAXResult(priorityHandler)
      }

      val optInputHandler = {
        if (c.maskRaxRoles403) {
          val raxRolesMaskHandler = wadl.saxTransformerFactory.newTransformerHandler(raxRolesMaskTemplates)
          buildHandler.setResult(new SAXResult(raxRolesMaskHandler))
          raxRolesMaskHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
          raxRolesMaskHandler
        } else {
          buildHandler
        }
      }

      if (c.removeDups || c.joinXPathChecks) {
        val dupsHandler = wadl.saxTransformerFactory.newTransformerHandler(dupsTemplates)
        val joinHandler = wadl.saxTransformerFactory.newTransformerHandler(joinTemplates)
        val joinHeaderHandler = wadl.saxTransformerFactory.newTransformerHandler(joinHeaderTemplates)

        dupsHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
        joinHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
        joinHeaderHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

        optInputHandler.setResult (new SAXResult (dupsHandler))
        dupsHandler.setResult(new SAXResult(joinHandler))
        joinHandler.setResult(new SAXResult (joinHeaderHandler))

        if (c.joinXPathChecks) {
          val xpathHandler = wadl.saxTransformerFactory.newTransformerHandler(joinXPathTemplates)

          xpathHandler.getTransformer().setParameter(DEFAULT_XPATH_VERSION, c.xpathVersion)
          xpathHandler.getTransformer().setParameter(PRESERVE_REQUEST_BODY, c.preserveRequestBody)
          xpathHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

          joinHeaderHandler.setResult(new SAXResult(xpathHandler))
          xpathHandler.setResult(output)
        } else {
          joinHeaderHandler.setResult(output)
        }
      } else {
        optInputHandler.setResult (output)
      }
      if(c.enableRaxRolesExtension){
        val raxRolesHandler = wadl.saxTransformerFactory.newTransformerHandler(raxRolesTemplates)
        raxRolesHandler.setResult(new SAXResult(buildHandler))
        raxRolesHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
        wadl.normalize (in, new SAXResult(raxRolesHandler), TREE, XSD11, false, KEEP, true)
      }else{
        wadl.normalize (in, new SAXResult(buildHandler), TREE, XSD11, false, KEEP, true)
      }
    } catch {
      case e : Exception => logger.error(e.getMessage())
                            throw new WADLException ("WADL Processing Error: "+e.getMessage(), e)
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

