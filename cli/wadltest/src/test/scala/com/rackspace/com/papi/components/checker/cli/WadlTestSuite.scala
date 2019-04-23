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
import org.scalatestplus.junit.JUnitRunner

import java.io.ByteArrayOutputStream
import java.io.PrintStream

@RunWith(classOf[JUnitRunner])
class WadlTestSuite extends FunSuite {

  def withOutput(test : (ByteArrayOutputStream, ByteArrayOutputStream) => Unit) = {
    val errStream = new ByteArrayOutputStream()
    val printErrStream = new PrintStream(errStream)

    val outStream = new ByteArrayOutputStream()
    val printOutStream = new PrintStream(outStream)

    this.synchronized {
      val oldErr = System.err
      val oldOut = System.out

      try {
        System.setErr(printErrStream)
        System.setOut(printOutStream)

        test(outStream, errStream)

      } finally {
        System.setErr(oldErr)
        System.setOut(oldOut)
      }
    }
  }


  test ("--help should generate usage info") {
    WadlTest.parser.reset()
    intercept[ArgotUsageException] {
      WadlTest.handleArgs(Array("--help"))
    }
  }

  test ("-h should generate usage info") {
    WadlTest.parser.reset()
    intercept[ArgotUsageException] {
      WadlTest.handleArgs(Array("-h"))
    }
  }

  test ("-bad_data should generate usage info") {
    WadlTest.parser.reset()
    intercept[ArgotUsageException] {
      WadlTest.handleArgs(Array("-bad_data"))
    }
  }

  test ("Too many params should generate usage info (2)") {
    WadlTest.parser.reset()
    intercept[ArgotUsageException] {
      WadlTest.handleArgs(Array("input", "junk"))
    }
  }

  test ("Too many params should generate usage info (3) ") {
    WadlTest.parser.reset()
    intercept[ArgotUsageException] {
      WadlTest.handleArgs(Array("input","junk", "junk2"))
    }
  }

  test ("-d should set removeDups") {
    WadlTest.parser.reset()
    assert (WadlTest.removeDups.value.isEmpty)
    WadlTest.handleArgs(Array("-d"))
    assert (WadlTest.removeDups.value.get)
  }

  test ("--remove-dups should set removeDups") {
    WadlTest.parser.reset()
    assert (WadlTest.removeDups.value.isEmpty)
    WadlTest.handleArgs(Array("--remove-dups"))
    assert (WadlTest.removeDups.value.get)
  }

  test ("-S with bad parameter") {
    WadlTest.parser.reset()
    withOutput ( (outStream, errStream) => {
      WadlTest.main(Array("-S", "foo", "input"))
      assert(outStream.toString().contains("Unrecognized XSL engine"))
      assert(outStream.toString().contains("foo"))
      assert(outStream.toString().contains("Xerces, SaxonEE"))
      assert(errStream.toString().isEmpty())
    })
  }

  test ("-S with Xerces parameter") {
    WadlTest.parser.reset()
    assert (WadlTest.xsdEngine.value.isEmpty)
    WadlTest.handleArgs(Array("-S","Xerces"))
    assert (WadlTest.xsdEngine.value.get == "Xerces")
  }

  test ("-S with SaxonEE parameter") {
    WadlTest.parser.reset()
    assert (WadlTest.xsdEngine.value.isEmpty)
    WadlTest.handleArgs(Array("-S","SaxonEE"))
    assert (WadlTest.xsdEngine.value.get == "SaxonEE")
  }

  test ("-D should set validate") {
    WadlTest.parser.reset()
    assert (WadlTest.dontValidate.value.isEmpty)
    WadlTest.handleArgs(Array("-D"))
    assert (WadlTest.dontValidate.value.get)
  }

  test ("--dont-validate should set validate") {
    WadlTest.parser.reset()
    assert (WadlTest.dontValidate.value.isEmpty)
    WadlTest.handleArgs(Array("--dont-validate"))
    assert (WadlTest.dontValidate.value.get)
  }

  test ("-r should set raxRoles") {
    WadlTest.parser.reset()
    assert (WadlTest.raxRoles.value.isEmpty)
    WadlTest.handleArgs(Array("-r"))
    assert (WadlTest.raxRoles.value.get)
  }

  test ("-u should set raxAuthenticatedBy") {
    WadlTest.parser.reset()
    assert (WadlTest.authenticatedBy.value.isEmpty)
    WadlTest.handleArgs(Array("-u"))
    assert (WadlTest.authenticatedBy.value.get)
  }

  test ("no params should set source with input stream"){
    WadlTest.parser.reset()
    WadlTest.handleArgs(Array())
    assert (WadlTest.getSource.asInstanceOf[StreamSource].getInputStream != null)
    assert (WadlTest.getSource.asInstanceOf[StreamSource].getSystemId == null)
  }

  test ("one params should set source to systemid and result to stream"){
    WadlTest.parser.reset()
    WadlTest.handleArgs(Array("test.wadl"))
    assert (WadlTest.getSource.asInstanceOf[StreamSource].getInputStream == null)
    assert (WadlTest.getSource.asInstanceOf[StreamSource].getSystemId != null)
  }

}
