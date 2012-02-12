package com.rackspace.com.papi.components.checker.cli

import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder
import org.clapper.argot.ArgotConverters._
import org.clapper.argot.ArgotParser
import org.clapper.argot.ArgotUsageException

import javax.xml.transform._
import javax.xml.transform.stream._

object Wadl2Checker {
  val parser = new ArgotParser("wadl2checker", preUsage=Some("wadl2checker: Vesion 1.0.0-SNAPSHOT"))

  val removeDups = parser.flag[Boolean] (List("d", "remove-dups"),
                                         "Remove duplicate nodes. Default: false")

  val validate = parser.flag[Boolean] (List("v", "validate"),
                                       "Validate produced checker Default: false")

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

      new WADLCheckerBuilder().build (getSource, getResult,
                                      removeDups.value.getOrElse(false),
                                      validate.value.getOrElse(false))
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}
