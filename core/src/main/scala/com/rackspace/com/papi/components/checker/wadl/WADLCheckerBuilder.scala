/***
 *   Copyright 2017 Rackspace US, Inc.
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
import javax.xml.transform.dom._
import javax.xml.transform.stream._
import javax.xml.validation._

import org.xml.sax.XMLReader

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.util.EntityCatcher
import com.rackspace.cloud.api.wadl.util.LogErrorListener
import com.rackspace.cloud.api.wadl.util.XSLErrorDispatcher

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.macros.TimeFunction._

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.language.reflectiveCalls
import scala.xml._
import scala.collection.JavaConversions._

import net.sf.saxon.serialize.MessageWarner

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XsltExecutable
import net.sf.saxon.s9api.XsltTransformer
import net.sf.saxon.s9api.XPathExecutable
import net.sf.saxon.s9api.XdmDestination
import net.sf.saxon.s9api.XdmValue
import net.sf.saxon.s9api.XdmAtomicValue
import net.sf.saxon.s9api.XdmItem
import net.sf.saxon.s9api.DOMDestination
import net.sf.saxon.s9api.SAXDestination
import net.sf.saxon.s9api.DocumentBuilder
import net.sf.saxon.s9api.BuildingContentHandler
import net.sf.saxon.lib.FeatureKeys
import net.sf.saxon.lib.AugmentedSource
import net.sf.saxon.lib.ParseOptions

import net.sf.saxon.om.NodeInfo

import net.sf.saxon.dom.NodeOverNodeInfo

import BuilderHelper._

object WADLCheckerBuilder {
  private val xpathCompiler = processor.newXPathCompiler

  xpathCompiler.declareNamespace ("svrl", "http://purl.oclc.org/dsdl/svrl")

  private lazy val normalizeWadlXsltExec : XsltExecutable = timeFunction ("compile /xsl/normalizeWadl.xsl",
                                                                        compiler.compile(new StreamSource(Class.forName("com.rackspace.cloud.api.wadl.WADLNormalizer").getResource("/xsl/normalizeWadl.xsl").toString)))


  private lazy val schematronXsltExec : XsltExecutable = timeFunction ("compile /xsl/wadl-links.xsl",
                                                                        compiler.compile(new StreamSource(Class.forName("com.rackspace.cloud.api.wadl.WADLNormalizer").getResource("/xsl/wadl-links.xsl").toString)))

  private lazy val svrlHandlerXsltExec : XsltExecutable = timeFunction ("compile /xsl/svrl-handler.xsl",
                                                                        compiler.compile(new StreamSource(Class.forName("com.rackspace.cloud.api.wadl.WADLNormalizer").getResource("/xsl/svrl-handler.xsl").toString)))


  private lazy val authenticatedByXsltExec: XsltExecutable = timeFunction ("compile /xsl/authenticated-by.xsl",
                                                                           compiler.compile(new StreamSource(getClass.getResource("/xsl/authenticated-by.xsl").toString)))

  private lazy val raxMetaTransformXsltExec: XsltExecutable = timeFunction ("compile /xsl/meta-transform.xsl",
                                                                            compiler.compile(new StreamSource(getClass.getResource("/xsl/meta-transform.xsl").toString)))

  private lazy val raxAssertXsltExec : XsltExecutable = timeFunction ("compile /xsl/raxAssert.xsl",
                                                                      compiler.compile(new StreamSource(getClass.getResource("/xsl/raxAssert.xsl").toString)))


  private lazy val raxRolesXsltExec : XsltExecutable = timeFunction ("compile /xsl/raxRoles.xsl",
                                                                     compiler.compile(new StreamSource(getClass.getResource("/xsl/raxRoles.xsl").toString)))

  private lazy val raxDeviceXsltExec : XsltExecutable = timeFunction ("compile /xsl/raxDevice.xsl",
                                                                      compiler.compile(new StreamSource(getClass.getResource("/xsl/raxDevice.xsl").toString)))

  private lazy val raxRolesMaskXsltExec : XsltExecutable = timeFunction ("compile /xsl/raxRolesMask.xsl",
                                                                         compiler.compile(new StreamSource(getClass.getResource("/xsl/raxRolesMask.xsl").toString)))

  private lazy val buildXsltExec : XsltExecutable = timeFunction ("compile /xsl/builder.xsl",
                                                                  compiler.compile(new StreamSource(getClass.getResource("/xsl/builder.xsl").toString)))

  private lazy val dupsXsltExec : XsltExecutable = timeFunction ("compile /xsl/opt/removeDups.xsl",
                                                                 compiler.compile(new StreamSource(getClass.getResource("/xsl/opt/removeDups.xsl").toString)))

  private lazy val joinXsltExec : XsltExecutable = timeFunction ("compile /xsl/opt/commonJoin.xsl",
                                                                 compiler.compile(new StreamSource(getClass.getResource("/xsl/opt/commonJoin.xsl").toString)))

  private lazy val joinHeaderXsltExec : XsltExecutable = timeFunction ("compile /xsl/opt/headerJoin.xsl",
                                                                       compiler.compile(new StreamSource(getClass.getResource("/xsl/opt/headerJoin.xsl").toString)))

  private lazy val joinXPathXsltExec : XsltExecutable = timeFunction ("compile /xsl/opt/xpathJoin.xsl",
                                                                      compiler.compile(new StreamSource(getClass.getResource("/xsl/opt/xpathJoin.xsl").toString)))

  private lazy val priorityXsltExec : XsltExecutable = timeFunction ("compile /xsl/priority.xsl",
                                                                     compiler.compile(new StreamSource(getClass.getResource("/xsl/priority.xsl").toString)))

  private lazy val adjustNextXsltExec : XsltExecutable = timeFunction ("compile /xsl/adjust-next-cont-error.xsl",
                                                                       compiler.compile(new StreamSource(getClass.getResource("/xsl/adjust-next-cont-error.xsl").toString)))

  private lazy val metaCheckXsltExec : XsltExecutable = timeFunction ("compile /xsl/meta-check.xsl",
                                                                      compiler.compile(new StreamSource(getClass.getResource("/xsl/meta-check.xsl").toString)))

  private lazy val checkerAssertsXsltExec : XsltExecutable = timeFunction ("compile /xsl/checker-asserts.xsl",
                                                                           compiler.compile(new StreamSource(getClass.getResource("/xsl/checker-asserts.xsl").toString)))

  private lazy val svrlCheckXPathExec : XPathExecutable = timeFunction ("compile XVRLCheck XPath",
                                                                        xpathCompiler.compile("/svrl:schematron-output/svrl:successful-report[@role='checkReference']/svrl:text"))

  /**
   *  XSL Transformer parameters.
   *
   *  {@see javax.xml.transform.Transformer}
   */
  object XSLParams {
    val CONFIG_METADATA = "configMetadata"
    val USER    = "user"
    val CREATOR = "creator"
    val SCHEMATRON_OUT = "schematronOutput"
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

  private lazy val checkerSchema = timeFunction("Xerces schema compile",{
    val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

    //
    //  Enable CTA full XPath2.0 checking in XSD 1.1
    //
    schemaFactory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)

    schemaFactory.newSchema(checkerSchemaSource)
  })

  private lazy val checkerSchemaManager = timeFunction ("SaxonEE schema compile", {
    processor.setConfigurationProperty(FeatureKeys.MULTIPLE_SCHEMA_IMPORTS, true)


    val sm = processor.getSchemaManager

    sm.setXsdVersion("1.1")
    sm.setErrorListener(new LogErrorListener)

    checkerSchemaSource.foreach(sm.load)
    sm
  })

  private lazy val wadlSchemaSource = new StreamSource(Class.forName("com.rackspace.cloud.api.wadl.WADLNormalizer").getResource("/xsd/wadl.xsd").toString)

  private lazy val wadlSchema = timeFunction("Xerces wadl schema compile", {
    val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
    schemaFactory.newSchema(wadlSchemaSource)
  })

  private lazy val wadlSchemaManager = timeFunction("SaxonEE wadl schema compile", {
    val proc = new Processor(true)
    proc.setConfigurationProperty(FeatureKeys.MULTIPLE_SCHEMA_IMPORTS, true)


    val sm = proc.getSchemaManager

    sm.setXsdVersion("1.0")
    sm.setErrorListener(new LogErrorListener)

    sm.load(wadlSchemaSource)
    sm
  })
}

