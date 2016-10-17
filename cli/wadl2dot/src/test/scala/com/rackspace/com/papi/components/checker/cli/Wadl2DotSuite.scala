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

import java.io.ByteArrayOutputStream
import java.io.PrintStream

@RunWith(classOf[JUnitRunner])
class Wadl2DotSuite extends FunSuite {

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
    withOutput( (outStream, errStream) =>  {
      Wadl2Dot.main(Array("--help", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(errStream.toString().contains("Usage: wadl2dot"))
      assert(outStream.toString().isEmpty())
  })}

  test ("--version should generate usage info") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("--version", "src/test/resources/wadl/sharedXPath.wadl"))
      //
      //  Weird version format: can't actually detect version during
      //  testing so we have null vnull.
      //
      assert(errStream.toString().contains("null vnull"))
      assert(outStream.toString().isEmpty())
  })}


  test ("-bad_data should generate usage info") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("--bad_data", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(errStream.toString().contains("Usage: wadl2dot"))
      assert(errStream.toString().contains("Unknown option: --bad_data"))
      assert(outStream.toString().isEmpty())
  })}

  test ("Too many params should generate usage info") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("input", "src/test/resources/wadl/sharedXPath.wadl", "junk"))
      assert(errStream.toString().contains("Usage: wadl2dot"))
      assert(errStream.toString().contains("Too many parameters."))
      assert(outStream.toString().isEmpty())
  })}

  test ("Bad XSD engine s hould generate an error") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("-S", "foo", "src/test/resources/wadl/sharedXPath.wadl"))
      assert(errStream.toString().contains("Unrecognized XSL engine"))
      assert(errStream.toString().contains("foo"))
      assert(errStream.toString().contains("Xerces, SaxonEE"))
      assert(outStream.toString().isEmpty())
  })}

  test ("Should generate dot to stdout") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("src/test/resources/wadl/sharedXPath.wadl"))

      //  No err
      assert(errStream.toString().isEmpty())

      val out = outStream.toString()

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(out.contains("digraph Checker"))
      assert(out.contains("rank=source"))
      assert(out.contains("->"))
      assert(!out.contains("SE0"))
  })}

  test ("Should generate dot to stdout (xsdEngine)") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("--xsd-engine","Xerces","src/test/resources/wadl/sharedXPath.wadl"))

      //  No err
      assert(errStream.toString().isEmpty())

      val out = outStream.toString()

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(out.contains("digraph Checker"))
      assert(out.contains("rank=source"))
      assert(out.contains("->"))
      assert(!out.contains("SE0"))
  })}

  test ("Should generate dot to file") {
    withOutput( (outStream, errStream) => {
      val outDot = "target/sharedXPath.dot"

      Wadl2Dot.main(Array("src/test/resources/wadl/sharedXPath.wadl", outDot))

      //  No err
      assert(errStream.toString().isEmpty())
      assert(outStream.toString().isEmpty())

      val source = scala.io.Source.fromFile(outDot)
      val out = try source.mkString finally source.close()

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output

      assert(out.contains("digraph Checker"))
      assert(out.contains("rank=source"))
      assert(out.contains("->"))
      assert(!out.contains("SE0"))
  })}

  //
  //  Some spot tests to make sure that configs are passing through
  //  correctly
  //
  test ("Should generate dot to stdout : test -n arg (nfa mode)") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("-n","src/test/resources/wadl/sharedXPath.wadl"))
      //  No err
      assert(errStream.toString().isEmpty())

      val out = outStream.toString()

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(out.contains("digraph Checker"))
      assert(out.contains("rank=source"))
      assert(out.contains("->"))
      assert(out.contains("SA[label=\"SA\", shape=doublecircle"))
  })}

  test ("Should generate dot to stdout : test -e arg (show error states)") {
    withOutput( (outStream, errStream) => {
      Wadl2Dot.main(Array("-e","src/test/resources/wadl/sharedXPath.wadl"))

      //  No err
      assert(errStream.toString().isEmpty())

      val out = outStream.toString()

      //  Just some basic asserts to make sure we're working, we'd
      //  expect to see all of these things in the output
      assert(out.contains("digraph Checker"))
      assert(out.contains("rank=source"))
      assert(out.contains("->"))
      assert(out.contains("SE0"))
  })}

}
