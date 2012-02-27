package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.step._

@RunWith(classOf[JUnitRunner])
class WADLStepSpec extends BaseStepSpec {
  feature ("The WADLStepBuilder can correctly transforma a WADL into a Step") {
    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API")
    info ("so that an API validator can process the machine to validate the API")

    scenario("The WADL does not contain any resources") {
      given("a WADL with no resources")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource/>
           </resources>
        </application>
      when("the wadl is translated")
      val step = builder.build (inWADL).asInstanceOf[Start]
      then("the start step should only be connected with an URLFail and MethodFail steps")
      assert (step.next.length == 2)
      assert (step.next.filter(a => a.isInstanceOf[URLFail]).length == 1)
      assert (step.next.filter(a => a.isInstanceOf[MethodFail]).length == 1)
    }
  }
}
