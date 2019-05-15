package com.rackspace.com.papi.components.checker.cli

import com.rackspace.com.papi.components.checker.Config
import scopt.OptionParser

import scala.language.implicitConversions

class CheckerParser(programName: String, config: Config) extends OptionParser[Unit](programName) {

  help('h', "help").text("Display usage.")

  version("version").text("Display version.")

  opt[Unit]('d', "remove-dups").text("Remove duplicate nodes. Default: false")
    .foreach(_ => config.removeDups = true)

  opt[Unit]('r', "rax-roles").text("Enable Rax-Roles extension. Default: false")
    .foreach(_ => config.enableRaxRolesExtension = true)

  opt[Unit]('T', "rax-is-tenant").text("Enable Rax-Is-Tenant extension. Default: false")
    .foreach(_ => config.enableRaxIsTenantExtension = true)

  opt[Unit]('R', "disable-rax-representation").text("Disable Rax-Representation extension. Default: false")
    .foreach(_ => config.enableRaxRepresentationExtension = false)

  opt[Unit]('M', "rax-roles-mask-403s").text("When Rax-Roles is enable mask 403 errors with 404 or 405s. Default: false")
    .foreach(_ => config.maskRaxRoles403 = true)

  opt[Unit]('u', "authenticated-by").text("Enable Authenticated-By extension. Default: false")
    .foreach(_ => config.enableAuthenticatedByExtension = true)

  opt[Unit]('w', "well-formed").text("Add checks to ensure that XML and JSON are well formed. Default: false")
    .foreach(_ => config.checkWellFormed = true)

  opt[Unit]('j', "join-xpaths").text("Join multiple XPath and XML well-formed checks into a single check. Default: false")
    .foreach(_ => config.joinXPathChecks = true)

  opt[Unit]('g', "xsd-grammar-transform").text("Transform the XML after validation, to fill in things like default values etconfig. Default: false")
    .foreach(_ => config.doXSDGrammarTransform = true)

  opt[Unit]('b', "preserve-req-body").text("Ensure that the request body is preserved after validating the request.")
    .foreach(_ => config.preserveRequestBody = true)

  opt[Unit]('L', "preserve-method-labels").text("Ensure that method labels are always preserved.")
    .foreach(_ => config.preserveMethodLabels = true)

  opt[Unit]('x', "xsd").text("Add checks to ensure that XML validates against XSD grammar Default: false")
    .foreach(_ => config.checkXSDGrammar = true)

  opt[Unit]('J', "json").text("Add checks to ensure that JSON validates against JSON Schema grammar Default: false")
    .foreach(_ => config.checkJSONGrammar = true)

  opt[Unit]('l', "element").text("Add checks to ensure that XML requests use the correct element : false")
    .foreach(_ => config.checkElements = true)

  opt[Unit]('H', "header").text("Add checks to ensure that required headers are passed in: false")
    .foreach(_ => config.checkHeaders = true)

  opt[Unit]('s', "setParamDefaults").text("Fill in required parameters if a default value is specified Default: false")
    .foreach(_ => config.setParamDefaults = true)

  opt[Unit]('p', "plain").text("Add checks for plain parameters : false")
    .foreach(_ => config.checkPlainParams = true)

  opt[Unit]('P', "disable-preproc-ext").text("Disable preprocess extension : false")
    .foreach(_ => config.enablePreProcessExtension = false)

  opt[Unit]('i', "disable-ignore-xsd-ext").text("Disable Ignore XSD  extension : false")
    .foreach(_ => config.enableIgnoreXSDExtension = false)

  opt[Unit]('I', "disable-ignore-json-ext").text("Disable Ignore JSON Schema  extension : false")
    .foreach(_ => config.enableIgnoreJSONSchemaExtension = false)

  opt[Unit]('m', "disable-message-ext").text("Disable Message extension : false")
    .foreach(_ => config.enableMessageExtension = false)

  opt[Unit]('c', "disable-capture-header-ext").text("Disable capture header extension : false")
    .foreach(_ => config.enableCaptureHeaderExtension = false)

  opt[Unit]('a', "disable-any-match").text("Disable any match extension : false")
    .foreach(_ => config.enableAnyMatchExtension = false)

  opt[Unit]('k', "disable-rax-assert").text("Disable Rax-Assert extension : false")
    .foreach(_ => config.enableAssertExtension = false)

  opt[Unit]('W', "disable-warn-headers").text("Disable warn headers : false")
    .foreach(_ => config.enableWarnHeaders = false)

  opt[String]('A', "warn-agent").text("The name of the agent used in WARNING headers. Default: -")
    .valueName("agent-name")
    .withFallback(() => "-")
    .foreach(x => config.warnAgent = x)

  opt[String]('E', "xsl-engine").text("The name of the XSLT engine to use. Possible names are Xalan, XalanC, SaxonHE, SaxonEE.  Default: XalanC")
    .valueName("xsl-engine")
    .withFallback(() => "XalanC")
    .foreach(x => config.xslEngine = x)

  opt[String]('S', "xsd-engine").text("The name of the XSD engine to use. Possible names are Xerces, SaxonEE.  Default: Xerces")
    .valueName("xsd-engine")
    .withFallback(() => "Xerces")
    .foreach(x => config.xsdEngine = x)

  opt[Unit]('D', "dont-validate").text("Don't validate produced checker Default: false")
    .foreach(_ => config.validateChecker = false)

  opt[Int]('t', "xpath-version").text("XPath version to use. Can be 10, 20, 30, 31 for 1.0, 2.0, 3.0, and 3.1. Default: 10")
    .valueName("n")
    .withFallback(() => 10)
    .foreach(x => config.xpathVersion = x)

}
