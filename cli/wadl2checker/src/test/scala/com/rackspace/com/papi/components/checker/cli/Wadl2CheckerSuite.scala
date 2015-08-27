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

import javax.xml.transform.stream._

import org.clapper.argot.ArgotUsageException
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Wadl2CheckerSuite extends FunSuite {

  test ("--help should generate usage info") {
    Wadl2Checker.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Checker.handleArgs(Array("--help"))
    }
  }

  test ("-h should generate usage info") {
    Wadl2Checker.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Checker.handleArgs(Array("-h"))
    }
  }


  test ("-bad_data should generate usage info") {
    Wadl2Checker.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Checker.handleArgs(Array("-bad_data"))
    }
  }

  test ("Too many params should generate usage info") {
    Wadl2Checker.parser.reset()
    intercept[ArgotUsageException] {
      Wadl2Checker.handleArgs(Array("input","output","junk"))
    }
  }

  test ("-d should set removeDups") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.removeDups.value.isEmpty)
    Wadl2Checker.handleArgs(Array("-d"))
    assert (Wadl2Checker.removeDups.value.get == true)
  }


  test ("-r should set raxRoles") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.raxRoles.value.isEmpty)
    Wadl2Checker.handleArgs(Array("-r"))
    assert (Wadl2Checker.raxRoles.value.get == true)
  }

  test ("--remove-dups should set removeDups") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.removeDups.value.isEmpty)
    Wadl2Checker.handleArgs(Array("--remove-dups"))
    assert (Wadl2Checker.removeDups.value.get == true)
  }

  test ("-v should set validate") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.validate.value.isEmpty)
    Wadl2Checker.handleArgs(Array("-v"))
    assert (Wadl2Checker.validate.value.get == true)
  }

  test ("--validate should set validate") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.validate.value.isEmpty)
    Wadl2Checker.handleArgs(Array("--validate"))
    assert (Wadl2Checker.validate.value.get == true)
  }

  test ("no params should set source and result with input/output stream"){
    Wadl2Checker.parser.reset()
    Wadl2Checker.handleArgs(Array())
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getInputStream() != null)
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getSystemId() == null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getOutputStream() != null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getSystemId() == null)
  }

  test ("one params should set source to systemid and result to stream"){
    Wadl2Checker.parser.reset()
    Wadl2Checker.handleArgs(Array("test.wadl"))
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getInputStream() == null)
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getSystemId() != null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getOutputStream() != null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getSystemId() == null)
  }

  test ("two params should set source and result to systemid"){
    Wadl2Checker.parser.reset()
    Wadl2Checker.handleArgs(Array("test.wadl", "out.xml"))
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getInputStream() == null)
    assert (Wadl2Checker.getSource.asInstanceOf[StreamSource].getSystemId() != null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getOutputStream() == null)
    assert (Wadl2Checker.getResult.asInstanceOf[StreamResult].getSystemId() != null)
  }

}
