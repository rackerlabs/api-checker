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
package com.rackspace.com.papi.components.checker

import com.rackspace.com.papi.components.checker.handler.{ResultHandler, ServletResultHandler}

import scala.annotation.StaticAnnotation
import scala.beans.BeanProperty
import scala.reflect.runtime.universe
import scala.xml._

import com.typesafe.scalalogging.slf4j.LazyLogging

/**
 * If set the annotation states that the configuration option
 * affects the state machine in checker format, if not set the
 * config option only affects runtime.
 */
private final class AffectsChecker extends StaticAnnotation

object Config {
  private val checkerConfigTypes = {
    val affectsCheckerType = universe.typeOf[AffectsChecker]
    universe.typeOf[Config].members.filter(i => i.annotations.map(a => a.tree.tpe).contains(affectsCheckerType))
  }

  //
  //  The default version of XPath to use for rax:assert XPath
  //  statements.  Currently, the version is fixed.
  //
  val RAX_ASSERT_XPATH_VERSION = 31
  val RAX_ASSERT_XPATH_VERSION_STRING = "3.1"
}

/**
 * This class contains all the configuration options for a {@link com.rackspace.com.papi.components.checker.Validator}
 *
 * A license is required for any SaxonEE functionality.  SaxonEE can be declared in <code>xslEngine</code>
 * and <code>xsdEngine</code>.
 */
class Config extends LazyLogging {
  //
  //  Setup appropriate factories.  We need these set to ensure config
  //  options work correctly.
  //
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/saxonica", "com.saxonica.jaxp.SchemaFactoryImpl")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "org.apache.xerces.jaxp.validation.XMLSchemaFactory")

  //
  //  Don't allow duplicate nodes in the machine.
  //
  @BeanProperty
  @AffectsChecker
  var removeDups : Boolean = true

  //
  //  Run code to validate that the validator was correctly generated
  //
  @BeanProperty var validateChecker : Boolean = true

  //
  //  The result handler, receives and manages all results.
  //
  @BeanProperty var resultHandler : ResultHandler = new ServletResultHandler


  //
  //  Use Xerces or SAXON-EE for XSD validation.  Note that the Saxon
  //  validator requires a license.
  //
  private var xsde : String = "Xerces"
  private val supportedXSDEngines = Set("Xerces", "SaxonEE")

  def xsdEngine : String = xsde
  def xsdEngine_= (engine : String) : Unit = {
    if (!supportedXSDEngines.contains(engine)) {
      throw new IllegalArgumentException("Unrecognized XSL engine: "+
        engine+" supported engines: "+supportedXSDEngines)
    }
    xsde = engine
  }

  def setXSDEngine (engine : String) : Unit = { xsdEngine_=(engine) }
  def getXSDEngine : String = xsdEngine


  //
  //  Use SAXON-EE for XSD validation: This means that in cases where
  //  XSD validation needs to be done use the Saxon XSD validator
  //  instead of the default Xerces validator.  Note that the Saxon
  //  validator requires a license.
  //
  @Deprecated
  def setUseSaxonEEValidation( use : Boolean ) : Unit = {

    depUseSaxonEEValidation = use

    if ( use ) {

      setXSLEngine( "SaxonEE" )
      setXSDEngine( "SaxonEE" )
    }
    else {

      setXSDEngine( "Xerces" )
    }
  }

  private var depUseSaxonEEValidation : Boolean = false

  @Deprecated
  def getUseSaxonEEValidation() : Unit = {

    xsdEngine == "SaxonEE"
  }

  @Deprecated
  def useSaxonEEValidation = getUseSaxonEEValidation()

  @Deprecated
  def useSaxonEEValidation_= ( use : Boolean ) : Unit = setUseSaxonEEValidation( use )

  //
  //  Check Well-Formed XML and JSON
  //
  @BeanProperty
  @AffectsChecker
  var checkWellFormed : Boolean = false

  //
  //  Check all XML against XSD Grammars
  //
  @BeanProperty
  @AffectsChecker
  var checkXSDGrammar : Boolean = false

  //
  //  Allow XSD grammar transform.  Transform the XML after
  //  validation, to fill in things like default values etc.
  //
  @BeanProperty
  @AffectsChecker
  var doXSDGrammarTransform : Boolean = false

  //
  //  Check all JSON against JSON Schema Grammars
  //
  @BeanProperty
  @AffectsChecker
  var checkJSONGrammar : Boolean = false

  //
  //  Ensure elemets are correct
  //
  @BeanProperty
  @AffectsChecker
  var checkElements : Boolean = false

  //
  //  XPath version used in the WADL.
  //
  //  Allowed values are:
  //  1 or 10 for Version 1.0  (this is the default)
  //  2 or 20 for Version 2.0
  //  30 for Version 3.0
  //  31 for Version 3.1
  //
  //  1 and 2 is specified for backward compatibility. You should use
  //  20 or 30.
  //
  //  Note that XPath schema awareness, high order functions, expath
  //  extensions and other fancy features require a Saxon license.
  //
  private val allowedVersions : Set[Int] = Set(1, 2, 10, 20, 30, 31)
  private var xpv : Int = 10

