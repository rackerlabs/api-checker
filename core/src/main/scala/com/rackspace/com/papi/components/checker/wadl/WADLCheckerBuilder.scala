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
import com.rackspace.cloud.api.wadl.util.XSLErrorDispatcher
import com.rackspace.com.papi.components.checker.Config
import com.typesafe.scalalogging.slf4j.LazyLogging
import net.sf.saxon.jaxp.TransformerImpl

import scala.language.reflectiveCalls
import scala.xml._


object WADLCheckerBuilder {
  private val _wadl = new WADLNormalizer // Static WADL normalizer used simply to build templates

  private val authenticatedByTemplates: Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/authenticated-by.xsl").toString))
  private val raxMetaTransformTemplates: Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/meta-transform.xsl").toString))
  private val raxRolesTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/raxRoles.xsl").toString))
  private val raxDeviceTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/raxDevice.xsl").toString))
  private val raxRolesMaskTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/raxRolesMask.xsl").toString))
  private val buildTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/builder.xsl").toString))
  private val dupsTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/opt/removeDups.xsl").toString))
  private val joinTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/opt/commonJoin.xsl").toString))
  private val joinHeaderTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/opt/headerJoin.xsl").toString))
  private val joinXPathTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/opt/xpathJoin.xsl").toString))
  private val priorityTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/priority.xsl").toString))
  private val adjustNextTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/adjust-next-cont-error.xsl").toString))
  private val metaCheckTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/meta-check.xsl").toString))
  private val checkerAssertsTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/checker-asserts.xsl").toString))

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


  val creatorString = {
    val title = getClass.getPackage.getImplementationTitle
    val version = getClass.getPackage.getImplementationVersion
    s"$title ($version)"
  }

  private lazy val checkerSchemaSource  = {
    val src = new Array[Source](2)

    src(0) = new StreamSource(getClass.getResource("/xsd/transform.xsd").toString)
    src(1) = new StreamSource(getClass.getResource("/xsd/checker.xsd").toString)
    src
  }

  private lazy val checkerSchema = {
    val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

    //
    //  Enable CTA full XPath2.0 checking in XSD 1.1
    //
    schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

    schemaFactory.newSchema(checkerSchemaSource)
  }

  private lazy val checkerSchemaSaxon = {
    val sf = new com.saxonica.ee.jaxp.SchemaFactoryImpl()
    sf.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")

    sf.newSchema(checkerSchemaSource)
  }
}

import WADLCheckerBuilder._

class WADLCheckerBuilder(protected[wadl] var wadl : WADLNormalizer) extends LazyLogging with XSLErrorDispatcher {

