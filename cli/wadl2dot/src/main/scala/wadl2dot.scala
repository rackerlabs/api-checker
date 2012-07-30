package com.rackspace.com.papi.components.checker.cli

import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder
import org.clapper.argot.ArgotConverters._
import org.clapper.argot.ArgotParser
import org.clapper.argot.ArgotUsageException

import javax.xml.transform._
import javax.xml.transform.stream._

import com.rackspace.com.papi.components.checker.Config

object Wadl2Dot {
  val parser = new ArgotParser("wadl2dot", preUsage=Some("wadl2dot: Version 1.0.0-SNAPSHOT"))

  val removeDups = parser.flag[Boolean] (List("d", "remove-dups"),
                                         "Remove duplicate nodes. Default: false")

  val wellFormed = parser.flag[Boolean] (List("w", "well-formed"),
                                         "Add checks to ensure that XML and JSON are well formed. Default: false")

  val joinXPaths = parser.flag[Boolean] (List("j", "join-xpaths"),
                                         "Join multiple XPath and XML well-formed checks into a single check: false")

  val xsdCheck = parser.flag[Boolean] (List("x", "xsd"),
                                         "Add checks to ensure that XML validates against XSD grammar Default: false")

  val element = parser.flag[Boolean] (List("l", "element"),
                                         "Add checks to ensure that XML requests use the correct element : false")

  val plainParam = parser.flag[Boolean] (List("p", "plain"),
                                         "Add checks for plain parameters : false")

  val preProc  = parser.flag[Boolean] (List("P", "preproc"),
                                       "Enable preprocess extension : false")

  val showErrors = parser.flag[Boolean] (List("e", "show-errors"),
                                          "Show error nodes. Default: false")

  val nfaMode = parser.flag[Boolean] (List("n", "nfa-mode"),
                                          "Display in NFA mode. Default: false")

  val help = parser.flag[Boolean] (List("h", "help"),
                                   "Display usage.")

  val input = parser.parameter[String]("wadl",
                                       "WADL file/uri to read.  If not specified, "+
                                       " stdin will be used.", true)

  val output = parser.parameter[String]("output",
                                        "Output file. If not specified, stdout will be used.",
                                        true)

  def getSource : Source = {
    var source : Source = null
    if (input.value == None) {
      source = new StreamSource(System.in)
    } else {
      source = new StreamSource(input.value.get)
    }
    source
  }

  def getResult : Result = {
    var result : Result = null
    if (output.value == None) {
      result = new StreamResult(System.out)
    } else {
      result = new StreamResult(output.value.get)
    }
    result
  }

  def handleArgs(args: Array[String]) : Unit = {
    parser.parse(args)

    if (help.value.getOrElse(false)) {
      parser.usage
    }
  }

  def main (args: Array[String]) = {
    try {
      handleArgs (args)

      val c = new Config

      c.removeDups = removeDups.value.getOrElse(false)
      c.checkWellFormed = wellFormed.value.getOrElse(false)
      c.checkXSDGrammar = xsdCheck.value.getOrElse(false)
      c.checkElements   = element.value.getOrElse(false)
      c.checkPlainParams = plainParam.value.getOrElse(false)
      c.enablePreProcessExtension = preProc.value.getOrElse(false)
      c.joinXPathChecks = joinXPaths.value.getOrElse(false)
      c.validateChecker = true

      new WADLDotBuilder().build (getSource, getResult,
                                  c,
                                  !showErrors.value.getOrElse(false),
                                  nfaMode.value.getOrElse(false))
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}
