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

import java.io.{ByteArrayOutputStream, InputStream, Reader}
import java.net.{URI, URISyntaxException}
import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.validation._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.util.LogErrorListener
import com.rackspace.com.papi.components.checker.Config
import com.typesafe.scalalogging.slf4j.LazyLogging
import net.sf.saxon.Controller

import scala.language.reflectiveCalls
import scala.xml._

class WADLCheckerBuilder(protected[wadl] var wadl : WADLNormalizer) extends LazyLogging {

  /**
   *  XSL Transformer parameters.
   *
   *  {@see javax.xml.transform.Transformer}
   */
  object XSLParams {
    val CONFIG_METADATA = "configMetadata"
    val USER    = "user"
    val CREATOR = "creator"
  }

  import XSLParams._

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


  val raxMetaTransformTemplates: Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/meta-transform.xsl").toString))
  val raxRolesTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/raxRoles.xsl").toString))
  val raxDeviceTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/raxDevice.xsl").toString))
  val raxRolesMaskTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/raxRolesMask.xsl").toString))
  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/builder.xsl").toString))
  val dupsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/removeDups.xsl").toString))
  val joinTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/commonJoin.xsl").toString))
  val joinHeaderTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/headerJoin.xsl").toString))
  val joinXPathTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/opt/xpathJoin.xsl").toString))
  val priorityTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/priority.xsl").toString))
  val adjustNextTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/adjust-next-cont-error.xsl").toString))
  val metaCheckTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/meta-check.xsl").toString))
  val checkerAssertsTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResource("/xsl/checker-asserts.xsl").toString))

  //
  //  We purposly do the identity transform using xalan instead of
  //  Saxon, because of SaxonEE license issue.
  //
  private val idTransform = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl",this.getClass.getClassLoader).newTransformer()
  idTransform.setErrorListener (new LogErrorListener)

  private def buildFromWADL (in : Source, out: Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config()
    }

    try {
      val buildHandler = wadl.saxTransformerFactory.newTransformerHandler(buildTemplates)

      buildHandler.getTransformer().setParameter (CONFIG_METADATA, new StreamSource(c.checkerMetaElem))
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

          val assertHandler = wadl.saxTransformerFactory.newTransformerHandler(checkerAssertsTemplates)
          assertHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
          assertHandler.setResult(new SAXResult (outHandler))

          val schemaHandler = checkerSchema.newValidatorHandler()
          schemaHandler.setContentHandler(assertHandler)

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

        optInputHandler.setResult (new SAXResult (joinHandler))
        joinHandler.setResult(new SAXResult(dupsHandler))
        dupsHandler.setResult(new SAXResult (joinHeaderHandler))

        if (c.joinXPathChecks) {
          val xpathHandler = wadl.saxTransformerFactory.newTransformerHandler(joinXPathTemplates)

          xpathHandler.getTransformer().setParameter(CONFIG_METADATA, new StreamSource(c.checkerMetaElem))
          xpathHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

          joinHeaderHandler.setResult(new SAXResult(xpathHandler))
          xpathHandler.setResult(output)
        } else {
          joinHeaderHandler.setResult(output)
        }
      } else {
        optInputHandler.setResult (output)
      }
      val deviceHandler = wadl.saxTransformerFactory.newTransformerHandler(raxDeviceTemplates)
      deviceHandler.setResult(new SAXResult(buildHandler))
      deviceHandler.getTransformer.asInstanceOf[Controller].addLogErrorListener
      if(c.enableRaxRolesExtension){
        val raxRolesHandler = wadl.saxTransformerFactory.newTransformerHandler(raxRolesTemplates)
        val raxMetaTransformHandler = wadl.saxTransformerFactory.newTransformerHandler(raxMetaTransformTemplates)

        raxRolesHandler.getTransformer.asInstanceOf[Controller].addLogErrorListener
        raxMetaTransformHandler.getTransformer.asInstanceOf[Controller].addLogErrorListener

        raxRolesHandler.setResult(new SAXResult(deviceHandler))
        raxMetaTransformHandler.setResult(new SAXResult(raxRolesHandler))

        wadl.normalize (in, new SAXResult(raxMetaTransformHandler), TREE, XSD11, false, KEEP, true)
      }else{
        wadl.normalize (in, new SAXResult(deviceHandler), TREE, XSD11, false, KEEP, true)
      }
    } catch {
      case e : Exception => logger.error(e.getMessage())
                            throw new WADLException ("WADL Processing Error: "+e.getMessage(), e)
    }
  }

  private def buildFromChecker (in : Source, out : Result, config : Config) : Unit = {
    val configSet = config == null
    val checkConfig = config match {
      case null => new Config
      case _ => config
    }

    val vout = {
      if (config.validateChecker) {
        val outHandler = wadl.saxTransformerFactory.newTransformerHandler()
        outHandler.setResult(out)

        val assertHandler = wadl.saxTransformerFactory.newTransformerHandler(checkerAssertsTemplates)
        assertHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener
        assertHandler.setResult(new SAXResult (outHandler))

        val schemaHandler = checkerSchema.newValidatorHandler()
        schemaHandler.setContentHandler(assertHandler)

        new SAXResult(schemaHandler)
      } else {
        out
      }
    }

    val metaHandler = wadl.saxTransformerFactory.newTransformerHandler(metaCheckTemplates)
    metaHandler.getTransformer().setParameter (CONFIG_METADATA, new StreamSource(checkConfig.checkerMetaElem))
    metaHandler.getTransformer().setParameter (CREATOR, creatorString)
    metaHandler.getTransformer().asInstanceOf[Controller].addLogErrorListener

    metaHandler.setResult(vout)

    try {
      idTransform.transform (in, new SAXResult(metaHandler))
    } catch {
      case e : Exception => logger.error(e.getMessage())
                            throw new WADLException ("WADL Processing Error: "+e.getMessage(), e)
    }
  }

  /*
   * We are starting with a very simple approach to detect if we the source contians
   * a checker format vs a wadl.
   *
   * 1. The Path compontent of the systemID ends with .checker
   * 2. The Query component contains the string checker=true
   *
   */
  private def useCheckerFormat (in : Source) : Boolean = {
    def nullGuard(s : String) : String = s match {
      case null => ""
      case _ => s
    }

    try {
      in.getSystemId() match {
        case null => false
        case s : String => val sysURI = new URI(s)
                           nullGuard(sysURI.getPath()).endsWith(".checker") ||
                           nullGuard(sysURI.getQuery()).contains("checker=true")
      }
    } catch {
      case u : URISyntaxException => logger.warn("Unable to parse systemId ("+in.getSystemId()+")URI continuing...")
                                     false
    }
  }

  def build (in : Source, out: Result, config : Config) : Unit = {
    if (useCheckerFormat(in)) {
      buildFromChecker (in, out, config)
    } else {
      buildFromWADL (in, out, config)
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

