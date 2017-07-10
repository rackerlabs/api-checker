package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.{LogAssertions, TestConfig}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerCaptureHeaderStepSpec extends BaseCheckerSpec with LogAssertions {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("xsl","http://www.w3.org/1999/XSL/Transform")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("tst","http://www.rackspace.com/repose/wadl/checker/step/test")


  //
  //  Configs...
  //

  val captureHeaderDisabled = {
    val tc = TestConfig()
    tc.enableCaptureHeaderExtension = false
    tc.enableAssertExtension = false
    tc.removeDups = false
    tc
  }

  val captureHeaderEnabled = {
    val tc = TestConfig()
    tc.enableCaptureHeaderExtension = true
    tc.enableAssertExtension = false
    tc.removeDups = false
    tc
  }

  val captureHeaderAssertEnabled = {
    val tc = TestConfig()
    tc.enableCaptureHeaderExtension = true
    tc.enableAssertExtension = true
    tc.removeDups = false
    tc
  }


  val captureHeaderEnabledRemoveDups = {
    val tc = TestConfig()
    tc.enableCaptureHeaderExtension = true
    tc.enableAssertExtension = false
    tc.removeDups = true
    tc
  }

  //
  // WADLs...
  //
  val captureHeaderAtMethodLevel =
            <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                 <request>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                    <rax:assert test="req:header('X-AUTH') = 'foo!'"/>
                  </request>
               </method>
            </resource>
           </resources>
          </application>

  val captureHeaderAtMethodMultiRep =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
            </resource>
           </resources>
          </application>

  val captureHeaderAtResourceLevel =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:captureHeader name="X-CONTAINS-A" path="contains($req:uri,'/a')"/>
            </resource>
           </resources>
          </application>

  val captureHeaderAtResourceLevelApplyChildrenFalse =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
                </resource>
                <rax:captureHeader name="X-CONTAINS-A" path="contains($req:uri,'/a')" applyToChildren="false"/>
            </resource>
           </resources>
          </application>

  val captureHeaderAtResourceLevelApplyChildrenTrue =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json"/>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:captureHeader name="X-CONTAINS-A" path="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
           </resources>
          </application>


  val captureHeaderAtResourceLevelApplyChildrenTrueRepAsserts =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:test="http://test/foo"
                     >
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml">
                      <rax:captureHeader name="X-FOO" path="/test:test/@foo"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                      <rax:captureHeader name="X-ACCEPT-DUP" path="req:headers('Accept', true())"/>
                    </representation>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json">
                            <rax:captureHeader name="X-ACCEPT-JSON" path="'application/json' = req:headers('Accept', true())"/>
                       </representation>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:captureHeader name="X-CONTAINS-A" path="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
           </resources>
          </application>

  val captureHeaderAtResourceLevelApplyChildrenTrueRepResourcesAsserts =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api"
                     xmlns:test="http://test/foo">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml">
                      <rax:captureHeader name="X-FOO" path="/test:test/@foo == 'bar'"/>
                    </representation>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml">
                      <rax:captureHeader name="X-ACCEPT-DUP" path="req:headers('Accept', true())"/>
                    </representation>
                    <rax:captureHeader name="X-AUTH-DUP" path="req:header('X-AUTH')"/>
                  </request>
               </method>
               <method name="PUT">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                    <representation mediaType="text/x-yaml"/>
                  </request>
               </method>
               <resource path="c">
                   <method name="GET">
                   </method>
                   <method name="POST">
                    <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                      <representation mediaType="text/x-yaml"/>
                    </request>
                   </method>
                   <method name="PUT">
                     <request>
                       <representation mediaType="application/xml"/>
                       <representation mediaType="application/json">
                            <rax:captureHeader name="X-ACCEPT-JSON" path="'application/json' = req:headers('Accept', true())"/>
                       </representation>
                       <representation mediaType="text/x-yaml"/>
                     </request>
                   </method>
               </resource>
               <rax:captureHeader name="X-CONTAINS-A" path="contains($req:uri,'/a')" applyToChildren="true"/>
            </resource>
            <rax:captureHeader name="X-URI-NOT-EMPTY" path="not(empty($req:uri))"/>
           </resources>
          </application>

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references rax:assert extensions into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    scenario ("The WADL contains a misplaced rax:captureHeader") {
      Given("A WADL with a misplaced rax:assert")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                  </request>
                  <!-- Notice captureHeader at method level, but not request level -->
                  <rax:captureHeader name="X-AUTH-DUP" test="req:header('X-AUTH')"/>
               </method>
            </resource>
           </resources>
          </application>

      When("the wadl is translated")
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, captureHeaderEnabled)
        Then("No asserts should exist in the checker format")
        assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      }
      And ("There should be a warning in the log that denotes that denotes the bad placement")
      assert(checkerLog, "bad placement for <rax:captureHeader")
      assert(checkerLog, "req:header('X-AUTH')")
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level (rax:captureHeader disabled)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:captureHeader disabled")
      val inWADL = captureHeaderAtMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), Accept)
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level (rax:captureHeader enabled)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following captureHeaderions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 1")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level (rax:captureHeader, rax:assert enabled)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderAssertEnabled)
      Then ("The following captureHeaderions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 1")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), RaxAssert("req:header('X-AUTH') = 'foo!'"),
        RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ContentFail)
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level, XML, JSON, and YAML representations (rax:captureHeader disabled)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:raxCaptureHeader disabled")
      val inWADL = captureHeaderAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level, XML, JSON, and YAML representations (rax:captureHeader enabled)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 3")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
    }

    scenario ("The WADL contains a method with a rax:captureHeader at the request level, XML, JSON, and YAML representations (rax:captureHeader enabled, removeDups)") {
      Given("A WADL with a GET operation with a rax:captureHeader at the request level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtMethodMultiRep
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 1")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader disabled)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader disabled")
      val inWADL = captureHeaderAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled)") {
      Given("A WADL with a with a rax:captureHeader at the resource level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 9")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }


    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, removeDups)") {
      Given("A WADL with a with a rax:captureHeader at the resource level, but rax:captureHeader enabled")
      val inWADL = captureHeaderAtResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader disabled, applyChildrenFalse)") {
      Given("A WADL with a rax:captureHeader at the request level, but rax:captureHeader disabled, applyChildrenFalse")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenFalse)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChldernFalse")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 9")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }


    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenFalse, removeDups)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChldernFalse")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader disabled, applyChildrenTrue)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader disabled applyChlidrenTrue")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue)") {
      Given("A WADL with a rax:captureHeader at the resource level applyChildernTrue")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following captureHeaderions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 16")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue, removeDups)") {
      Given("A WADL with a rax:captureHeader at the resource level applyChildernTrue")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following captureHeaderions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 2")

      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader disabled, applyChildrenTrue, captureHeaders at representation)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader disabled applyChlidrenTrue, and captureHeaders at representation")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue, captureHeaders at representation)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChlidrenTrue, and captureHeaders at representation")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 19")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@path,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@path,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-FOO","/test:test/@foo"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-ACCEPT-DUP","req:headers('Accept', true())"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-ACCEPT-JSON","'application/json' = req:headers('Accept', true())"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue, captureHeaders at representation, removeDups)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChlidrenTrue, and captureHeaders at representation")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker, "count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 5")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@path,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@path,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-FOO","/test:test/@foo"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-ACCEPT-DUP","req:headers('Accept', true())"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-ACCEPT-JSON","'application/json' = req:headers('Accept', true())"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader disabled, applyChildrenTrue, captureHeaders at representation and resources level)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader disabled applyChlidrenTrue, and captureHeaders at representation and resources level")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 0")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue, captureHeaders at representation and resources level)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChlidrenTrue, and captureHeaders at representation and resources level")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 32")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@path,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@path,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-FOO","/test:test/@foo == 'bar'"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-ACCEPT-DUP","req:headers('Accept', true())"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-ACCEPT-JSON","'application/json' = req:headers('Accept', true())"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
    }

    scenario ("The WADL contains a resource with a rax:captureHeader at the resource level, XML, JSON, and YAML representations in methods (rax:captureHeader enabled, applyChildrenTrue, captureHeaders at representation and resources level, removeDups)") {
      Given("A WADL with a rax:captureHeader at the resource level, but rax:captureHeader enabled applyChlidrenTrue, and captureHeaders at representation and resources level")
      val inWADL = captureHeaderAtResourceLevelApplyChildrenTrueRepResourcesAsserts
      When("the wadl is translated")
      val checker = builder.build (inWADL, captureHeaderEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='CAPTURE_HEADER']) = 6")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@path,'test:')]) = 'test'")
      assert(checker, "namespace-uri-for-prefix('test', /chk:checker/chk:step[contains(@path,'test:')]) = 'http://test/foo'")
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-FOO","/test:test/@foo == 'bar'"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-ACCEPT-DUP","req:headers('Accept', true())"),
             RaxCaptureHeader("X-AUTH-DUP","req:header('X-AUTH')"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)


      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("GET"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("POST"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)

      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON,
             RaxCaptureHeader("X-ACCEPT-JSON","'application/json' = req:headers('Accept', true())"),
             RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"), RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
      assert(checker, Start,URL("a"), URL("b"), URL("c"), Method("PUT"), ReqType("(text/x\\-yaml)(;.*)?"), RaxCaptureHeader("X-CONTAINS-A","contains($req:uri,'/a')"),
             RaxCaptureHeader("X-URI-NOT-EMPTY","not(empty($req:uri))"), Accept)
    }


  }
}
