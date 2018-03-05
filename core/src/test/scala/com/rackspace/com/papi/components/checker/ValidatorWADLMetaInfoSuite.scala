/***
 *   Copyright 2018 Rackspace US, Inc.
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

import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder
import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder

import com.rackspace.cloud.api.wadl.Converters._

import java.nio.charset.StandardCharsets.UTF_8
import java.io.ByteArrayOutputStream

import javax.xml.transform.stream.StreamSource
import javax.xml.transform.stream.StreamResult

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.apache.logging.log4j.Level


@RunWith(classOf[JUnitRunner])
class ValidatorWADLMetaInfoSuite extends BaseValidatorSuite with LogAssertions {

  //
  // A simple WADL to test with...
  //
  val simpleWADL = ("myApp.wadl", <application xmlns="http://wadl.dev.java.net/2009/02"
                                                 xmlns:rax="http://docs.rackspace.com/api"
         >
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/b">
                   <method name="GET" rax:roles="admin">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>)

  //
  //  Setup a config
  //
  val config = new Config()

  config.enableRaxRolesExtension = true
  config.enableMessageExtension = true


  test("When an output for metadata info is supplied, it should be filled in with information about the related to the checker.") {
    val bout = new ByteArrayOutputStream
    val validator = Validator("testValidator", new StreamSource(simpleWADL._2, simpleWADL._1), Some(new StreamResult(bout)), config)

    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }

  test("When logging is set to TRACE metadata info should be written to the log") {
    val traceLog = log(Level.TRACE) {
      val validator = Validator("testValidator2", new StreamSource(simpleWADL._2, simpleWADL._1), None, config)
    }
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...
  }


  test("When logging is set to INFO metadata info should not be written to the log") {
    val traceLog = log(Level.INFO) {
      val validator = Validator("testValidator3", new StreamSource(simpleWADL._2, simpleWADL._1), None, config)
    }
    assertEmpty(traceLog)
  }


  test("When logging is set to TRACE and output for metadata is supplied  metadata info should be written to both places") {
    val bout = new ByteArrayOutputStream

    val traceLog = log(Level.TRACE) {
      val validator = Validator("testValidator4", new StreamSource(simpleWADL._2, simpleWADL._1), Some(new StreamResult(bout)), config)
    }

    //
    //  Check log
    //
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...

    //
    //  Check out
    //
    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }


  test("When a WADL is converted to checker format if out for metadata info is supplied it should be filled in with related info"){
    val builder = new WADLCheckerBuilder
    val bout = new ByteArrayOutputStream
    val checkerOut = new StreamResult(new ByteArrayOutputStream)

    builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), checkerOut, Some(new StreamResult(bout)), config)
    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }

  test("When a WADL is converted to checker format if logging is set to TRACE, metadata info should make it to the log") {
    val builder = new WADLCheckerBuilder
    val checkerOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.TRACE) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), checkerOut, None, config)
    }
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...
  }


  test("When a WADL is converted to checker format if logging is set to INFO, metadata info should not make it to the log") {
    val builder = new WADLCheckerBuilder
    val checkerOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.INFO) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), checkerOut, None, config)
    }
    assertEmpty(traceLog)
  }


  test("When a WADL is converted to checker format if logging is set to TRACE and output to metadata is supplied metadata info should make it to both places") {
    val builder = new WADLCheckerBuilder
    val bout = new ByteArrayOutputStream
    val checkerOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.TRACE) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), checkerOut, Option(new StreamResult(bout)), config)
    }

    //
    // Check Log
    //
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...

    //
    // Check out
    //
    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }


  test("When a WADL is converted to dot format if out for metadata info is supplied it should be filled with related info") {
    val builder = new WADLDotBuilder
    val bout = new ByteArrayOutputStream
    val dotOut = new StreamResult(new ByteArrayOutputStream)

    builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), dotOut, Some(new StreamResult(bout)), config, false, false)
    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }

  test("When a WADL is converted to dot format if logging is set to TRACE, metadata info should make it to the log") {
    val builder = new WADLDotBuilder
    val dotOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.TRACE) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), dotOut, None, config, false, false)
    }
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...
  }

  test("When a WADL is converted to dot format if logging is set to INFO, metadata info should not make it to the log") {
    val builder = new WADLDotBuilder
    val dotOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.INFO) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), dotOut, None, config, false, false)
    }
    assertEmpty(traceLog)
  }


  test("When a WADL is converted to dot format if logging is set to TRACE and output to metadata is supplied, metadata info should make it to both places") {
    val builder = new WADLDotBuilder
    val bout = new ByteArrayOutputStream
    val dotOut = new StreamResult(new ByteArrayOutputStream)

    val traceLog = log(Level.TRACE) {
      builder.build(new StreamSource(simpleWADL._2, simpleWADL._1), dotOut, Option(new StreamResult(bout)), config, false, false)
    }

    //
    // Check log
    //
    assert(traceLog, "myApp.wadl") // WADL name must appear
    assert(traceLog, "enableRaxRolesExtension = true") // set this
    assert(traceLog, "enableMessageExtension = true")  // set this
    assert(traceLog, "doXSDGrammarTransform = false")  // didn't set this...

    //
    // Check out
    //
    val info = new String(bout.toByteArray, UTF_8)
    assert(info.contains("myApp.wadl")) // WADL name must appear
    assert(info.contains("enableRaxRolesExtension = true")) // set this
    assert(info.contains("enableMessageExtension = true"))  // set this
    assert(info.contains("doXSDGrammarTransform = false"))  // didn't set this...
  }

}
