/***
 *   Copyright 2017 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.macros

import scala.tools.reflect.ToolBox

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.junit.JUnitRunner

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import scala.reflect.runtime.universe._

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

import TimeFunction._

@RunWith(classOf[JUnitRunner])
class TimeFunctionSuite extends FunSuite with BeforeAndAfterAll {

  val toolbox = currentMirror.mkToolBox()
  var currentProp : Option[String] = None

  override def beforeAll = currentProp = Option(System.getProperty(TIME_PROP))
  override def afterAll = {
    if (currentProp.isEmpty) {
      System.clearProperty(TIME_PROP)
    } else {
      System.setProperty(TIME_PROP, currentProp.get)
    }
  }

  test ("If the system property is set then the output should contain some millis"){
    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)

    Console.withErr(printOutStream) {
      System.setProperty(TIME_PROP, "")
      toolbox.eval(toolbox.parse("""com.rackspace.com.papi.components.checker.macros.TimeFunction.timeFunction("print", {Console.err.println("hello!")})"""))
    }
    val outString = outStream.toString
    assert(outString.contains("hello!\n"))
    assert(outString.contains("print :"))
    assert(outString.contains("Millis"))
  }

  test ("If the system property is not set then the output should match exactly"){
    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)

    Console.withErr(printOutStream) {
      System.clearProperty(TIME_PROP)
      toolbox.eval(toolbox.parse("""com.rackspace.com.papi.components.checker.macros.TimeFunction.timeFunction("foo", {Console.err.println("foo!")})"""))
    }
    assert(outStream.toString == "foo!\n")
  }

  test ("If the system property is set then the output should contain some millis (should evaluate)"){
    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)
    var res : Int = 0
    Console.withErr(printOutStream) {
      System.setProperty(TIME_PROP, "")
      res = toolbox.eval(toolbox.parse("""com.rackspace.com.papi.components.checker.macros.TimeFunction.timeFunction("add", {7+2})""")).asInstanceOf[Int]
    }
    val outString = outStream.toString
    assert(res == 9)
    assert(outString.contains("add :"))
    assert(outString.contains("Millis"))
  }

  test ("If the system property is not set then there should be no output but should evaluate"){
    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)
    var res : Int = 0

    Console.withErr(printOutStream) {
      System.clearProperty(TIME_PROP)
      res = toolbox.eval(toolbox.parse("""com.rackspace.com.papi.components.checker.macros.TimeFunction.timeFunction("add", {10+5})""")).asInstanceOf[Int]
    }
    assert(res == 15)
    assert(outStream.toString.isEmpty)
  }

  test ("If the system property is set then the output should contain some millis that matches time elapsed"){
    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)
    Console.withErr(printOutStream) {
      System.setProperty(TIME_PROP, "")
      toolbox.eval(toolbox.parse("""com.rackspace.com.papi.components.checker.macros.TimeFunction.timeFunction("sleep", {Thread.sleep(500)})"""))
    }
    val parts = outStream.toString.split(" ")
    assert(parts(0) == "[sleep")
    assert(parts(1) == ":")
    assert(parts(2).toInt >= 500) // Make sure we capture time elapse
    assert(parts(3) == "Millis]\n")
  }

}
