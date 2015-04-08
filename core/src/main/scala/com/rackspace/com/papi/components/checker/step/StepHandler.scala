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
package com.rackspace.com.papi.components.checker.step

import java.net.URL
import javax.xml.namespace.{NamespaceContext, QName}
import javax.xml.transform.{Source, Templates, TransformerFactory}
import javax.xml.transform.dom.{DOMResult, DOMSource}
import javax.xml.transform.sax.{SAXSource, SAXTransformerFactory, TransformerHandler}
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import javax.xml.xpath.XPathExpression

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.report.{ListReportProvider, LogLevel}
import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step}
import com.rackspace.com.papi.components.checker.step.startend._
import com.rackspace.com.papi.components.checker.util.{ImmutableNamespaceContext, ObjectMapperPool, XPathExpressionPool}
import com.saxonica.config.EnterpriseTransformerFactory
import net.sf.saxon.TransformerFactoryImpl
import org.w3c.dom.Document
import org.xml.sax.{Attributes, ContentHandler, InputSource, Locator, SAXParseException}

import scala.collection.mutable.{ArrayBuffer, HashMap, Map}


/**
 * The StepHandler assumes it is receiving content that is valid
 *  according to the checker schema.  Please ensure that a validation
 * stage occurs before the handler is called.
 * <p>
 * The StepHandler is also <b>not</b> thread safe.
 */
class StepHandler(var contentHandler : ContentHandler, val config : Config) extends ContentHandler {
  //
  // ID -> Step
  //
  private[this] val steps : Map[String, Step] = new HashMap[String, Step]
  //
  // ID -> Next Step IDs
  //
  private[this] val next  : Map[String, Array[String]] = new HashMap[String, Array[String]]
  //
  // The start step
  //
  private[this] var start : Step = null
  //
  // The prefix mappings
  //
  private[this] val prefixes : Map[String, String] = new HashMap[String, String]
  //
  // In the first phase we process grammars, when this is false we're
  // processing steps.
  //
  private[this] var processGrammar : Boolean = true
  //
  // A list of XML source
  //
  private[this] val grammarSources : ArrayBuffer[Source] = new ArrayBuffer[Source]
  //
  // JSON Schema factory
  //
  private[this] val jsonSchemaFactory = JsonSchemaFactory.newBuilder.setReportProvider(new ListReportProvider(LogLevel.WARNING, LogLevel.ERROR)).freeze
  //
  // JSON Schema grammar
  //
  private[this] var jsonGrammar : JsonNode = null
  //
  // JSON Schema buffer
  //
  private[this] var jsonBuffer : StringBuilder = new StringBuilder()
  //
  // Should we be processing the JSON buffer
  //
  private[this] var processJSONBuffer : Boolean = false

  //
  // Our schema factory...
  //
  private[this] val schemaFactory = {
    var sf : SchemaFactory = null

    config.xsdEngine match {

      //
      //  Enable CTA full XPath2.0 checking in XSD 1.1
      //
      case "Xerces"  => {
        sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory", this.getClass.getClassLoader)
        sf.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)
      }

      //
      //  Enable Schema 1.1 support
      //
      case "SaxonEE" => {
        sf = new com.saxonica.jaxp.SchemaFactoryImpl()
        sf.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")
      }
    }