  @AffectsChecker
  def xpathVersion : Int = xpv

  def xpathVersion_= (version : Int) : Unit = {
    if (!allowedVersions(version))
      throw new IllegalArgumentException(s"XPath valid version values are $allowedVersions")
    xpv = version match {
      case 1 => logger.warn ("Use of 1 is deprecated to specify XPath version use 10 instead to specify 1.0") ; 10
      case 2 => logger.warn ("Use of 2 is deprecated to specify XPath version use 20 instead to specify 2.0") ; 20
      case v : Int => v
    }
  }

  def setXPathVersion (version : Int) : Unit = { xpathVersion_=(version) }
  def getXPathVersion : Int = xpathVersion

  //
  //  Check plain parameters
  //
  @BeanProperty
  @AffectsChecker
  var checkPlainParams : Boolean = false

  //
  //  Enable preprocess extension
  //
  @BeanProperty
  @AffectsChecker
  var enablePreProcessExtension : Boolean = true

  //
  //  Enable ignore XSD extension
  //
  @BeanProperty
  @AffectsChecker
  var enableIgnoreXSDExtension : Boolean = true

  //
  //  Enable ignore JSON Schema extension
  //
  @BeanProperty
  @AffectsChecker
  var enableIgnoreJSONSchemaExtension : Boolean = true

  //
  //  Enable message extension
  //
  @BeanProperty
  @AffectsChecker
  var enableMessageExtension : Boolean = true

  //
  //  Enable rax-roles extension.
  //
  //  Note that enabling rax-roles will also enable the following:
  //
  //  1. The rax:anyMatch extension
  //  2. The rax:assert extension
  //  3. Header Checks
  //
  //  ...these features are required to implement rax:roles.
  //
  @BeanProperty
  @AffectsChecker
  var enableRaxRolesExtension : Boolean = false

  //
  //  Enable the rax-representation extension.
  //
  //  The extension is intended to validate representations that are
  //  not in the usual area.  For example representations within
  //  representations.  For example, JSON content inside XML content
  //  or vice versa.  A <rax:representation/> works like a
  //  <wadl:representation> except for 3 major differences:
  //
  //
  //  1. A rax:representation contains an XPath that is expected to
  //  return a string containing the representation.  Usually this
  //  XPath points to the parent representation. In XML, the parent
  //  body is placed in the local contex but can also be accessed via
  //  the $_ or $body variable.
  //
  //  2. In addition to looking at the body a rax:representation may
  //  look at the uri ($req:uri), method ($req:method), and headers
  //  (with a call to req:header or req:headers).  The extension may
  //  be placed in a wadl:method/wadl:request,
  //  wadl:method/wadl:request/wadl:representation or in another
  //  rax:representation.  The extension may also be placed in
  //  wadl:resources where it applies to all methods globally or on
  //  wadl:resource which applies to all methods of that resource and
  //  if @applyToChildren is set to true then it will also apply to
  //  all subresources as well.
  //
  //  Note that having the extension on a wadl:representation will
  //  enable wellformed checks.
  //
  //  3. A rax:representation has an optional name.
  //
  @BeanProperty
  @AffectsChecker
  var enableRaxRepresentationExtension : Boolean = true

  //
  //  Mask rax-roles with 404 and 405 errors. By default rax-roles
  //  response with a 403 if there is a role mismatch, if
  //  maskRaxRoles403 is true then the respose will be 404 if no
  //  methods are accessible or 405 if some methods are available.
  //
  @BeanProperty
  @AffectsChecker
  var maskRaxRoles403 : Boolean = false

  //
  //  Enable rax:captureHeader extension attribute.  This extension
  //  allows capturing the contents of a WADL parameter to a request
  //  header. Currently works with template, header, XML, and JSON
  //  plain parameters.
  //
  //  Additionally, this enables the <rax:captureHeader/> element
  //  which allows capturing a header based on an XPath 3.1. The XPath
  //  follows the same rules as Assert XPath (see
  //  enableAssertExtension), but expects a sequence of strings (which
  //  are stored as header values) rather than a boolean.
  //
  @BeanProperty
  @AffectsChecker
  var enableCaptureHeaderExtension : Boolean = true

  //
  //  When rax:anyMatch boolean attribute is specificed on a
  //  parameter, enable matching any value of a parameter when
  //  multiple parameters of the same name are specificed.
  //
  @BeanProperty
  @AffectsChecker
  var enableAnyMatchExtension : Boolean = true

  //
  //  When the rax:authenticatedBy attribute is specified on a
  //  resource or method, requests are required to have an
  //  X-Authenticated-By header with at least one of the values
  //  specified.
  //
  @BeanProperty
  @AffectsChecker
  var enableAuthenticatedByExtension : Boolean = false

