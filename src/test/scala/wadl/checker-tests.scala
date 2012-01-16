package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._


@RunWith(classOf[JUnitRunner])
class WADLCheckerSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    scenario("The WADL does not contain any resources") {
      given("a WADL with no resources")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com"/>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
      then("The checker should contain a single start node")
      assert (checker, "count(//chk:step[@type='START']) = 1")
      and("The path from start should only contain METHOD_FAIL and URL_FAIL and the start node itself")
      val path = allStepsFromStep(checker, (stepsWithType(checker,"START")(0) \ "@id").text)
      assert (path, "count(//chk:step) = 3")
      assert (path, "/chk:checker/chk:step[@type='START']")
      assert (path, "/chk:checker/chk:step[@type='METHOD_FAIL']")
      assert (path, "/chk:checker/chk:step[@type='URL_FAIL']")
    }
  }
}
