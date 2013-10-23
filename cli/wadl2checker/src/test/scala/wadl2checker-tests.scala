package com.rackspace.com.papi.components.checker.cli

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

import org.clapper.argot.ArgotUsageException

import javax.xml.transform.stream._

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
    assert (Wadl2Checker.removeDups.value == None)
    Wadl2Checker.handleArgs(Array("-d"))
    assert (Wadl2Checker.removeDups.value.get == true)
  }


  test ("-r should set raxRoles") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.raxRoles.value == None)
    Wadl2Checker.handleArgs(Array("-r"))
    assert (Wadl2Checker.raxRoles.value.get == true)
  }

  test ("--remove-dups should set removeDups") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.removeDups.value == None)
    Wadl2Checker.handleArgs(Array("--remove-dups"))
    assert (Wadl2Checker.removeDups.value.get == true)
  }

  test ("-v should set validate") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.validate.value == None)
    Wadl2Checker.handleArgs(Array("-v"))
    assert (Wadl2Checker.validate.value.get == true)
  }

  test ("--validate should set validate") {
    Wadl2Checker.parser.reset()
    assert (Wadl2Checker.validate.value == None)
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
