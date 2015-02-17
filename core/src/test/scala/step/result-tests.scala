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
package com.rackspace.com.papi.components.checker.step

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class ResultSuite extends BaseStepSuite {
  val e12 = new MismatchResult("Mismatch", StepContext(-1), "12")
  val e13 = new MismatchResult("Mismatch", StepContext(-1), "13")
  val e14 = new BadMediaTypeResult("Bad Media Type", StepContext(-1), "14", 8)
  val e15 = new MethodFailResult("Bad Method", StepContext(-1), "15", 8, Map("Foo"->"Bar"))
  val e16 = new BadMediaTypeResult("Bad MT", StepContext(-1), "16", 9)

  val e10 = new MultiFailResult(Array(e12, e13, e14, e15), "10")

  e10.addStepId("9")
  e10.addStepId("8")

  val e7 = new MethodFailResult("Bad Method", StepContext(-1), "7", 8, Map("Foo"->"Bar"))

  e7.addStepId("6")
  e7.addStepId("5")

  val e3 = new MultiFailResult(Array(e10, e7), "3")

  e3.addStepId("2")
  e3.addStepId("1")
  e3.addStepId("S")

  val e11 = new MultiFailResult(Array(e3, e16), "0")

  test("Given e3, the result should be bad media type.") {
    assert (e3.code == 415, "The error code should match bad media type")
    assert (e3.message == "Bad Media Type", "The message should match bad media type")
  }

  test("Given e11, the result should be a *different* bad media type.") {
    assert (e11.code == 415, "The error should match bad media type")
    assert (e11.message == "Bad MT", "The message should match bad mt")
  }

  test("Confirm path for e12") {
    assert (e12.path == "(12)")
  }

  test("Confirm path for e13") {
    assert (e13.path == "(13)")
  }

  test("Confirm path for e14") {
    assert (e14.path == "[14]")
  }

  test("Confirm path for e15") {
    assert (e15.path == "[15]")
  }

  test("Confirm path for e16") {
    assert (e16.path == "[16]")
  }

  test("Confirm path for e7") {
    assert (e7.path == "[5 6 7]")
  }

  test ("Confirm path for e10") {
    assert (e10.path == "{8 9 10 (12) (13) [14] [15]}*14*")
  }

  test("Confirm path for e3") {
    assert (e3.path == "{S 1 2 3 {8 9 10 (12) (13) [14] [15]}*14* [5 6 7]}*14*")
  }

  test ("Confirm path for e11") {
    assert (e11.path == "{0 {S 1 2 3 {8 9 10 (12) (13) [14] [15]}*14* [5 6 7]}*14* [16]}*16*")
  }
}
