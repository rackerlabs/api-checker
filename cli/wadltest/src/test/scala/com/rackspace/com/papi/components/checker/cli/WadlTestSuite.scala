/** *
  * Copyright 2014 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker.cli

import java.io.{ByteArrayOutputStream, PrintStream}

import javax.xml.transform.stream._
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WadlTestSuite extends FunSuite with Matchers {

  def withOutput(test: (ByteArrayOutputStream, ByteArrayOutputStream) => Unit) = {
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


  test("-bad_data should generate usage info") {
    WadlTest.handleArgs(Array("-bad_data")) shouldBe None
  }

  test("Too many params should generate usage info (2)") {
    WadlTest.handleArgs(Array("input", "junk")) shouldBe None
  }

  test("Too many params should generate usage info (3) ") {
    WadlTest.handleArgs(Array("input", "junk", "junk2")) shouldBe None
  }

  test("-d should set removeDups") {
    val option = WadlTest.handleArgs(Array("-d", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.removeDups shouldBe true
    }
  }

  test("--remove-dups should set removeDups") {
    val option = WadlTest.handleArgs(Array("--remove-dups", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.removeDups shouldBe true
    }
  }

  test("-S with bad parameter") {
    withOutput((outStream, errStream) => {
      WadlTest.main(Array("-S", "foo", "input"))
      assert(outStream.toString().contains("Unrecognized XSL engine"))
      assert(outStream.toString().contains("foo"))
      assert(outStream.toString().contains("Xerces, SaxonEE"))
      assert(errStream.toString().isEmpty())
    })
  }

  test("-S with Xerces parameter") {
    val option = WadlTest.handleArgs(Array("-S", "Xerces", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.xsdEngine shouldBe "Xerces"
    }
  }

  test("-S with SaxonEE parameter") {
    val option = WadlTest.handleArgs(Array("-S", "SaxonEE", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.xsdEngine shouldBe "SaxonEE"
    }
  }

  test("-D should set validate") {
    val option = WadlTest.handleArgs(Array("-D", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.validateChecker shouldBe false
    }
  }

  test("--dont-validate should set validate") {
    val option = WadlTest.handleArgs(Array("--dont-validate", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.validateChecker shouldBe false
    }
  }

  test("-r should set raxRoles") {
    val option = WadlTest.handleArgs(Array("-r", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.enableRaxRolesExtension shouldBe true
    }
  }

  test("-u should set raxAuthenticatedBy") {
    val option = WadlTest.handleArgs(Array("-u", "test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, config, _, _, _)) =>
        config.enableAuthenticatedByExtension shouldBe true
    }
  }

  test("no params should set source with input stream") {
    val option = WadlTest.handleArgs(Array())
    option shouldBe defined
    option match {
      case Some((_, _, _, _, _, _, _, input)) =>
        WadlTest.getSource(input).asInstanceOf[StreamSource].getInputStream should not be (null)
        WadlTest.getSource(input).asInstanceOf[StreamSource].getSystemId should be(null)
    }
  }

  test("one params should set source to systemid and result to stream") {
    val option = WadlTest.handleArgs(Array("test.wadl"))
    option shouldBe defined
    option match {
      case Some((_, _, _, _, _, _, _, input)) =>
        WadlTest.getSource(input).asInstanceOf[StreamSource].getInputStream should be(null)
        WadlTest.getSource(input).asInstanceOf[StreamSource].getSystemId should not be (null)
    }
  }

}
