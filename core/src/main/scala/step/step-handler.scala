package com.rackspace.com.papi.components.checker.step

import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.Attributes

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

//
//  The StepHandler assumes it is receiving content that is valid
//  according to the checker schema.  Please ensure that a validation
//  stage occurs before the handler is called.
//
class StepHandler(var contentHandler : ContentHandler) extends ContentHandler {
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

  def this() = this(null)

  override def startElement (uri : String, localName : String, qname : String, atts : Attributes) = {
    if (localName == "step") {
      atts.getValue("type") match {
        case "START"       => addStart(atts)
        case "ACCEPT"      => addAccept(atts)
        case "URL_FAIL"    => addURLFail(atts)
        case "METHOD_FAIL" => addMethodFail(atts)
        case "URL"         => addURL(atts)
        case "METHOD"      => addMethod(atts)
        case "URLXSD"      => addURLXSD(atts)
      }
    }
    if (contentHandler != null) {
      contentHandler.startElement(uri, localName, qname, atts)
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

    if (notMatch == null) {
      steps += (id -> new URLFail(id, label))
    } else {
      steps += (id -> new URLFailMatch(id, label, notMatch.r))
    }
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
    System.err.println("StepHandler WARNING: URLXSD currently not supported")
  }

  //
  //  Other content handler methods
  //
  override def characters(ch : Array[Char], start : Int, length : Int) = {
    if (contentHandler != null) {
      contentHandler.characters(ch, start, length)
    }
  }
  override def endElement(uri : String, localName : String, qname : String) = {
    if (contentHandler != null) {
      contentHandler.endElement(uri, localName, qname)
    }
  }
  override def startPrefixMapping (prefix : String, uri : String) = {
    prefixes += (prefix -> uri)

    if (contentHandler != null) {
      contentHandler.startPrefixMapping(prefix, uri)
    }
  }
  override def endPrefixMapping (prefix : String) = {
    prefixes -= prefix

    if (contentHandler != null) {
      contentHandler.endPrefixMapping(prefix)
    }
  }
  override def ignorableWhitespace(ch : Array[Char], start : Int, length : Int) = {
    if (contentHandler != null) {
      contentHandler.ignorableWhitespace(ch, start, length)
    }
  }
  override def processingInstruction(target : String, data : String) = {
    if (contentHandler != null) {
      contentHandler.processingInstruction(target, data)
    }
  }
  override def setDocumentLocator(locator : Locator) = {
    if (contentHandler != null) {
      contentHandler.setDocumentLocator(locator)
    }
  }
  override def skippedEntity (name : String) = {
    if (contentHandler != null) {
      contentHandler.skippedEntity(name)
    }
  }
  override def startDocument = {
    if (contentHandler != null) {
      contentHandler.startDocument()
    }
  }
}
