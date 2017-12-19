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
package com.rackspace.com.papi.components.checker.cli

import javax.xml.transform._
import javax.xml.transform.stream._

import java.io.File
import java.io.PrintStream
import java.io.InputStream


import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.util.URLResolver
import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder

import org.clapper.argot.ArgotConverters._
import org.clapper.argot.{ArgotParser, ArgotUsageException}

import com.martiansoftware.nailgun.NGContext

object Wadl2Dot {
  val title = getClass.getPackage.getImplementationTitle
  val version = getClass.getPackage.getImplementationVersion

  def parseArgs(args: Array[String], base : String,
                in : InputStream, out : PrintStream, err : PrintStream) : Option[(Source, StreamResult, Config, Boolean, Boolean)] = {

    val parser = new ArgotParser("wadl2dot", preUsage=Some(s"$title v$version"))

    val input = parser.parameter[String]("wadl",
                                         "WADL file/uri to read.",
                                         false)

    val output = parser.parameter[String]("output",
                                          "Output file. If not specified, stdout will be used.",
                                          true)

    val help = parser.flag[Boolean] (List("h", "help"),
                                     "Display usage.")

    def source: Source = new StreamSource(URLResolver.toAbsoluteSystemId(input.value.get, base))

    def result: StreamResult = {
      var r: StreamResult = null
      if (output.value.isEmpty) {
        r = new StreamResult(out)
      } else {
        r = new StreamResult(URLResolver.toAbsoluteSystemId(output.value.get, base))
      }
      r
    }

    val removeDups = parser.flag[Boolean] (List("d", "remove-dups"),
                                           "Remove duplicate nodes. Default: false")

    val raxRoles = parser.flag[Boolean] (List("r", "rax-roles"),
                                         "Enable Rax-Roles extension. Default: false")

    val raxIsTenant = parser.flag[Boolean] (List("T", "rax-is-tenant"),
                                         "Enable Rax-Is-Tenant extension. Default: false")

    val raxRepresentation = parser.flag[Boolean] (List("R", "disable-rax-representation"),
                                         "Disable Rax-Representation extension. Default: false")

    val raxRolesMask403 = parser.flag[Boolean] (List("M", "rax-roles-mask-403s"),
                                                "When Rax-Roles is enable mask 403 errors with 404 or 405s. Default: false")

    val authenticatedBy = parser.flag[Boolean] (List("u", "authenticated-by"),
                                                "Enable Authenticated-By extension. Default: false")

    val wellFormed = parser.flag[Boolean] (List("w", "well-formed"),
                                           "Add checks to ensure that XML and JSON are well formed. Default: false")

    val joinXPaths = parser.flag[Boolean] (List("j", "join-xpaths"),
                                           "Join multiple XPath and XML well-formed checks into a single check. Default: false")

    val xsdGrammarTransform = parser.flag[Boolean] (List("g", "xsd-grammar-transform"),
                                                    "Transform the XML after validation, to fill in things like default values etc. Default: false")

    val preserveRequestBody = parser.flag[Boolean] (List("b", "preserve-req-body"),
                                                    "Ensure that the request body is preserved after validating the request.")

    val preserveMethodLabels = parser.flag[Boolean] (List("L", "preserve-method-labels"),
                                                    "Ensure that method labels are always preserved.")

    val xsdCheck = parser.flag[Boolean] (List("x", "xsd"),
                                         "Add checks to ensure that XML validates against XSD grammar Default: false")

    val jsonCheck = parser.flag[Boolean] (List("J", "json"),
                                          "Add checks to ensure that JSON validates against JSON Schema grammar Default: false")

    val element = parser.flag[Boolean] (List("l", "element"),
                                        "Add checks to ensure that XML requests use the correct element : false")

    val header = parser.flag[Boolean] (List("H", "header"),
                                       "Add checks to ensure that required headers are passed in: false")

    val setDefaults = parser.flag[Boolean] (List("s", "setParamDefaults"),
                                            "Fill in required parameters if a default value is specified Default: false")

    val plainParam = parser.flag[Boolean] (List("p", "plain"),
                                           "Add checks for plain parameters : false")

    val preProc  = parser.flag[Boolean] (List("P", "disable-preproc-ext"),
                                         "Disable preprocess extension : false")

    val ignoreXSD  = parser.flag[Boolean] (List("i", "disable-ignore-xsd-ext"),
                                           "Disable Ignore XSD  extension : false")

    val ignoreJSON  = parser.flag[Boolean] (List("I", "disable-ignore-json-ext"),
                                            "Disable Ignore JSON Schema  extension : false")

    val message  = parser.flag[Boolean] (List("m", "disable-message-ext"),
                                         "Disable Message extension : false")

    val captureHeader = parser.flag[Boolean] (List("c", "disable-capture-header-ext"),
                                              "Disable capture header extension : false")

    val anyMatch  = parser.flag[Boolean] (List("a", "disable-any-match"),
                                          "Disable any match extension : false")

    val raxAssert = parser.flag[Boolean] (List("k", "disable-rax-assert"),
                                          "Disable Rax-Assert extension : false")

    val warnHeaders = parser.flag[Boolean] (List("W", "disable-warn-headers"),
                                            "Disable warn headers : false")

    val warnAgent = parser.option[String] (List("A", "warn-agent"), "agent-name",
                                           "The name of the agent used in WARNING headers. Default: -")

    val xslEngine = parser.option[String] (List("E", "xsl-engine"), "xsl-engine",
                                           "The name of the XSLT engine to use. Possible names are Xalan, XalanC, SaxonHE, SaxonEE.  Default: XalanC")

    val xsdEngine = parser.option[String] (List("S", "xsd-engine"), "xsd-engine",
                                           "The name of the XSD engine to use. Possible names are Xerces, SaxonEE.  Default: Xerces")

    val dontValidate = parser.flag[Boolean] (List("D", "dont-validate"),
                                             "Don't validate produced checker Default: false")

    val showErrors = parser.flag[Boolean] (List("e", "show-errors"),
                                           "Show error nodes. Default: false")

    val nfaMode = parser.flag[Boolean] (List("n", "nfa-mode"),
                                        "Display in NFA mode. Default: false")

    val xpathVersion = parser.option[Int](List("t", "xpath-version"), "n",
                                          "XPath version to use. Can be 10, 20, 30, 31 for 1.0, 2.0, 3.0, and 3.1. Default: 10")

    val printVersion = parser.flag[Boolean] (List("version"),
                                             "Display version.")

    try {
      parser.parse(args)

      if (help.value.getOrElse(false)) {
        parser.usage() // throws ArgotUsageException
      }

      if (printVersion.value.getOrElse(false)) {
        err.println(s"$title v$version")
        None
      } else {
        val c = new Config

        c.removeDups = removeDups.value.getOrElse(false)
        c.enableRaxRolesExtension = raxRoles.value.getOrElse(false)
        c.enableRaxIsTenantExtension = raxIsTenant.value.getOrElse(false)
        c.enableRaxRepresentationExtension = !raxRepresentation.value.getOrElse(false)
        c.maskRaxRoles403 = raxRolesMask403.value.getOrElse(false)
        c.enableAuthenticatedByExtension = authenticatedBy.value.getOrElse(false)
        c.checkWellFormed = wellFormed.value.getOrElse(false)
        c.checkXSDGrammar = xsdCheck.value.getOrElse(false)
        c.checkJSONGrammar = jsonCheck.value.getOrElse(false)
        c.checkElements = element.value.getOrElse(false)
        c.checkPlainParams = plainParam.value.getOrElse(false)
        c.enablePreProcessExtension = !preProc.value.getOrElse(false)
        c.joinXPathChecks = joinXPaths.value.getOrElse(false)
        c.checkHeaders = header.value.getOrElse(false)
        c.setParamDefaults = setDefaults.value.getOrElse(false)
        c.enableIgnoreXSDExtension = !ignoreXSD.value.getOrElse(false)
        c.enableIgnoreJSONSchemaExtension = !ignoreJSON.value.getOrElse(false)
        c.enableMessageExtension = !message.value.getOrElse(false)
        c.enableCaptureHeaderExtension = !captureHeader.value.getOrElse(false)
        c.enableAnyMatchExtension = !anyMatch.value.getOrElse(false)
        c.enableAssertExtension = !raxAssert.value.getOrElse(false)
        c.xpathVersion = xpathVersion.value.getOrElse(10)
        c.preserveRequestBody = preserveRequestBody.value.getOrElse(false)
        c.preserveMethodLabels = preserveMethodLabels.value.getOrElse(false)
        c.doXSDGrammarTransform = xsdGrammarTransform.value.getOrElse(false)
        c.validateChecker = !dontValidate.value.getOrElse(false)
        c.enableWarnHeaders = !warnHeaders.value.getOrElse(false)
        c.warnAgent = warnAgent.value.getOrElse("-")
        c.xslEngine = xslEngine.value.getOrElse("XalanC")
        c.xsdEngine = xsdEngine.value.getOrElse("Xerces")

        Some((source, result, c, !showErrors.value.getOrElse(false), nfaMode.value.getOrElse(false)))
      }
    } catch {
      case e: ArgotUsageException => err.println(e.message)
                                     None
      case iae : IllegalArgumentException => err.println(iae.getMessage)
                                             None
    }
  }

  private def getBaseFromWorkingDir (workingDir : String) : String = {
    (new File(workingDir)).toURI().toString
  }

  //
  // Local run...
  //
  def main(args : Array[String]) = {
    parseArgs (args, getBaseFromWorkingDir(System.getProperty("user.dir")),
               System.in, System.out, System.err) match {
      case Some((source : Source, result : StreamResult, config : Config,
                 ignoreSinks : Boolean, nfaMode : Boolean)) =>
                   new WADLDotBuilder().build(source, result,
                                              config, ignoreSinks, nfaMode)
      case None => /* Bad args, Ignore */
    }
  }

  //
  //  Nailgun run...
  //
  def nailMain(context : NGContext) = {
    parseArgs (context.getArgs, getBaseFromWorkingDir(context.getWorkingDirectory),
               context.in, context.out, context.err) match {
      case Some((source : Source, result : StreamResult, config : Config,
                 ignoreSinks : Boolean, nfaMode : Boolean)) =>
                   new WADLDotBuilder().build(source, result,
                                              config, ignoreSinks, nfaMode)
      case None => /* Bad args, Ignore */
    }
  }
}
