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
class StepHandler() extends ContentHandler {
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
  }

  override def endDocument = {
    next.foreach { case (id, nexts) => {
      val step = steps(id).asInstanceOf[ConnectedStep]
      for ( i <- 0 to nexts.length) {
        step.next(i) = steps(nexts(i))
      }
    }}
    next.clear
  }

  //
  //  Returns the start step after the document has been parsed.
  //
  def step : Step = start

  //
  //  The following add steps...
  //
  private[this] def addStart(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("nexts").split(" ")
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
    val nexts : Array[String] = atts.getValue("nexts").split(" ")
    val id : String = atts.getValue("id")
    val label : String = atts.getValue("label")
    val _match : String = atts.getValue("match")

    next  += (id -> nexts)
    steps += (id -> new URI(id, label, _match.r, new Array[Step](nexts.length)))
  }

  private[this] def addMethod(atts : Attributes) : Unit = {
    val nexts : Array[String] = atts.getValue("nexts").split(" ")
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
  override def characters(ch : Array[Char], start : Int, length : Int) = {}
  override def endElement(uri : String, localName : String, qname : String) = {}
  override def endPrefixMapping (prefix : String) = {}
  override def ignorableWhitespace(ch : Array[Char], start : Int, length : Int) = {}
  override def processingInstruction(target : String, data : String) = {}
  override def setDocumentLocator(locator : Locator) = {}
  override def skippedEntity (name : String) = {}
  override def startDocument = {}
  override def startPrefixMapping (prefix : String, uri : String) = {}
}
