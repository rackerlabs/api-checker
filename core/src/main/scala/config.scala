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
  //  Ensure elemets are correct
  //
  @BeanProperty var checkElements : Boolean = false
}
