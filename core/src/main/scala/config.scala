package com.rackspace.com.papi.components.checker

import scala.reflect.BeanProperty

import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.handler.ServletResultHandler

class Config {
  //
  //  Setup appropriate factories.  We need these set to ensure config
  //  options work coccertly.
  //
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/saxonica", "com.saxonica.jaxp.SchemaFactoryImpl")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory")
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "org.apache.xerces.jaxp.validation.XMLSchemaFactory")

  //
  //  Don't allow duplicate nodes in the machine.
  //
  @BeanProperty var removeDups : Boolean = true

  //
  //  Run code to validate that the validator was correctly generated
  //
  @BeanProperty var validateChecker : Boolean = true

  //
  //  The result handler, recives and manages all results.
  //
  @BeanProperty var resultHandler : ResultHandler = new ServletResultHandler

  //
  //  Use SAXON-EE for XSD validation: This means that in cases where
  //  XSD validation needs to be done use the Saxon XSD validator
  //  instead of the default Xerces validator.  Note that the Saxon
  //  validator requires a license.
  //
  @BeanProperty var useSaxonEEValidation : Boolean = false

  //
  //  Check Well-Formed XML and JSON
  //
  @BeanProperty var checkWellFormed : Boolean = false

  //
  //  Check all XML against XSD Grammars
  //
  @BeanProperty var checkXSDGrammar : Boolean = false

  //
  //  Allow XSD grammar transform.  Transform the XML after
  //  validation, to fill in things like default values etc.
  //
  @BeanProperty var doXSDGrammarTransform : Boolean = false

  //
  //  Ensure elemets are correct
  //
  @BeanProperty var checkElements : Boolean = false

  //
  //  XPath version used in the WADL.  Can be 1 or 2. If 1 is set the
  //  Xalan implementation will be used, if 2 then Saxon will be used.
  //  Note that XPath 2 with schema awareness requires a Saxon
  //  license.
  //
  private var xpv : Int = 1

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
  @BeanProperty var checkPlainParams : Boolean = false

  //
  //  Enable preprocess extension
  //
  @BeanProperty var enablePreProcessExtension : Boolean = false

  //
  //  The XSL 1.0 engine to use.  Possible choices are Xalan, XalanC,
  //  and Saxon. Note that Saxon is an XSL 2.0 engine, but most 1.0
  //  XSLs should work fine.
  //
  private var xsle : String = "Xalan"
  private val supportedXSLEngines = Set("Xalan", "XalanC", "Saxon")

  def xslEngine : String = xsle
  def xslEngine_= (engine : String) : Unit = {
    if (!supportedXSLEngines.contains(engine)) {
      throw new IllegalArgumentException("Unrecognized XSL engine: "+
                                         engine+" supported engines: "+supportedXSLEngines)
    }
    xsle = engine
  }

  def setXSLEngine (engine : String) : Unit = { xslEngine_=(engine) }
  def getXSLEngine : String = xslEngine
}