import WADLCheckerBuilder._

class WADLCheckerBuilder(protected[wadl] var wadl : WADLNormalizer) extends LazyLogging with XSLErrorDispatcher {

  import XSLParams._

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)


  //
  //  Our version of transform which gets the resolver from the
  //  WADLNormalizer.
  //
  private def getXsltTransformer (xsltExec : XsltExecutable, params : Map[QName, XdmValue]=Map[QName, XdmValue]()) : XsltTransformer = {
    BuilderHelper.getXsltTransformer (xsltExec, wadl.saxTransformerFactory.getURIResolver, params)
  }

  //------------------------
  //  Transform Functions
  //------------------------

  //
  //  Initial parse WADL
  //
  private def parseWADL (in : Source, c : Config) : Source = timeFunction ("parseWADL",{
    if (!in.isInstanceOf[AugmentedSource])
      throw new SAXParseException("That's strange expected an augmented source with an entity catcher! Please report this error.",
                                  null)
    val augSource    = in.asInstanceOf[AugmentedSource]
    val parseOptions = augSource.getParseOptions
    val docBuilder = new net.sf.saxon.dom.DocumentBuilderImpl
    docBuilder.setParseOptions (parseOptions)

    val inputSource = augSource.getContainedSource match {
      case saxSource    : SAXSource => saxSource.getInputSource
      case streamSource : StreamSource => {
        val is = new InputSource(streamSource.getInputStream)
        is.setSystemId(streamSource.getSystemId)
        is
      }
      case ni : NodeInfo => return ni.asInstanceOf[Source] // entities already resolved!
      case domSource : DOMSource => return domSource  // entities already resolved!
      case s : Source => new InputSource(s.getSystemId)
    }

    //
    //  Although this call is depricated it, we use it to avoid
    //  parsing the wadl twice. The issue: standard document builder
    //  (which builds tinytree) doesn't take an entity resolver which
    //  we need to find entities.
    //
    //  This call takes a resolver, but creates a standard DOM node
    //  which wraps a tinytree. We unwrap the tiny tree NodeInfo and
    //  return it.
    //
    //  The alternative is to parse twice -- once to find the entities
    //  and then again to get the DOM in the right format.
    //
    val doc = docBuilder.parse(inputSource)
    doc.asInstanceOf[NodeOverNodeInfo].getUnderlyingNodeInfo
  })

  //
  //  Schematron checks on the WADL
  //
  private def schematronWADL (in : Source, c : Config) : Source = timeFunction ("schematronWADL",{
    if (!in.isInstanceOf[AugmentedSource])
      throw new SAXParseException("That's strange expected an augmented source with an entity catcher! Please report this error.",
                                  null)
    val augSource    = in.asInstanceOf[AugmentedSource]
    val parseOptions = augSource.getParseOptions
    val schematronTrans = getXsltTransformer(schematronXsltExec)
    val out = new XdmDestination
    schematronTrans.setURIResolver(new Object() with URIResolver {
      val origSCHURIResolver = schematronTrans.getURIResolver
      def resolve(href : String, base : String) = {
        BuilderHelper.Source(origSCHURIResolver.resolve(href, base), Option(parseOptions.getEntityResolver))
      }
    })
    schematronTrans.setSource(in)
    schematronTrans.setDestination(out)
    schematronTrans.transform
    out.getXdmNode.asSource
  })

  //
  //  Check schematron report for errors
  //
  private def svrlHandler (in : Source, c : Config) : Source = timeFunction ("svrlHandler",{
    if (!in.isInstanceOf[AugmentedSource])
      throw new SAXParseException("That's strange expected an augmented source with an entity catcher! Please report this error.",
                                  null)
    val augSource    = in.asInstanceOf[AugmentedSource]
    val parseOptions = augSource.getParseOptions
    val entityCatcher = parseOptions.getEntityResolver.asInstanceOf[EntityCatcher]
    val svrlHandlerTrans = getXsltTransformer(svrlHandlerXsltExec,
                                              Map(new QName("systemIds")-> new XdmValue(entityCatcher.systemIds.map(new XdmAtomicValue(_)))))
    val out = new XdmDestination
    svrlHandlerTrans.setSource(in)
    svrlHandlerTrans.setDestination(out)
    svrlHandlerTrans.transform
    out.getXdmNode.asSource
  })


  //
  //  Normalize the WADL
  //
  private def normalizeWADL (in : Source, c : Config) : Source = timeFunction ("normalizeWADL2", {
    if (!in.isInstanceOf[AugmentedSource])
      throw new SAXParseException("That's strange expected an augmented source with an entity catcher! Please report this error.",
                                  null)
    val augSource    = in.asInstanceOf[AugmentedSource]
    val parseOptions = augSource.getParseOptions
    val normTransformer = getXsltTransformer(normalizeWadlXsltExec,
                                             Map(new QName("format") -> new XdmAtomicValue("tree-format"),
                                                 new QName("xsdVersion") -> new XdmAtomicValue("1.1"),
                                                 new QName("resource_types") -> new XdmAtomicValue("keep"),
                                                 new QName("flattenXsds") -> new XdmAtomicValue("false")))

    normTransformer.setURIResolver(new Object() with URIResolver {
      val origResolver = normTransformer.getURIResolver
      def resolve(href : String, base : String) = {
        BuilderHelper.Source(origResolver.resolve(href, base), Option(parseOptions.getEntityResolver))
      }
    })
    val out = new XdmDestination
    normTransformer.setSource(in)
    normTransformer.setDestination(out)
    normTransformer.transform
    out.getXdmNode.asSource
  })

  //
  //  Validates the wadl against schema
  //
  private def validateWADL (in : Source, c : Config) : Source = timeFunction("validateWADL", {
    val docBuilder = processor.newDocumentBuilder

    docBuilder.setBaseURI(new java.net.URI(in.getSystemId))

    val bch = docBuilder.newBuildingContentHandler

    if (c.xsdEngine == "SaxonEE") {
      logger.debug ("Using SaxonEE for WADL validation")
      val schemaValidator = wadlSchemaManager.newSchemaValidator
      schemaValidator.setDestination(new SAXDestination(bch))
      schemaValidator.validate(in)
    } else {
      logger.debug ("Using Xerces for WADL validation")
      val schemaHandler = wadlSchema.newValidatorHandler
      schemaHandler.setContentHandler(bch)
      idTransform.transform(in, new SAXResult(schemaHandler))
    }
    bch.getDocumentNode.asSource
  })

  //
  // AuthBy transform, handles authenticatedBy extension
  //
  private def authenticatedBy (in : Source, c : Config) : Source = timeFunction ("authenticatedBy", {
    if (c.enableAuthenticatedByExtension) {
      val authByTrans = getXsltTransformer(authenticatedByXsltExec)
      val out = new XdmDestination
      authByTrans.setSource(in)
      authByTrans.setDestination(out)
      authByTrans.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  // Handles the rax:assert extension
  //
  private def raxAssert(in : Source, c : Config) : Source = timeFunction("raxAssert", {
    if (c.enableAssertExtension) {
      val raxAssertTransform = getXsltTransformer(raxAssertXsltExec)
      val out = new XdmDestination
      raxAssertTransform.setSource(in)
      raxAssertTransform.setDestination(out)
      raxAssertTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Handles the metadata and raxRoles extensions
  //
  private def raxRoles(in : Source, c : Config) : Source = timeFunction("raxRoles", {
    if (c.enableRaxRolesExtension) {
      val raxMetaTransform = getXsltTransformer(raxMetaTransformXsltExec)
      val raxRolesTransform = getXsltTransformer(raxRolesXsltExec)
      val out = new XdmDestination

      raxMetaTransform.setSource(in)
      raxMetaTransform.setDestination(raxRolesTransform)
      raxRolesTransform.setDestination(out)
      raxMetaTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Handles the device extension and converts WADL to checker format
  //
  private def buildChecker(schematronOut : Source, in : Source, c : Config) : Source = timeFunction("buildChecker", {
    val builder = processor.newDocumentBuilder
    val deviceTransform  = getXsltTransformer(raxDeviceXsltExec)
    val buildTransformer = getXsltTransformer(buildXsltExec,
                                              Map(new QName(CONFIG_METADATA) -> builder.build(new StreamSource(c.checkerMetaElem)),
                                                  new QName(USER) -> new XdmAtomicValue(System.getProperty("user.name")),
                                                  new QName(CREATOR) -> new XdmAtomicValue(creatorString),
                                                  new QName(SCHEMATRON_OUT)-> builder.build(schematronOut)))
    val out = new XdmDestination
    deviceTransform.setSource(in)
    deviceTransform.setDestination(buildTransformer)
    buildTransformer.setDestination(out)
    deviceTransform.transform
    out.getXdmNode.asSource
  })

  //
  //  Handles the raxRolesMask extension
  //
  private def raxRolesMask(in : Source, c : Config) : Source = timeFunction("raxRolesMask", {
    if (c.maskRaxRoles403) {
      val raxRolesMaskTransformer = getXsltTransformer(raxRolesMaskXsltExec)
      val out = new XdmDestination
      raxRolesMaskTransformer.setSource(in)
      raxRolesMaskTransformer.setDestination(out)
      raxRolesMaskTransformer.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Perform the join optimization
  //
  private def joinOpt(in : Source, c : Config) : Source = timeFunction("joinOpt", {
    if (c.removeDups || c.joinXPathChecks) {
      val joinTransform = getXsltTransformer(joinXsltExec)
      val out = new XdmDestination
      joinTransform.setSource(in)
      joinTransform.setDestination(out)
      joinTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Perform Dups Optimization
  //
  private def dupsOpt(in : Source, c : Config) : Source = timeFunction("dupsOpt", {
    if (c.removeDups || c.joinXPathChecks) {
      val dupsTransform = getXsltTransformer(dupsXsltExec)
      val out = new XdmDestination
      dupsTransform.setSource(in)
      dupsTransform.setDestination(out)
      dupsTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Perform join header optimization
  //
  private def joinHeaderOpt(in : Source, c : Config) : Source = timeFunction("joinHeaderOpt", {
    if (c.removeDups || c.joinXPathChecks) {
      val joinHeaderTransform = getXsltTransformer(joinHeaderXsltExec)
      val out = new XdmDestination
      joinHeaderTransform.setSource(in)
      joinHeaderTransform.setDestination(out)
      joinHeaderTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Perform join XPath optimization
  //
  private def joinXPathOpt(in : Source, c : Config) : Source = timeFunction("joinXPathOpt", {
    if ((c.removeDups || c.joinXPathChecks) && c.joinXPathChecks) {
      val joinXPathTransform = getXsltTransformer(joinXPathXsltExec,
                                                  Map(new QName(CONFIG_METADATA) -> processor.newDocumentBuilder.build(new StreamSource(c.checkerMetaElem))))
      val out = new XdmDestination
      joinXPathTransform.setSource(in)
      joinXPathTransform.setDestination(out)
      joinXPathTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Performs transforms related to error priority
  //
  private def adjustNext(in : Source, c : Config) : Source = timeFunction("adjustNext", {
    val priorityTransform = getXsltTransformer(priorityXsltExec)
    val adjustTransform = getXsltTransformer(adjustNextXsltExec)
    val out = new XdmDestination
    priorityTransform.setSource(in)
    priorityTransform.setDestination(adjustTransform)
    adjustTransform.setDestination(out)
    priorityTransform.transform
    out.getXdmNode.asSource
  })

  //
  //  Validates the checker format against schema and other assertions
  //
  private def validateChecker (in : Source, c : Config) : Source = timeFunction("validateChecker", {
    if (c.validateChecker) {
      val assertTransform = getXsltTransformer(checkerAssertsXsltExec)
      val out = new XdmDestination
      assertTransform.setDestination(out)
      if (c.xsdEngine == "SaxonEE") {
        logger.debug ("Using SaxonEE for checker validation")
        val schemaValidator = checkerSchemaManager.newSchemaValidator
        schemaValidator.validate(in)
        assertTransform.setSource(in)
      } else {
        logger.debug ("Using Xerces for checker validation")
        val schemaHandler = checkerSchema.newValidatorHandler
        val buildingHandler = processor.newDocumentBuilder.newBuildingContentHandler
        schemaHandler.setContentHandler(buildingHandler)
        idTransform.transform(in, new SAXResult(schemaHandler))
        assertTransform.setSource(buildingHandler.getDocumentNode.asSource)
      }
      assertTransform.transform
      out.getXdmNode.asSource
    } else {
      in
    }
  })

  //
  //  Compares checker metadata with current settings and warns if they dont match
  //
  private def metaCheck (in : Source, c : Config) : Source = timeFunction("metaCheck", {
    val metaCheckTransform = getXsltTransformer(metaCheckXsltExec,
                                                Map(new QName(CONFIG_METADATA) -> processor.newDocumentBuilder.build(new StreamSource(c.checkerMetaElem)),
                                                    new QName(CREATOR) -> new XdmAtomicValue(creatorString)))
    val out = new XdmDestination
    metaCheckTransform.setSource(in)
    metaCheckTransform.setDestination(out)
    metaCheckTransform.transform
    out.getXdmNode.asSource
  })

  //
  //  Given an SVRL report, check refences that didn't look right in
  //  order to generate accurate error messages, we simply run each of
  //  these references through the XML parser to check them.
  //
  private def checkAdditionalSVRLReports (in : Source) : Unit = timeFunction("Check Additional SRVL Reports", {
    val xpathSelector = svrlCheckXPathExec.load
    xpathSelector.setContextItem(processor.newDocumentBuilder.build(in))
    xpathSelector.iterator.map (_.getStringValue.trim).toSet.foreach((inDoc : String) => {
      try {
        val reader = BuilderHelper.XMLReader(None)
        reader.parse(inDoc)
        logger.warn (s"This is strange document $inDoc was reported for further checking, but looks good. Ignoring.")
      } catch {
        case spe : SAXParseException => logger.error (spe.toString())
                                        throw new SAXParseException(spe.toString(), null, spe)
        case e : Exception => logger.error (inDoc+" : "+e.getMessage())
                              throw new SAXParseException(inDoc+" : "+e.getMessage(), null, e)
      }
    })
  })


  private def buildFromWADL (in : Source, out: Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config()
    }

    try {
      handleXSLException({
        //
        //  Used to Catch entities so we can treat them as dependencies
        //
        val entityCatcher = new EntityCatcher
        val parseOptions = new ParseOptions
        parseOptions.setEntityResolver (entityCatcher)
        parseOptions.setXIncludeAware(true)

        //
        //  Get the WADL as a tinytree source
        //
        val wadlSource = parseWADL (new AugmentedSource(in, parseOptions), c)

        //
        //  Get Schematron report, with entityCatcher, then check to
        //  make sure all dependecies are present. This validates all
        //  WADL dependecies.
        //
        val schematronReport = schematronWADL(new AugmentedSource(wadlSource, parseOptions), c)
        val entityDoc = svrlHandler(new AugmentedSource(schematronReport, parseOptions), c)

        checkAdditionalSVRLReports(entityDoc)

        val valWadl  = validateWADL(new AugmentedSource(wadlSource, parseOptions), c)
        val normWADL = normalizeWADL(new AugmentedSource(valWadl, parseOptions), c)

        //
        //  The build transformations listed in the order that they'll
        //  be applied.
        //
        val buildSteps : List[CheckerTransform] =
          List(raxAssert, authenticatedBy, raxRoles,
               buildChecker(entityDoc, _, _),
               raxRolesMask, joinOpt, dupsOpt,
               joinHeaderOpt, joinXPathOpt, adjustNext, validateChecker)

        //
        //  Apply the transformations and send results to out
        //
        val stepsSource = applyBuildSteps(buildSteps, normWADL, c)
        timeFunction("convert", idTransform.transform(stepsSource, out))
      })
    } catch {
      case e : Exception => logger.error(e.getMessage)
                            throw new WADLException ("WADL Processing Error: "+e.getMessage, e)
    }
  }

  private def buildFromChecker (in : Source, out : Result, config : Config) : Unit = {
    var c = config

    if (c == null) {
      c = new Config()
    }

    try {
      handleXSLException({
        //
        //  The build transformations listed in the order that they'll
        //  be applied.
        //
        val buildSteps : List[CheckerTransform] = List(metaCheck, validateChecker)

        val stepsSource = applyBuildSteps(buildSteps, in, c)
        timeFunction("convert", idTransform.transform (stepsSource, out))
      })
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
