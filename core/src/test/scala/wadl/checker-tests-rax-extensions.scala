package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

@RunWith(classOf[JUnitRunner])
class WADLCheckerRaxRolesSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("xsd", "http://www.w3.org/2001/XMLSchema")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references the rax:roles extenson into a ")
    info ("a description of a machine that can handle the correct header validations in checker format")
    info ("so that an API validator can process the checker format to validate the roles")

    val raxRolesWADLNoRef =
      <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
        </resources>
      </application>

    val raxRolesWADLRef =
            <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method href="#putOnA" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method href="#postOnB"/>
              <method href="#putOnB"/>
              <method href="#deleteOnB" rax:roles="b:observer b:admin"/>
            </resource>
          </resource>
        </resources>
        <method id="putOnA" name="PUT"/>
        <method id="postOnB" name="POST"/>
        <method id="putOnB" name="PUT" rax:roles="b:observer"/>
        <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
      </application>

    val raxRolesDisabled = TestConfig()
    val raxRolesEnabled  = {
      val tf = TestConfig()
      tf.enableRaxRolesExtension = true
      tf
    }

    scenario ("The WADL contains rax:roles, but rax:roles checks are disabled") {
      Given ("a WADL that contains rax:roles attributes")
      val inWADL = raxRolesWADLNoRef
      When ("The wadl is translated with rax:roles disabled")
      val config = raxRolesDisabled
      val checker = builder.build (inWADL, config)
      Then("Header checks should not be set")
      assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY']) = 0")
      And("URLs and Methods should be validated as always")
      assert (checker, Start, URL("a"), Method("PUT"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), Accept)
    }

    scenario ("The WADL contains rax:roles and method references, but rax:roles checks are disabled") {
      Given ("a WADL that contains rax:roles attributes")
      val inWADL = raxRolesWADLRef
      When ("The wadl is translated with rax:roles disabled")
      val config = raxRolesDisabled
      val checker = builder.build (inWADL, config)
      Then("Header checks should not be set")
      assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY']) = 0")
      And("URLs and Methods should be validated as always")
      assert (checker, Start, URL("a"), Method("PUT"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), Accept)
    }

    scenario ("The WADL contains rax:roles and rax:roles checks are enabled") {
      Given ("a WADL that contains rax:roles attributes")
      val inWADL = raxRolesWADLNoRef
      When ("The wadl is translated with rax:roles disabled")
      val config = raxRolesEnabled
      val checker = builder.build (inWADL, config)
      Then("Header checks should be set for each method")
      assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @code='403']) = 4")
      And("URLs and Methods should be validated as well as headers")
      assert (checker, Start, URL("a"), Method("PUT"),
              HeaderAny("X-ROLES","a:admin|a:observer","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"),
              HeaderAny("X-ROLES","a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
              HeaderAny("X-ROLES","b:observer|a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
              HeaderAny("X-ROLES","b:admin|b:observer|a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), Method("PUT"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), ContentFail)
    }

    scenario ("The WADL contains rax:roles and method references and rax:roles checks are enabled") {
      Given ("a WADL that contains rax:roles attributes")
      val inWADL = raxRolesWADLRef
      When ("The wadl is translated with rax:roles disabled")
      val config = raxRolesEnabled
      val checker = builder.build (inWADL, config)
      Then("Header checks should be set for each method")
      assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @code='403']) = 4")
      And("URLs and Methods should be validated as well as headers")
      assert (checker, Start, URL("a"), Method("PUT"),
              HeaderAny("X-ROLES","a:admin|a:observer","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"),
              HeaderAny("X-ROLES","a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
              HeaderAny("X-ROLES","b:observer|a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
              HeaderAny("X-ROLES","b:admin|b:observer|a:admin|b:creator","You are forbidden to perform the operation", 403),
              Accept)
      assert (checker, Start, URL("a"), Method("PUT"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("POST"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ContentFail)
      assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), ContentFail)
    }
  }
}