    sf
  }

  //
  //  XSL 2.0 schema factory
  //
  private[this] val transformFactoryXSL2 : TransformerFactory = {
    /**
     * Packages up a saxon factory, but also specifies the classloader for the DynamicLoader within saxon
     * @return
     */
    def saxonFactory() = {
      val factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)
      val cast = factory.asInstanceOf[TransformerFactoryImpl]
      cast.getConfiguration.getDynamicLoader.setClassLoader(this.getClass.getClassLoader)
      factory
    }
    config.xslEngine match  {


      case "SaxonEE" => {
        val factory = TransformerFactory.newInstance("com.saxonica.config.EnterpriseTransformerFactory", this.getClass.getClassLoader)
       /*
       * I found this through here: http://sourceforge.net/p/saxon/mailman/message/29737564/
       * A bit of deduction and stuff let me to assume that all dynamic loading is done with the DynamicLoader
       * object. The only way to get ahold of that is to typecast the TransformerFactory to the actual class, and
       * then get the DynamicLoader out of it, and set it's classloader to the one where the saxonica classes
       * are located.
       */
        //Now that we have a Saxon EE transformer factory, we need to configure it...
        //We have to do casting to get the configuration object, to configure the DynamicLoader for our classloader
        //This is only needed for saxon EE, because it generates bytecode.
        val cast = factory.asInstanceOf[EnterpriseTransformerFactory]
        cast.getConfiguration.getDynamicLoader.setClassLoader(this.getClass.getClassLoader)
        factory
      }
      case "SaxonHE" => saxonFactory()
      // TODO:  if the wadl every explicitly calls out for  XSLT2 , we need to give them a SAXON transformer,
      // Xalan doesn't support 2
      case _ =>  saxonFactory()
    }
  }



  //
  //  XSL schema factory
  //
  private[this] val transformFactoryXSL1 : TransformerFactory = {
    config.xslEngine match  {

      case "Xalan" => TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", this.getClass.getClassLoader)
      case "XalanC" => TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", this.getClass.getClassLoader)
      case _ => transformFactoryXSL2
    }
  }

  //
  // Our schema...
  //
  private[this] var _schema : Schema = null
  private[this] def schema(qn : QName) : Schema = {
    if ((_schema == null) && (qn.getNamespaceURI() != "http://www.w3.org/2001/XMLSchema")) {
      throw new SAXParseException("No schema available.", locator)
    } else if (_schema == null) {
      schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/blank.xsd")))
    } else {
      _schema
    }
  }
  private[this] def schema (qn : Array[QName]) : Schema = {
    if ((_schema == null) && (qn.exists(q => q.getNamespaceURI() != "http://www.w3.org/2001/XMLSchema"))) {
      throw new SAXParseException("No schema available.", locator)
    } else if (_schema == null) {
      schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/blank.xsd")))
    } else {
      _schema
    }
  }
  private[this] def schema : Schema = {
    _schema
  }
  private[this] def schema_= (sch : Schema) : Unit = {
    _schema = sch
  }


  //
  //  The document locator...
  //
  private[this] var locator : Locator = null

  //
  //  Saxon transformer factory, schemahandler and result...this is
  //  used to capture inline schema and inline XSL.
  //
  private[this] val saxTransformerFactory : SAXTransformerFactory =
    TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader).asInstanceOf[SAXTransformerFactory]

  private[this] var currentSchemaHandler : TransformerHandler = null
  private[this] var currentSchemaResult  : DOMResult = null

  private[this] var currentXSLHandler : TransformerHandler = null
  private[this] var currentXSLResult  : DOMResult = null

  //
  //  The last XSL step processed, we may need to fill in the
  //  stylesheet.
  //
  private[this] var lastXSL : XSL = null
  private[this] var lastXSLVersion : String = null

  def this() = this( null, new Config() )

  override def startElement (uri : String, localName : String, qname : String, atts : Attributes) = {
    uri match {
      case "http://www.rackspace.com/repose/wadl/checker" => startCheckerElement(uri, localName, qname, atts)
      case "http://www.w3.org/2001/XMLSchema" => startSchemaElement(uri, localName, qname, atts)
      case "http://www.w3.org/1999/XSL/Transform" => startTransformElement(uri, localName, qname, atts)
      case _ => // ignore
    }
    if (contentHandler != null) {
      contentHandler.startElement(uri, localName, qname, atts)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.startElement(uri, localName, qname, atts)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.startElement(uri, localName, qname, atts)
    }
  }

  override def endElement(uri : String, localName : String, qname : String) = {
    uri match {
      case "http://www.rackspace.com/repose/wadl/checker" => endCheckerElement(uri, localName, qname)
      case "http://www.w3.org/2001/XMLSchema" => endSchemaElement(uri, localName, qname)
      case "http://www.w3.org/1999/XSL/Transform" => endTransformElement(uri, localName, qname)
      case _ => // ignore
    }

    if (contentHandler != null) {
      contentHandler.endElement(uri, localName, qname)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.endElement(uri, localName, qname)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.endElement(uri, localName, qname)
    }
  }

  override def endDocument = {
    next.foreach { case (id, nexts) => {
      val step = steps(id).asInstanceOf[ConnectedStep]
      for ( i <- 0 to (nexts.length - 1)) {
        step.next(i) = steps(nexts(i))
      }
    }}
    next.clear
    if (contentHandler != null) {
      contentHandler.endDocument()
    }
  }

  //
  //  Returns the start step after the document has been parsed.
  //
  def step : Step = start

  //
  //  Element handlers
  //
  private[this] def startCheckerElement (uri : String, localName : String, qname : String, atts : Attributes) = {
    localName match {
      case "step" =>
        if (processGrammar) {
          setupGrammar
        }
        atts.getValue("type") match {
          case "START"       => addStart(atts)
          case "ACCEPT"      => addAccept(atts)
          case "URL_FAIL"    => addURLFail(atts)
          case "METHOD_FAIL" => addMethodFail(atts)
          case "REQ_TYPE_FAIL" => addReqTypeFail(atts)
          case "CONTENT_FAIL" => addContentFail(atts)
          case "URL"         => addURL(atts)
          case "METHOD"      => addMethod(atts)
          case "URLXSD"      => addURLXSD(atts)
          case "REQ_TYPE"    => addReqType(atts)
          case "WELL_XML"    => addWellXML(atts)
          case "WELL_JSON"   => addWellJSON(atts)
          case "XSD"         => addXSD(atts)
          case "XPATH"       => addXPath(atts)
          case "XSL"         => addXSLT(atts)
          case "HEADER"      => addHeader(atts)
          case "HEADERXSD"   => addHeaderXSD(atts)
          case "HEADER_ANY"  => addHeaderAny(atts)
          case "HEADERXSD_ANY"  => addHeaderXSDAny(atts)
          case "JSON_SCHEMA" => addJSONSchema(atts)
        }
      case "grammar" =>
        addGrammar(atts)
      case _ =>  // ignore
    }
  }

  private[this] def endCheckerElement (uri : String, localName : String, qname : String) = {
    localName match {
      case "step" => if (lastXSL != null) closeXSLTStep
      case "grammar" => endGrammar
      case _ => //ignore
    }
  }

  private[this] def startSchemaElement (uri : String, localName : String, qname : String, atts : Attributes) = {
    localName match {
      case "schema" => startInlineSchema
      case _ => //ignore
    }
  }

  private[this] def endSchemaElement (uri : String, localName : String, qname : String) = {
    localName match {
      case "schema" => endInlineSchema
      case _ => //ignore
    }
  }

  private[this] def startTransformElement (uri : String, localName : String, qname : String, atts : Attributes) = {
    localName match {
      case "transform" => startInlineXSL
      case "stylesheet" => startInlineXSL
      case _ => //ignore
    }
  }

  private[this] def endTransformElement (uri : String, localName : String, qname : String) = {
    localName match {
      case "transform" => endInlineXSL
      case "stylesheet" => endInlineXSL
      case _ => //ignore
    }
  }

  //
  //  We add new grammar source, we use it for processing later.
  //
  private[this] def addGrammar(atts : Attributes) : Unit = {
    val href = atts.getValue("href")

    atts.getValue("type") match {
      case "W3C_XML" => {
        if (href != null) {
          grammarSources += new SAXSource(new InputSource(href))
        }
      }
      case "SCHEMA_JSON" => {
        if (href != null) {
          var om : ObjectMapper = null
          try {
            om = ObjectMapperPool.borrowParser
            jsonGrammar = om.readValue(new URL(href), classOf[JsonNode])
          } finally {
            if (om != null) ObjectMapperPool.returnParser(om)
          }
        } else {
          processJSONBuffer = true
        }
      }
    }
  }

  //
  //  Grammar section ends
  //
  private[this] def endGrammar : Unit = {
    if (processJSONBuffer) {
      var om : ObjectMapper = null
      try {
        om = ObjectMapperPool.borrowParser
        jsonGrammar = om.readValue(jsonBuffer.toString, classOf[JsonNode])
        jsonBuffer.setLength(0)
        processJSONBuffer = false
      } finally {
        if (om != null) ObjectMapperPool.returnParser(om)
      }
    }
  }

  //
  //  Handle internal schema....
  //
  private[this] def startInlineSchema : Unit = {
    if (currentSchemaHandler == null) {
      currentSchemaHandler = saxTransformerFactory.newTransformerHandler()
      currentSchemaResult  = new DOMResult()
      currentSchemaHandler.setResult (currentSchemaResult)

      currentSchemaHandler.startDocument()
      currentSchemaHandler.setDocumentLocator(locator)
      prefixes.foreach { case (prefix, uri) => {
        currentSchemaHandler.startPrefixMapping (prefix, uri)
      }}
    }
  }

  private[this] def endInlineSchema : Unit = {
    if (currentSchemaHandler != null) {
      currentSchemaHandler.endDocument()
      currentSchemaHandler = null

      grammarSources += new DOMSource(currentSchemaResult.getNode())

      currentSchemaResult = null
    }
  }

  //
  //  Handle internal XSL...
  //
  private[this] def startInlineXSL : Unit = {
    if (currentXSLHandler == null) {
      currentXSLHandler = saxTransformerFactory.newTransformerHandler()
      currentXSLResult = new DOMResult()
      currentXSLHandler.setResult (currentXSLResult)

      currentXSLHandler.startDocument()
      currentXSLHandler.setDocumentLocator(locator)
      prefixes.foreach { case (prefix, uri) => {
        currentXSLHandler.startPrefixMapping (prefix, uri)
      }}
    }
  }

  private[this] def endInlineXSL : Unit = {
    if (currentXSLHandler != null) {
      currentXSLHandler.endDocument()
      currentXSLHandler = null

      //
      //  The currentXSLResult is cleared by the step that consumes
      //  it.
      //
    }
  }

  //
  //  Process the grammar to generate a schema.
  //
  private[this] def setupGrammar : Unit = {
    if (grammarSources.length != 0) {
      schema = schemaFactory.newSchema(grammarSources.toArray)
    }
    processGrammar = false
  }


  private[this] def getPriority(atts : Attributes) : Long = atts.getValue("priority") match {
    case null => 1
    case s : String => s.toLong
  }

  //
  //  The following add steps...
  //
  private[this] def addStart(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")

    start = new Start(id, label, new Array[Step](nexts.length))
    next  +=  (id -> nexts)
    steps +=  (id -> start)
  }

  private[this] def addAccept(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val priority = getPriority (atts)

    steps += (id -> new Accept(id, label, priority))
  }

  private[this] def addURLFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val notMatch : String = atts.getValue("notMatch")
    val notTypes : Array[String] = {
      val nt = atts.getValue("notTypes")
      if (nt != null) {
        nt.split(" ")
      } else {
        null
      }
    }
    val priority = getPriority (atts)

    val notQNames : Array[QName] = {
      if (notTypes != null) {
        notTypes.map(x => qname(x))
      } else {
        null
      }
    }

    if (notMatch == null && notTypes == null) {
      steps += (id -> new URLFail(id, label, priority))
    } else if (notMatch != null && notTypes == null) {
      steps += (id -> new URLFailMatch(id, label, notMatch.r, priority))
    } else if (notMatch == null && notTypes != null) {
      steps += (id -> new URLFailXSD(id, label, notQNames, schema(notQNames), priority))
    } else {
      steps += (id -> new URLFailXSDMatch(id, label, notMatch.r, notQNames, schema(notQNames), priority))
    }
  }

  private[this] def addReqTypeFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val notMatch : String = atts.getValue("notMatch")
    val priority = getPriority (atts)

    steps += (id -> new ReqTypeFail(id, label, notMatch.r, priority))
  }

  private[this] def addContentFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val priority = getPriority (atts)

    steps += (id -> new ContentFail(id, label, priority))
  }

  private[this] def addMethodFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val notMatch : String = atts.getValue("notMatch")
    val priority = getPriority (atts)

    if (notMatch == null) {
      steps += (id -> new MethodFail(id, label, priority))
    } else {
      steps += (id -> new MethodFailMatch (id, label, notMatch.r, priority))
    }
  }

  private[this] def addReqType(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")

    next += (id -> nexts)
    steps += (id -> new ReqType(id, label, _match.r, new Array[Step](nexts.length)))
  }

  private[this] def addWellXML(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val priority = getPriority (atts)

    next += (id -> nexts)
    steps += (id -> new WellFormedXML (id, label, priority, new Array[Step](nexts.length)))
  }

  private[this] def addWellJSON(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val priority = getPriority (atts)

    next += (id -> nexts)
    steps += (id -> new WellFormedJSON (id, label, priority, new Array[Step](nexts.length)))
  }

  private[this] def addXSD(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val transform : Boolean = {
      val stransform = atts.getValue("transform")

      if (stransform == null) {
        config.doXSDGrammarTransform
      } else {
        stransform.toBoolean
      }
    }
    val priority = getPriority (atts)

    next += (id -> nexts)
    steps += (id -> new XSD(id, label, schema, transform, priority, new Array[Step](nexts.length)))
  }

  private[this] def addJSONSchema(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val priority = getPriority (atts)

    next += (id -> nexts)
    steps += (id -> new JSONSchema(id, label, jsonSchemaFactory.getJsonSchema(jsonGrammar), priority, new Array[Step](nexts.length)))
  }

  private[this] def addXSLT(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val href : String = atts.getValue("href")
    val version : String = atts.getValue("version")
    val priority = getPriority (atts)

    try {
      val templates : Templates = {
        if (href != null) {
          version match {
            case "1" => transformFactoryXSL1.newTemplates(new StreamSource(href))
            case "2" => transformFactoryXSL2.newTemplates(new StreamSource(href))
          }
        } else {
          null
        }
      }

      val xsl = new XSL(id, label, templates, priority, new Array[Step](nexts.length))

      next += (id -> nexts)
      steps += (id -> xsl)

      if (templates == null) {
        lastXSL = xsl
        lastXSLVersion = version
      }
    } catch {
      case e : Exception => throw new SAXParseException("Error while parsing XSLT", locator, e)
    }
  }

  private[this] def closeXSLTStep : Unit = {
    try {
      val templates : Templates = {
        val xslDoc = currentXSLResult.getNode().asInstanceOf[Document]
        lastXSLVersion match {
          case "1" => transformFactoryXSL1.newTemplates(new DOMSource(xslDoc))
          case "2" => transformFactoryXSL2.newTemplates(new DOMSource(xslDoc))
        }
      }

      steps += (lastXSL.id -> new XSL(lastXSL.id, lastXSL.label, templates, lastXSL.priority, lastXSL.next))

    } catch {
      case e : Exception => throw new SAXParseException("Error while parsing XSLT", locator, e)
    } finally {
      lastXSL = null
      lastXSLVersion = null
      currentXSLResult = null
    }
  }

  private[this] def getMessageCode(atts : Attributes) = {
    val message : Option[String] = {
      if (atts.getValue("message") == null) {
        None
      } else {
        Some(atts.getValue("message"))
      }
    }
    val code : Option[Int] = {
      if (atts.getValue("code") == null) {
        None
      } else {
        Some(atts.getValue("code").toInt)
      }
    }
    (message, code)
  }

  private[this] def getCaptureHeader(atts : Attributes) = atts.getValue("captureHeader") match {
    case s : String => Some(s)
    case null => None
  }

  private[this] def addXPath(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val captureHeader = getCaptureHeader(atts)
    val mc = getMessageCode(atts)
    val message = mc._1
    val code = mc._2
    val context : NamespaceContext = ImmutableNamespaceContext(prefixes)
    val version : Int = {
      val sversion = atts.getValue("version")

      if (sversion == null) {
        config.xpathVersion
      } else {
        sversion.toInt
      }
    }
    val priority = getPriority (atts)

    //
    //  Make an attempt to compile the XPath expression. Throw a
    //  SAXParseException if something goes wrong.
    //
    var expression : XPathExpression = null
    try {
      expression = XPathExpressionPool.borrowExpression(_match, context, version)
    } catch {
      case spe : SAXParseException => throw spe
      case e : Exception => throw new SAXParseException ("Error while compiling XPath expression", locator, e)
    } finally {
      if (expression != null) XPathExpressionPool.returnExpression(_match, context, version, expression)
    }

    next += (id -> nexts)
    steps += (id -> new XPath(id, label, _match, message, code, context, version, captureHeader, priority, new Array[Step](nexts.length)))
  }

  private[this] def addURL(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val captureHeader = getCaptureHeader(atts)

    next  += (id -> nexts)
    steps += (id -> new URI(id, label, _match.r, captureHeader, new Array[Step](nexts.length)))
  }

  private[this] def addHeader(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val name : String = atts.getValue("name")
    val mc = getMessageCode(atts)
    val message = mc._1
    val code = mc._2
    val priority = getPriority (atts)
    val captureHeader = getCaptureHeader(atts)

    next += (id -> nexts)
    steps += (id -> new Header(id, label, name, _match.r,
                               message, code, captureHeader, priority,
                               new Array[Step](nexts.length)))
  }

  private[this] def addHeaderAny(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val name : String = atts.getValue("name")
    val mc = getMessageCode(atts)
    val message = mc._1
    val code = mc._2
    val priority = getPriority (atts)
    val captureHeader = getCaptureHeader(atts)

    next += (id -> nexts)
    steps += (id -> new HeaderAny(id, label, name, _match.r,
                                  message, code, captureHeader,
                                  priority, new Array[Step](nexts.length)))
  }

  private[this] def addHeaderXSD(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val name : String = atts.getValue("name")
    val qn : QName = qname(_match)
    val mc = getMessageCode(atts)
    val message = mc._1
    val code = mc._2
    val priority = getPriority (atts)
    val captureHeader = getCaptureHeader(atts)

    next += (id -> nexts)
    steps += (id -> new HeaderXSD(id, label, name, qn, schema(qn),
                                  message, code, captureHeader,
                                  priority, new Array[Step](nexts.length)))
  }

  private[this] def addHeaderXSDAny(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val name : String = atts.getValue("name")
    val qn : QName = qname(_match)
    val mc = getMessageCode(atts)
    val message = mc._1
    val code = mc._2
    val priority = getPriority (atts)
    val captureHeader = getCaptureHeader(atts)

    next += (id -> nexts)
    steps += (id -> new HeaderXSDAny(id, label, name, qn, schema(qn),
                                     message, code, captureHeader,
                                     priority, new Array[Step](nexts.length)))
  }

  private[this] def addMethod(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")

    next  += (id -> nexts)
    steps += (id -> new Method(id, label, _match.r, new Array[Step](nexts.length)))
  }

  private[this] def addURLXSD(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val qn : QName = qname(_match)
    val captureHeader = getCaptureHeader(atts)

    next  += (id -> nexts)
    steps += (id -> new URIXSD(id, label, qn, schema(qn), captureHeader, new Array[Step](nexts.length)))
  }

  private[this] def qname(_match : String)  : QName = {
    if (_match.contains(":")) {
      val qname = _match.split(":")
      new QName(prefixes(qname(0)), qname(1), qname(0))
    } else {
      new QName(prefixes(""), _match)
    }
  }

  //
  //  Other content handler methods
  //
  override def characters(ch : Array[Char], start : Int, length : Int) = {
    if (contentHandler != null) {
      contentHandler.characters(ch, start, length)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.characters(ch, start, length)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.characters(ch, start, length)
    }
    if (processJSONBuffer) {
      jsonBuffer.appendAll (ch, start, length)
    }
  }
  override def startPrefixMapping (prefix : String, uri : String) = {
    prefixes += (prefix -> uri)

    if (contentHandler != null) {
      contentHandler.startPrefixMapping(prefix, uri)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.startPrefixMapping(prefix, uri)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.startPrefixMapping(prefix, uri)
    }
  }
  override def endPrefixMapping (prefix : String) = {
    prefixes -= prefix

    if (contentHandler != null) {
      contentHandler.endPrefixMapping(prefix)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.endPrefixMapping(prefix)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.endPrefixMapping(prefix)
    }
  }
  override def ignorableWhitespace(ch : Array[Char], start : Int, length : Int) = {
    if (contentHandler != null) {
      contentHandler.ignorableWhitespace(ch, start, length)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.ignorableWhitespace(ch, start, length)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.ignorableWhitespace(ch, start, length)
    }
  }
  override def processingInstruction(target : String, data : String) = {
    if (contentHandler != null) {
      contentHandler.processingInstruction(target, data)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.processingInstruction(target, data)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.processingInstruction(target, data)
    }
  }
  override def setDocumentLocator(locator : Locator) = {
    this.locator = locator

    if (contentHandler != null) {
      contentHandler.setDocumentLocator(locator)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.setDocumentLocator(locator)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.setDocumentLocator(locator)
    }
  }
  override def skippedEntity (name : String) = {
    if (contentHandler != null) {
      contentHandler.skippedEntity(name)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.skippedEntity(name)
    }
    if (currentXSLHandler != null) {
      currentXSLHandler.skippedEntity(name)
    }
  }
  override def startDocument = {
    if (contentHandler != null) {
      contentHandler.startDocument()
    }
  }
}
