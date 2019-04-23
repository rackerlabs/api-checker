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

import com.rackspace.cloud.api.wadl.test.XPathAssertions

import scala.xml._

@RunWith(classOf[JUnitRunner])
class Wadl2CheckerSuite extends FunSuite with XPathAssertions {

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

  //
  //  Register checker namespace.
  //
  register ("chk", "http://www.rackspace.com/repose/wadl/checker")

  test ("--help should generate usage info") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("--help", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(out.toString().isEmpty())
      assert(error.toString().contains("Usage: wadl2checker"))
  })}

  test ("--version should generate usage info") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("--version", "src/test/resources/wadl/sharedXPath.wadl"))
      //
      //  Weird version format: can't actually detect version during
      //  testing so we have null vnull.
      //
      assert(error.toString().contains("null vnull"))
      assert(out.toString().isEmpty())
  })}

  test ("-bad_data should generate usage info") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("--bad_data", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(error.toString().contains("Usage: wadl2checker"))
      assert(error.toString().contains("Unknown option: --bad_data"))
      assert(out.toString().isEmpty())
  })}

  test ("Too many params should generate usage info") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("input", "src/test/resources/wadl/sharedXPath.wadl", "junk"))
      assert(error.toString().contains("Usage: wadl2checker"))
      assert(error.toString().contains("Too many parameters."))
      assert(out.toString().isEmpty())
  })}

  test ("Bad XSD engine should generate an error") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("-S", "foo", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(error.toString().contains("Unrecognized XSL engine"))
      assert(error.toString().contains("foo"))
      assert(error.toString().contains("Xerces, SaxonEE"))
      assert(out.toString().isEmpty())
  })}

  test ("Should generate checker xml to stdout") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("src/test/resources/wadl/sharedXPath.wadl"))

      val outXML = XML.loadString(out.toString())

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(outXML, "/chk:checker")
      assert(outXML, "starts-with(/chk:checker/chk:meta/chk:created-by,'API Checker')")
      assert(outXML, "/chk:checker/chk:meta/chk:config")
      assert(outXML, "/chk:checker/chk:step[@type='START']")
      assert(outXML, "/chk:checker/chk:step[@type='ACCEPT']")
  })}

  test ("Should generate checker xml to stdout (xsd engine)") {
    withOutput ( (out, error) => {
      Wadl2Checker.main(Array("--xsd-engine","Xerces","src/test/resources/wadl/sharedXPath.wadl"))

      val outXML = XML.loadString(out.toString())

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      //
      assert(outXML, "/chk:checker")
      assert(outXML, "starts-with(/chk:checker/chk:meta/chk:created-by,'API Checker')")
      assert(outXML, "/chk:checker/chk:meta/chk:config")
      assert(outXML, "/chk:checker/chk:step[@type='START']")
      assert(outXML, "/chk:checker/chk:step[@type='ACCEPT']")
  })}

  test ("Should generate checker xml to file") {
    withOutput ( (out, error) => {
      val outFile = "target/sharedXPath.checker"

      Wadl2Checker.main(Array("src/test/resources/wadl/sharedXPath.wadl", outFile))

      assert(out.toString().isEmpty())

      val outXML = XML.loadFile(outFile)

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(outXML, "/chk:checker")
      assert(outXML, "starts-with(/chk:checker/chk:meta/chk:created-by,'API Checker')")
      assert(outXML, "/chk:checker/chk:meta/chk:config")
      assert(outXML, "/chk:checker/chk:step[@type='START']")
      assert(outXML, "/chk:checker/chk:step[@type='ACCEPT']")
  })}

  //
  //  Some spot tests to make sure configs are working
  //

  //
  //  Flag, to valid XPath
  //
  val argFlags : Map[String, String] =
    Map("-d"->"/chk:checker/chk:meta/chk:config[@option='removeDups' and @value='true']",
        "-r"->"/chk:checker/chk:meta/chk:config[@option='enableRaxRolesExtension' and @value='true']",
        "-u"->"/chk:checker/chk:meta/chk:config[@option='enableAuthenticatedByExtension' and @value='true']",
        "-H"->"/chk:checker/chk:meta/chk:config[@option='checkHeaders' and @value='true']",
        "--remove-dups"->"/chk:checker/chk:meta/chk:config[@option='removeDups' and @value='true']",
        "--rax-roles"->"/chk:checker/chk:meta/chk:config[@option='enableRaxRolesExtension' and @value='true']",
        "--authenticated-by"->"/chk:checker/chk:meta/chk:config[@option='enableAuthenticatedByExtension' and @value='true']",
        "--header"->"/chk:checker/chk:meta/chk:config[@option='checkHeaders' and @value='true']",
        "" -> """
          /chk:checker/chk:meta/chk:config[@option='checkHeaders' and @value='false'] and
          /chk:checker/chk:meta/chk:config[@option='removeDups' and @value='false'] and
          /chk:checker/chk:meta/chk:config[@option='enableRaxRolesExtension' and @value='false'] and
          /chk:checker/chk:meta/chk:config[@option='enableAuthenticatedByExtension' and @value='false']
        """  // Check defaults
       )

  for ((arg, xpath) <- argFlags) {
      test (s"Should correctly handle argument $arg") {
        withOutput ( (out, error) => {
          if (arg.isEmpty) {
            Wadl2Checker.main(Array("src/test/resources/wadl/sharedXPath.wadl"))
          } else {
            Wadl2Checker.main(Array(arg,"src/test/resources/wadl/sharedXPath.wadl"))
          }

          val outXML = XML.loadString(out.toString())

          assert(outXML, xpath)
      })}
  }
}
