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
import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder
import com.martiansoftware.nailgun.NGContext
import scopt.OptionParser

object Wadl2Checker {
  val title = getClass.getPackage.getImplementationTitle
  val implVersion = getClass.getPackage.getImplementationVersion

  def parseArgs(args : Array[String], base : String,
                in : InputStream, out : PrintStream, err : PrintStream) : Option[(Source, Result, Option[StreamResult], Config)] = {

    val c = new Config

    // Fix default values for fields with defaults that differ between the API and CLI Config
    c.removeDups = false

    var input: String = ""
    var output: Option[String] = None
    var outputMetadata: Boolean = false

    val parser = new OptionParser[Unit]("wadl2checker") {
      head(s"$title v$implVersion")

      opt[Unit]('d', "remove-dups").text("Remove duplicate nodes. Default: false")
        .foreach(_ => c.removeDups = true)

      opt[Unit]('r', "rax-roles").text("Enable Rax-Roles extension. Default: false")
        .foreach(_ => c.enableRaxRolesExtension = true)

      opt[Unit]('T', "rax-is-tenant").text("Enable Rax-Is-Tenant extension. Default: false")
        .foreach(_ => c.enableRaxIsTenantExtension = true)

      opt[Unit]('R', "disable-rax-representation").text("Disable Rax-Representation extension. Default: false")
        .foreach(_ => c.enableRaxRepresentationExtension = false)

      opt[Unit]('M', "rax-roles-mask-403s").text("When Rax-Roles is enable mask 403 errors with 404 or 405s. Default: false")
        .foreach(_ => c.maskRaxRoles403 = true)

      opt[Unit]('u', "authenticated-by").text("Enable Authenticated-By extension. Default: false")
        .foreach(_ => c.enableAuthenticatedByExtension = true)

      opt[Unit]('w', "well-formed").text("Add checks to ensure that XML and JSON are well formed. Default: false")
        .foreach(_ => c.checkWellFormed = true)

      opt[Unit]('O', "output-metadata").text("Display checker metadata")
        .foreach(_ => outputMetadata = true)

      opt[Unit]('j', "join-xpaths").text("Join multiple XPath and XML well-formed checks into a single check. Default: false")
        .foreach(_ => c.joinXPathChecks = true)

      opt[Unit]('g', "xsd-grammar-transform").text("Transform the XML after validation, to fill in things like default values etc. Default: false")
        .foreach(_ => c.doXSDGrammarTransform = true)

      opt[Unit]('b', "preserve-req-body").text("Ensure that the request body is preserved after validating the request.")
        .foreach(_ => c.preserveRequestBody = true)

      opt[Unit]('L', "preserve-method-labels").text("Ensure that method labels are always preserved.")
        .foreach(_ => c.preserveMethodLabels = true)

      opt[Unit]('x', "xsd").text("Add checks to ensure that XML validates against XSD grammar Default: false")
        .foreach(_ => c.checkXSDGrammar = true)

      opt[Unit]('J', "json").text("Add checks to ensure that JSON validates against JSON Schema grammar Default: false")
        .foreach(_ => c.checkJSONGrammar = true)

      opt[Unit]('l', "element").text("Add checks to ensure that XML requests use the correct element : false")
        .foreach(_ => c.checkElements = true)

      opt[Unit]('H', "header").text("Add checks to ensure that required headers are passed in: false")
        .foreach(_ => c.checkHeaders = true)

      opt[Unit]('s', "setParamDefaults").text("Fill in required parameters if a default value is specified Default: false")
        .foreach(_ => c.setParamDefaults = true)

      opt[Unit]('p', "plain").text("Add checks for plain parameters : false")
        .foreach(_ => c.checkPlainParams = true)

      opt[Unit]('P', "disable-preproc-ext").text("Disable preprocess extension : false")
        .foreach(_ => c.enablePreProcessExtension = false)

      opt[Unit]('i', "disable-ignore-xsd-ext").text("Disable Ignore XSD  extension : false")
        .foreach(_ => c.enableIgnoreXSDExtension = false)

      opt[Unit]('I', "disable-ignore-json-ext").text("Disable Ignore JSON Schema  extension : false")
        .foreach(_ => c.enableIgnoreJSONSchemaExtension = false)

      opt[Unit]('m', "disable-message-ext").text("Disable Message extension : false")
        .foreach(_ => c.enableMessageExtension = false)

      opt[Unit]('c', "disable-capture-header-ext").text("Disable capture header extension : false")
        .foreach(_ => c.enableCaptureHeaderExtension = false)

      opt[Unit]('a', "disable-any-match").text("Disable any match extension : false")
        .foreach(_ => c.enableAnyMatchExtension = false)

      opt[Unit]('k', "disable-rax-assert").text("Disable Rax-Assert extension : false")
        .foreach(_ => c.enableAssertExtension = false)

      opt[Unit]('W', "disable-warn-headers").text("Disable warn headers : false")
        .foreach(_ => c.enableWarnHeaders = false)

      opt[String]('A', "warn-agent").text("The name of the agent used in WARNING headers. Default: -")
        .valueName("agent-name")
        .withFallback(() => "-")
        .foreach(x => c.warnAgent = x)

      opt[String]('E', "xsl-engine").text("The name of the XSLT engine to use. Possible names are Xalan, XalanC, SaxonHE, SaxonEE.  Default: XalanC")
        .valueName("xsl-engine")
        .withFallback(() => "XalanC")
        .foreach(x => c.xslEngine = x)

      opt[String]('S', "xsd-engine").text("The name of the XSD engine to use. Possible names are Xerces, SaxonEE.  Default: Xerces")
        .valueName("xsd-engine")
        .withFallback(() => "Xerces")
        .foreach(x => c.xsdEngine = x)

      opt[Unit]('D', "dont-validate").text("Don't validate produced checker Default: false")
        .foreach(_ => c.validateChecker = false)

      opt[Int]('t', "xpath-version").text("XPath version to use. Can be 10, 20, 30, 31 for 1.0, 2.0, 3.0, and 3.1. Default: 10")
        .valueName("n")
        .withFallback(() => 10)
        .foreach(x => c.xpathVersion = x)

      help('h', "help").text("Display usage.")

      version("version").text("Display version.")

      arg[String]("wadl").text("WADL file/uri to read.")
        .foreach(x => input = x)

      arg[String]("output").text("Output file. If not specified, stdout will be used.")
        .optional()
        .foreach(x => output = Some(x))
    }

    def source: Source = new StreamSource(URLResolver.toAbsoluteSystemId(input, base))

    def result: Result = {
      var r: Result = null
      if (output.isEmpty) {
        r = new StreamResult(out)
      } else {
        r = new StreamResult(URLResolver.toAbsoluteSystemId(output.get, base))
      }
      r
    }

    if (parser.parse(args)) {
      val metaOutResult = {
        if (outputMetadata) {
          Some(new StreamResult(err))
        } else {
          None
        }
      }

      Some((source, result, metaOutResult, c))
    } else {
      // arguments are bad, usage message will have been displayed
      None
    }
  }

  private def getBaseFromWorkingDir (workingDir : String) : String = {
    (new File(workingDir)).toURI().toString
  }

  //
  //  Local run...
  //
  def main(args: Array[String]) = {
    parseArgs (args, getBaseFromWorkingDir(System.getProperty("user.dir")),
               System.in, System.out, System.err) match {
      case Some((source : Source, result : Result, metaResult : Option[StreamResult], config : Config)) =>
        new WADLCheckerBuilder().build(source, result, metaResult, config)
      case None => /* Bad args, Ignore */
    }
  }

  //
  //  Nailgun run...
  //
  def nailMain(context : NGContext) = {
    parseArgs (context.getArgs, getBaseFromWorkingDir(context.getWorkingDirectory),
               context.in, context.out, context.err) match {
      case Some((source : Source, result : Result, metaResult : Option[StreamResult], config : Config)) =>
        new WADLCheckerBuilder().build(source, result, metaResult, config)
      case None => /* Bad args, Ignore */
    }
  }
}