  //
  //  Enables rax:assert extensions which allows specifying an
  //  XPath3.1 assertion.  The assertion works similar to XSD 1.1
  //  assertion and supports @message, @code for reporting errors.
  //
  //  The assertion may look at the uri ($req:uri), method ($req:method),
  //  request body ($_ or $body), and headers (with a call to
  //  req:header or req:headers).  The extension may be placed in a
  //  wadl:method/wadl:request,
  //  wadl:method/wadl:request/wadl:representation.  The extension may
  //  also be placed in wadl:resources where it applies to all methods
  //  globally or on wadl:resource which applies to all methods of
  //  that resource and if @applyToChildren is set to true then it
  //  will also apply to all subresources as well.
  //
  //  Note that having an assertion on a wadl:representation will
  //  enable wellformed checks.
  @BeanProperty
  @AffectsChecker
  var enableAssertExtension : Boolean = true


  //
  // Ensure that method labels are always preserved in the state
  // machine, even at the cost of having a more complex machine.  If
  // the value is set to false then method labels may be dropped by
  // optimization stages in order to consolidate steps.
  //
  // Method labels are typically needed for reporting and debugging
  // purposes, they have no effect on validation.
  //
  @BeanProperty
  @AffectsChecker
  var preserveMethodLabels : Boolean = false

  //
  //  The XSL engine to use.  Possible choices are Xalan, XalanC,
  //  SaxonHE and SaxonEE. SaxonEE requires a Saxon License.
  //
  //  - Xalan, XalanC and SaxonEE support XSLT 1.0
  //  - SaxonHE and SaxonEE support XSLT 3.0
  //
  //  There is no formal XSL 2.0 engine however XSLT 3.0 is highly
  //  backwards compatible with XSLT 2.0 and 2.0 stylesheets will work
  //  unmodified in most cases.
  //
  private var xsle : String = "XalanC"
  private val supportedXSLEngines = Set("Xalan", "XalanC", "SaxonHE", "SaxonEE",
  "Saxon" )  // NOTE:  "Saxon" is deprecated as well, remove when removing depUseSaxonEEValidation

  @AffectsChecker
  def xslEngine : String = xsle
  def xslEngine_= (engine : String) : Unit = {

    if (!supportedXSLEngines.contains(engine)) {
      throw new IllegalArgumentException("Unrecognized XSL engine: "+
                                         engine+" supported engines: "+supportedXSLEngines)
    }

    xsle =  engine match {
      case "Saxon" => logger.warn("Use of Saxon to specify XSL engine is depricated you should specify SaxonHE or SaxonEE")
                      if (depUseSaxonEEValidation) "SaxonEE" else "SaxonHE"
      case _ => engine
    }
  }

  def setXSLEngine (engine : String) : Unit = { xslEngine_=(engine) }
  def getXSLEngine : String = xslEngine

  //
  //  This is an optimization where the well formness check and
  //  multiple XPath checks can be merged into a single check.
  //
  @BeanProperty
  @AffectsChecker
  var joinXPathChecks : Boolean = false

  //
  //  Check that required headers are set.
  //

  @BeanProperty
  @AffectsChecker
  var checkHeaders : Boolean = false

  //
  //  Fill in required parameters if a default value is specified.
  //  This currently only works with header parameters.
  //

  @BeanProperty
  @AffectsChecker
  var setParamDefaults : Boolean = false

  //
  // Preserve the ability to process the request body always. Setting
  // this to true ensures that the request remains readable after
  // validation is performed. Setting this to true, however, may also
  // disable some optimizations.
  //

  @BeanProperty
  @AffectsChecker
  var preserveRequestBody : Boolean = false

  //
  // The name of the agent used in WARNING headers. Can be a host:port
  // or a pseudonym, using - as the name allowed when the name of the
  // agent is unknown.
  //
  @BeanProperty
  @AffectsChecker
  var warnAgent : String = "-"

  //
  // Enable warning headers per RFC 7234. These warnings come into
  // play when a transformation is applied to a message body, for
  // example.
  //
  @BeanProperty
  @AffectsChecker
  var enableWarnHeaders : Boolean = true

  /**
   * Returns checker metadata (<meta/> element in checker format) for
   * the config options that affect the checker.
   */
  def checkerMetaElem : Elem = {
    <meta xmlns="http://www.rackspace.com/repose/wadl/checker">
      {
        checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
      }
    </meta>
  }

  /**
   * Provides a map from checker meta option to its value.
   */
  def checkerMetaMap : Map[String, Any] = {
    val conf_ref = universe.runtimeMirror(this.getClass.getClassLoader).reflect(this)
    val fields = Config.checkerConfigTypes.filter(s => !s.isMethod).map(s =>s.asTerm).map(s => {
      (s.name.toString.trim -> conf_ref.reflectField(s).get)
    }).toList
    val methods = Config.checkerConfigTypes.filter(s => s.isMethod).map(s =>s.asMethod).map(s => {
      (s.name.toString.trim -> conf_ref.reflectMethod(s).apply())
    }).toList
    Map() ++ (fields ::: methods)
  }
}