  import XSLParams._

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)

  //
  //  We purposly do the identity transform using xalan instead of
  //  Saxon, because of SaxonEE license issue.
  //
  private val idTransform = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl",this.getClass.getClassLoader).newTransformer()
  idTransform.setErrorListener (new LogErrorListener)

  //
  //  Given Templates and an optional set of XSLT parameters, creates a TransformerHandler
  //
  private def getTransformerHandler (templates : Templates, params : Map[String, Object]=Map[String,Object]()) : TransformerHandler = {
    val handler = wadl.saxTransformerFactory.newTransformerHandler(templates)
    val transformer = handler.getTransformer
    transformer.asInstanceOf[TransformerImpl].addLogErrorListener
    transformer.setURIResolver(wadl.saxTransformerFactory.getURIResolver)
    for ((param, value) <- params) {
      transformer.setParameter(param, value)
    }
    handler
  }

  private def buildFromWADL (in : Source, out: Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config()
    }

    try {
      handleXSLException({
        val buildHandler = getTransformerHandler(buildTemplates,
                                                 Map(CONFIG_METADATA -> new StreamSource(c.checkerMetaElem),
                                                     USER -> System.getProperty("user.name"),
                                                     CREATOR -> creatorString))

        val output = {
          val priorityHandler = getTransformerHandler(priorityTemplates)
          val adjustNextHandler = getTransformerHandler(adjustNextTemplates)

          priorityHandler.setResult(new SAXResult(adjustNextHandler))

          if (c.validateChecker) {
            val outHandler = wadl.saxTransformerFactory.newTransformerHandler()
            outHandler.setResult(out)

            val assertHandler = getTransformerHandler(checkerAssertsTemplates)
            assertHandler.setResult(new SAXResult (outHandler))

            val schemaHandler = config.xsdEngine match {
              case "SaxonEE" => logger.debug ("Using SaxonEE for checker validation")
                                checkerSchemaSaxon.newValidatorHandler()
              case xe : String => logger.debug (s"Using $xe for checker validation")
                                  checkerSchema.newValidatorHandler()
            }
            schemaHandler.setContentHandler(assertHandler)

            adjustNextHandler.setResult(new SAXResult(schemaHandler))
          } else {
            adjustNextHandler.setResult(out)
          }
          new SAXResult(priorityHandler)
        }

        val optInputHandler = {
          if (c.maskRaxRoles403) {
            val raxRolesMaskHandler = getTransformerHandler(raxRolesMaskTemplates)
            buildHandler.setResult(new SAXResult(raxRolesMaskHandler))
            raxRolesMaskHandler
          } else {
            buildHandler
          }
        }

        if (c.removeDups || c.joinXPathChecks) {
          val dupsHandler = getTransformerHandler(dupsTemplates)
          val joinHandler = getTransformerHandler(joinTemplates)
          val joinHeaderHandler = getTransformerHandler(joinHeaderTemplates)

          optInputHandler.setResult (new SAXResult (joinHandler))
          joinHandler.setResult(new SAXResult(dupsHandler))
          dupsHandler.setResult(new SAXResult (joinHeaderHandler))

          if (c.joinXPathChecks) {
            val xpathHandler = getTransformerHandler(joinXPathTemplates,
                                                     Map(CONFIG_METADATA -> new StreamSource(c.checkerMetaElem)))

            joinHeaderHandler.setResult(new SAXResult(xpathHandler))
            xpathHandler.setResult(output)
          } else {
            joinHeaderHandler.setResult(output)
          }
        } else {
          optInputHandler.setResult (output)
        }
        val deviceHandler = getTransformerHandler(raxDeviceTemplates)
        deviceHandler.setResult(new SAXResult(buildHandler))

        var firstHandler = deviceHandler

        if (c.enableRaxRolesExtension) {
          val raxRolesHandler = getTransformerHandler(raxRolesTemplates)
          val raxMetaTransformHandler = getTransformerHandler(raxMetaTransformTemplates)

          raxRolesHandler.setResult(new SAXResult(firstHandler))
          raxMetaTransformHandler.setResult(new SAXResult(raxRolesHandler))
          firstHandler = raxMetaTransformHandler
        }

        if (c.enableAuthenticatedByExtension) {
          val authByHandler = getTransformerHandler(authenticatedByTemplates)
          authByHandler.setResult(new SAXResult(firstHandler))
          firstHandler = authByHandler
        }

        wadl.normalize (in, new SAXResult(firstHandler), TREE, XSD11, false, KEEP, true)
      })
    } catch {
      case e : Exception => logger.error(e.getMessage)
                            throw new WADLException ("WADL Processing Error: "+e.getMessage, e)
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

        val assertHandler = getTransformerHandler(checkerAssertsTemplates)
        assertHandler.setResult(new SAXResult (outHandler))

        val schemaHandler = config.xsdEngine match {
          case "SaxonEE" => logger.debug ("Using SaxonEE for checker validation")
                            checkerSchemaSaxon.newValidatorHandler()

          case xe : String => logger.debug (s"Using $xe for checker validation")
                            checkerSchema.newValidatorHandler()


        }
        schemaHandler.setContentHandler(assertHandler)

        new SAXResult(schemaHandler)
      } else {
        out
      }
    }

    val metaHandler = getTransformerHandler(metaCheckTemplates,
                                            Map(CONFIG_METADATA -> new StreamSource(checkConfig.checkerMetaElem),
                                                CREATOR -> creatorString))
    metaHandler.setResult(vout)

    try {
      idTransform.transform (in, new SAXResult(metaHandler))
    } catch {
      case e : Exception => logger.error(e.getMessage)
                            throw new WADLException ("WADL Processing Error: "+e.getMessage, e)
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
      in.getSystemId match {
        case null => false
        case s : String => val sysURI = new URI(s)
                           nullGuard(sysURI.getPath).endsWith(".checker") ||
                           nullGuard(sysURI.getQuery).contains("checker=true")
      }
    } catch {
      case u : URISyntaxException => logger.warn("Unable to parse systemId ("+in.getSystemId+")URI continuing...")
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
    XML.loadString (bytesOut.toString)
  }

  def build (in: NodeSeq, config : Config = null) : NodeSeq = {
    build (("test://app/mywadl.wadl",in), config)
  }
}

