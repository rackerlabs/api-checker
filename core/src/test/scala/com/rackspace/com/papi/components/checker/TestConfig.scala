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

import com.rackspace.com.papi.components.checker.handler._

import scala.language.implicitConversions

object TestConfig {
  val assertHandler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(),
                                                                    new AssertResultHandler(),
                                                                    new RunAssertionsHandler(),
                                                                    new ServletResultHandler()))


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean, preserveRequestBody : Boolean,
             maskRaxRoles403 : Boolean, enableCaptureHeaderExtension: Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension, enableRaxRolesExtension, preserveRequestBody,
      maskRaxRoles403)

    config.enableCaptureHeaderExtension = enableCaptureHeaderExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean, preserveRequestBody : Boolean,
             maskRaxRoles403 : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension, enableRaxRolesExtension, preserveRequestBody)

    config.maskRaxRoles403 = maskRaxRoles403

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean, preserveRequestBody : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension, enableRaxRolesExtension)

    config.preserveRequestBody = preserveRequestBody

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean,
             enableRaxRolesExtension: Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
      xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
      xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
      checkJSONGrammar, enableIgnoreJSONSchemaExtension)

    config.enableRaxRolesExtension = enableRaxRolesExtension

    config
  }


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean, enableIgnoreJSONSchemaExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension,
                       checkJSONGrammar)

    config.enableIgnoreJSONSchemaExtension = enableIgnoreJSONSchemaExtension

    config
  }


  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean,
             checkJSONGrammar : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension, enableMessageExtension)

    config.checkJSONGrammar = checkJSONGrammar

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean, enableMessageExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders, enableIgnoreXSDExtension)

    config.enableMessageExtension = enableMessageExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean,
             enableIgnoreXSDExtension : Boolean) : Config = {

    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks, checkHeaders)

    config.enableIgnoreXSDExtension = enableIgnoreXSDExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean, checkHeaders : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine, joinXPathChecks)

    config.checkHeaders = checkHeaders

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String,
             joinXPathChecks : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension,
                       xslEngine)

    config.joinXPathChecks = joinXPathChecks

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean, xslEngine : String) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements,
                       xpathVersion, checkPlainParams, doXSDGrammarTransform, enablePreProcessExtension)

    config.xslEngine = xslEngine

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean,
             enablePreProcessExtension : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion, checkPlainParams, doXSDGrammarTransform)

    config.enablePreProcessExtension = enablePreProcessExtension

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean, doXSDGrammarTransform : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion, checkPlainParams)

    config.doXSDGrammarTransform = doXSDGrammarTransform

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int,
             checkPlainParams : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements, xpathVersion)
    config.checkPlainParams = checkPlainParams

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean,
             checkXSDGrammar : Boolean, checkElements : Boolean, xpathVersion : Int) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar, checkElements)

    //
    //  Convert version here to avoid WARNINGS on a lot of tests.  We
    //  have tests elsewhere that ensure the version conversion occurs
    //  currectly.
    //
    config.xpathVersion = xpathVersion match {
      case 1 => 10
      case 2 => 20
      case _ => xpathVersion
    }

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean, checkXSDGrammar : Boolean, checkElements : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed, checkXSDGrammar)
    config.checkElements = checkElements

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean, checkXSDGrammar : Boolean) : Config = {
    val config = apply(removeDups, saxoneeValidation, wellFormed)
    config.checkXSDGrammar = checkXSDGrammar

    config
  }

  def apply (removeDups : Boolean, saxoneeValidation : Boolean, wellFormed : Boolean) : Config = {
    val config = apply(saxoneeValidation, wellFormed)
    config.removeDups = removeDups

    config
  }

  def apply (saxoneeValidation : Boolean, wellFormed : Boolean) : Config = {
    val config = new Config
    config.resultHandler = assertHandler
    config.setXSDEngine( if ( saxoneeValidation ) { "SaxonEE" } else { "Xerces" } )
    config.checkWellFormed = wellFormed

    config
  }

  def apply() : Config = {
    apply(false, false)
  }
}
