
package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

//
//  Test for bug fix where preproc was turning on WellForm check in
//  appropriately.
//

@RunWith(classOf[JUnitRunner])
class WADLCheckerPreProcSpec extends BaseCheckerSpec {

    register ("chk","http://www.rackspace.com/repose/wadl/checker")

    feature ("The WADLCheckerBuilder should enable wellform checks only when approprite when prepoc extension is enabled") {
      val testWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
           <resource path="/any">
              <method name="POST">
                 <request>
                    <representation mediaType="*/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/text">
              <method name="POST">
                 <request>
                    <representation mediaType="text/*"/>
                 </request>
              </method>
           </resource>
           <resource path="/v">
              <method name="POST">
                 <request>
                    <representation mediaType="text/plain;charset=UTF8"/>
                 </request>
              </method>
           </resource>
        </resources>
      </application>

      scenario("The testWADL is processed with preporc extension disabled") {
        given("The testWADL with preproc extension disabled")
        when("The WADL is transalted")
        val checker = builder.build(testWADL, TestConfig(false, false, false, false, false, 1, false, false, false))
        then("There should not be any well-form checks")
        assert(checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 0");
      }

      scenario("The testWADL is processed with preporc extension enabled") {
        given("The testWADL with preproc extension disabled")
        when("The WADL is transalted")
        val checker = builder.build(testWADL, TestConfig(false, false, false, false, false, 1, false, false, true))
        then("There should not be any well-form checks")
        assert(checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 0");
      }
    }
}
