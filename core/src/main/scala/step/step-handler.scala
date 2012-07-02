package com.rackspace.com.papi.components.checker.step

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.dom.DOMResult

import javax.xml.xpath.XPathExpression

import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

import javax.xml.namespace.NamespaceContext
import javax.xml.namespace.QName

import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.XPathExpressionPool

//
//  The StepHandler assumes it is receiving content that is valid
//  according to the checker schema.  Please ensure that a validation
//  stage occurs before the handler is called.
//
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
  // A list of source
  //
  private[this] val grammarSources : ArrayBuffer[Source] = new ArrayBuffer[Source]
  //
  // Our schema factory...
  //
  private[this] val schemaFactory = {
    var sf : SchemaFactory = null

    if (config.useSaxonEEValidation) {
      sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema/saxonica")

      //
      //  Enable Schema 1.1 support
      //
      sf.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")
    } else {
      sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

      //
      //  Enable CTA full XPath2.0 checking in XSD 1.1
      //
      sf.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true)
    }
    sf
  }

  //
  // Our schema...
  //
  private[this] var schema : Schema = null


  //
  //  The document locator...
  //
  private[this] var locator : Locator = null


  //
  //  Saxon transformer factory, schemahandler and result...this is
  //  used to capture inline schema.
  //
  private[this] val saxTransformerFactory : SAXTransformerFactory =
    TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).asInstanceOf[SAXTransformerFactory]
  private[this] var currentSchemaHandler : TransformerHandler = null
  private[this] var currentSchemaResult  : DOMResult = null

  def this() = this(null, new Config)

  override def startElement (uri : String, localName : String, qname : String, atts : Attributes) = {
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
        }
      case "grammar" =>
        addGrammar(atts)
      case "schema" =>
        startInlineSchema
      case _ =>  // ignore
    }
    if (contentHandler != null) {
      contentHandler.startElement(uri, localName, qname, atts)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.startElement(uri, localName, qname, atts)
    }
  }

  override def endElement(uri : String, localName : String, qname : String) = {
    if (contentHandler != null) {
      contentHandler.endElement(uri, localName, qname)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.endElement(uri, localName, qname)
    }
    if (localName == "schema") {
      endInlineSchema
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
  //  We add new grammar source, we use it for processing later.
  //
  private[this] def addGrammar(atts : Attributes) : Unit = {
    val href = atts.getValue("href")
    if (href != null) {
      grammarSources += new SAXSource(new InputSource(href))
    }
  }

  //
  //  We add internal schema....
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
  //  Process the grammar to generate a schema.
  //
  private[this] def setupGrammar : Unit = {
    if (grammarSources.length != 0) {
      schema = schemaFactory.newSchema(grammarSources.toArray)
    }
    processGrammar = false
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

    steps += (id -> new Accept(id, label))
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

    if (notMatch == null && notTypes == null) {
      steps += (id -> new URLFail(id, label))
    } else if (notMatch != null && notTypes == null) {
      steps += (id -> new URLFailMatch(id, label, notMatch.r))
    } else if (notMatch == null && notTypes != null) {
      steps += (id -> new URLFailXSD(id, label, notTypes.map (nt => qname(nt)), schema))
    } else {
      steps += (id -> new URLFailXSDMatch(id, label, notMatch.r, notTypes.map(nt => qname(nt)), schema))
    }
  }

  private[this] def addReqTypeFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val notMatch : String = atts.getValue("notMatch")

    steps += (id -> new ReqTypeFail(id, label, notMatch.r))
  }

  private[this] def addContentFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")

    steps += (id -> new ContentFail(id, label))
  }

  private[this] def addMethodFail(atts : Attributes) : Unit = {
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val notMatch : String = atts.getValue("notMatch")

    if (notMatch == null) {
      steps += (id -> new MethodFail(id, label))
    } else {
      steps += (id -> new MethodFailMatch (id, label, notMatch.r))
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

    next += (id -> nexts)
    steps += (id -> new WellFormedXML (id, label, new Array[Step](nexts.length)))
  }

  private[this] def addWellJSON(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")

    next += (id -> nexts)
    steps += (id -> new WellFormedJSON (id, label, new Array[Step](nexts.length)))
  }

  private[this] def addXSD(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")

    next += (id -> nexts)
    steps += (id -> new XSD(id, label, schema, new Array[Step](nexts.length)))
  }

  private[this] def addXPath(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")
    val context : NamespaceContext = ImmutableNamespaceContext(prefixes)
    val version : Int = {
      val sversion = atts.getValue("version")

      if (sversion == null) {
        config.xpathVersion
      } else {
        sversion.toInt
      }
    }

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
      if (expression != null) XPathExpressionPool.returnExpression(_match, version, expression)
    }

    next += (id -> nexts)
    steps += (id -> new XPath(id, label, _match, context, version, new Array[Step](nexts.length)))
  }

  private[this] def addURL(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("next").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")

    next  += (id -> nexts)
    steps += (id -> new URI(id, label, _match.r, new Array[Step](nexts.length)))
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

    if (schema == null) {
      throw new SAXParseException("No schema available to validate "+_match, locator)
    }

    next  += (id -> nexts)
    steps += (id -> new URIXSD(id, label, qname(_match), schema, new Array[Step](nexts.length)))
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
  }
  override def startPrefixMapping (prefix : String, uri : String) = {
    prefixes += (prefix -> uri)

    if (contentHandler != null) {
      contentHandler.startPrefixMapping(prefix, uri)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.startPrefixMapping(prefix, uri)
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
  }
  override def ignorableWhitespace(ch : Array[Char], start : Int, length : Int) = {
    if (contentHandler != null) {
      contentHandler.ignorableWhitespace(ch, start, length)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.ignorableWhitespace(ch, start, length)
    }
  }
  override def processingInstruction(target : String, data : String) = {
    if (contentHandler != null) {
      contentHandler.processingInstruction(target, data)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.processingInstruction(target, data)
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
  }
  override def skippedEntity (name : String) = {
    if (contentHandler != null) {
      contentHandler.skippedEntity(name)
    }
    if (currentSchemaHandler != null) {
      currentSchemaHandler.skippedEntity(name)
    }
  }
  override def startDocument = {
    if (contentHandler != null) {
      contentHandler.startDocument()
    }
  }
}
