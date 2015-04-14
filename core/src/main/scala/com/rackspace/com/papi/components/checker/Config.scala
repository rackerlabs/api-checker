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
import scala.reflect.BeanProperty
import scala.reflect.runtime.universe
import scala.xml._

/**
 * If set the annotation states that the configuration option
 * affects the state machine in checker format, if not set the
 * config option only affects runtime.
 */
private final class AffectsChecker extends StaticAnnotation

object Config {
  private val checkerConfigTypes = {
    val affectsCheckerType = universe.typeOf[AffectsChecker]
    universe.typeOf[Config].members.filter(i => i.annotations.map(a => a.tpe).contains(affectsCheckerType))
  }
}

/**
 * This class contains all the configuration options for a {@link com.rackspace.com.papi.components.checker.Validator}
 *
 * A license is required for any SaxonEE functionality.  SaxonEE can be declared in <code>xslEngine</code>
 * and <code>xsdEngine</code>.
 */
class Config {
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
  //  The result handler, recives and manages all results.
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
  //  XPath version used in the WADL.  Can be 1 or 2. If 1 is set the
  //  Xalan implementation will be used, if 2 then Saxon will be used.
  //  Note that XPath 2 with schema awareness requires a Saxon
  //  license.
  //
  private var xpv : Int = 1

  @AffectsChecker
  def xpathVersion : Int = xpv

  def xpathVersion_= (version : Int) : Unit = {
    if ((version != 1) && (version != 2))
      throw new IllegalArgumentException("XPath version can only be 1 or 2.")
    xpv = version
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
  //  Enable rax-roles extension
  //
  @BeanProperty
  @AffectsChecker
  var enableRaxRolesExtension : Boolean = false

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
  //  Enable capture header extension.  This extension allows
  //  capturing the contents of a WADL parameter to a request
  //  header. Currently works with template, header, and XML plain
  //  parameters.
  @BeanProperty
  @AffectsChecker
  var enableCaptureHeaderExtension : Boolean = true

  //
  //  The XSL 1.0 engine to use.  Possible choices are Xalan, XalanC,
  //  and Saxon. Note that Saxon is an XSL 2.0 engine, but most 1.0
  //  XSLs should work fine.
  //
  private var xsle : String = "XalanC"
  private val supportedXSLEngines = Set("Xalan", "XalanC", "SaxonHE", "SaxonEE",
  "Saxon" )  // NOTE:  "Saxon" is deprecated as well, remove when removing depUseSaxonEEValidation

  def xslEngine : String = xsle
  def xslEngine_= (engine : String) : Unit = {

    if (!supportedXSLEngines.contains(engine)) {
      throw new IllegalArgumentException("Unrecognized XSL engine: "+
                                         engine+" supported engines: "+supportedXSLEngines)
    }

    xsle =  engine match {
      case "Saxon" => if (depUseSaxonEEValidation) "SaxonEE" else "SaxonHE"
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
  // Preserve the ability to process the request body always. Setting
  // this to true ensures that the request remains readable after
  // validation is performed. Setting this to true, however, may also
  // disable some optimizations.
  //

  @BeanProperty
  @AffectsChecker
  var preserveRequestBody : Boolean = false

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
