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
package com.rackspace.com.papi.components.checker.cli

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

import org.clapper.argot.ArgotUsageException

import javax.xml.transform.stream._

@RunWith(classOf[JUnitRunner])
class Wadl2DotSuite extends FunSuite {

  test ("--help should generate usage info") {
    Wadl2Dot.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Dot.handleArgs(Array("--help"))
    }
  }

  test ("-h should generate usage info") {
    Wadl2Dot.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Dot.handleArgs(Array("-h"))
    }
  }

  test ("-bad_data should generate usage info") {
    Wadl2Dot.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Dot.handleArgs(Array("-bad_data"))
    }
  }

  test ("Too many params should generate usage info") {
    Wadl2Dot.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Dot.handleArgs(Array("input","output","junk"))
    }
  }

  test ("-d should set removeDups") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.removeDups.value == None)
    Wadl2Dot.handleArgs(Array("-d"))
    assert (Wadl2Dot.removeDups.value.get == true)
  }

  test ("-r should set rax-roles") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.raxRoles.value == None)
    Wadl2Dot.handleArgs(Array("-r"))
    assert (Wadl2Dot.raxRoles.value.get == true)
  }

  test ("--remove-dups should set removeDups") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.removeDups.value == None)
    Wadl2Dot.handleArgs(Array("--remove-dups"))
    assert (Wadl2Dot.removeDups.value.get == true)
  }

  test ("-e should show errors") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.showErrors.value == None)
    Wadl2Dot.handleArgs(Array("-e"))
    assert (Wadl2Dot.showErrors.value.get == true)
  }

  test ("--show-errors should set validate") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.showErrors.value == None)
    Wadl2Dot.handleArgs(Array("--show-errors"))
    assert (Wadl2Dot.showErrors.value.get == true)
  }

  test ("-n should enable nfaMode") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.nfaMode.value == None)
    Wadl2Dot.handleArgs(Array("-n"))
    assert (Wadl2Dot.nfaMode.value.get == true)
  }

  test ("--nfa-mode should enable nfaMode") {
    Wadl2Dot.parser.reset()
    assert (Wadl2Dot.nfaMode.value == None)
    Wadl2Dot.handleArgs(Array("--nfa-mode"))
    assert (Wadl2Dot.nfaMode.value.get == true)
  }

  test ("no params should set source and result with input/output stream"){
    Wadl2Dot.parser.reset()
    Wadl2Dot.handleArgs(Array())
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getInputStream() != null)
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getSystemId() == null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getOutputStream() != null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getSystemId() == null)
  }

  test ("one params should set source to systemid and result to stream"){
    Wadl2Dot.parser.reset()
    Wadl2Dot.handleArgs(Array("test.wadl"))
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getInputStream() == null)
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getSystemId() != null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getOutputStream() != null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getSystemId() == null)
  }

  test ("two params should set source and result to systemid"){
    Wadl2Dot.parser.reset()
    Wadl2Dot.handleArgs(Array("test.wadl", "out.xml"))
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getInputStream() == null)
    assert (Wadl2Dot.getSource.asInstanceOf[StreamSource].getSystemId() != null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getOutputStream() == null)
    assert (Wadl2Dot.getResult.asInstanceOf[StreamResult].getSystemId() != null)
  }

}
