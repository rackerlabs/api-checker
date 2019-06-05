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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner
import org.apache.logging.log4j.Level

/**
 * Verifies that deprecated methods correctly set the new Config object.
 * <p>
 * Delete this when the Config.useSaxonEEValidation related methods are deleted.
 */
@RunWith(classOf[JUnitRunner])
class ConfigSuite extends FunSuite with LogAssertions {

  test( "UseSaxonEEValidation == true : xsdEngine == SaxonEE, xslEngine == SaxonEE ") {

    val config = new Config

    config.setUseSaxonEEValidation( true )

    assert( config.xsdEngine === "SaxonEE" )
    assert( config.xslEngine === "SaxonEE" )
  }

  test( "UseSaxonEEValidation == false, xslEngine == Saxon : xsdEngine == Xerces, xslEngine == SaxonHE") {
    val config = new Config
    val configLog = log(Level.WARN) {
      config.setUseSaxonEEValidation( false )
      config.setXSLEngine( "Saxon" )
    }

    assert( config.xsdEngine === "Xerces" )
    assert( config.xslEngine === "SaxonHE" )
    assert(configLog, "Saxon to specify XSL engine is depricated")
  }

  test( "UseSaxonEEValidation == false : xsdEngine == Xerces, xslEngine == XalanC") {

    val config = new Config

    assert( config.xsdEngine === "Xerces" )
    assert( config.xslEngine === "XalanC" )
  }


  test ("XPathVersion 1, should warn and set to 10") {
    val config = new Config
    val configLog = log(Level.WARN) {
      config.xpathVersion = 1
    }

    assert (config.xpathVersion == 10)
    assert (configLog, "1 is deprecated to specify XPath version")
  }

  test ("XPathVersion 2, should warn and set to 20") {
    val config = new Config
    val configLog = log(Level.WARN) {
      config.xpathVersion = 2
    }

    assert (config.xpathVersion == 20)
    assert (configLog, "2 is deprecated to specify XPath version")
  }
}
