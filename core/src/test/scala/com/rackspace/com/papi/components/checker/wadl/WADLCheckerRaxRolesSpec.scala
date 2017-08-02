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
package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.TestConfig
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

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
      <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
          <resource path="/c">
            <param name="X-Auth-Token" style="header" required="true" repeating="true"/>
            <method name="GET" rax:roles="a:admin"/>
            <method name="POST" rax:roles="a:observer a:admin"/>
          </resource>
        </resources>
      </application>

    val raxRolesWADLRef =
      <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method href="#putOnA" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method href="#postOnB"/>
              <method href="#putOnB"/>
              <method href="#deleteOnB" rax:roles="b:observer b:admin"/>
              <method href="#getOnB"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method href="#postOnB"/>
              <method href="#putOnB"/>
            </resource>
          </resource>
          <resource path="/c">
            <param name="X-Auth-Token" style="header" required="true" repeating="true"/>
            <method href="#getOnC"/>
            <method href="#postOnC"/>
          </resource>
        </resources>
        <method id="putOnA" name="PUT"/>
        <method id="postOnB" name="POST"/>
        <method id="putOnB" name="PUT" rax:roles="b:observer"/>
        <method id="deleteOnB" name="DELETE" rax:roles="b:foo"/>
        <method id="getOnB" name="GET" rax:roles="#all"/>
        <method id="getOnC" name="GET" rax:roles="a:admin"/>
        <method id="postOnC" name="POST" rax:roles="a:observer a:admin"/>
      </application>

    val raxRolesDisabled = {
      val tf = TestConfig()
      tf.removeDups = false
      tf.checkHeaders = true
      tf
    }
    val raxRolesEnabled  = {
      val tf = TestConfig()
      tf.removeDups = false
      tf.enableRaxRolesExtension = true
      tf
    }
    val raxRolesEnabledRemoveDups  = {
      val tf = TestConfig()
      tf.removeDups = true
      tf.enableRaxRolesExtension = true
      tf
    }
    val raxRolesMaskEnabled = {
      val tf = TestConfig()
      tf.removeDups = false
      tf.enableRaxRolesExtension = true
      tf.maskRaxRoles403 = true
      tf
    }
    val raxRolesMaskEnabledRemoveDups = {
      val tf = TestConfig()
      tf.removeDups = true
      tf.enableRaxRolesExtension = true
      tf.maskRaxRoles403 = true
      tf
    }

    val wadls = Map[String, NodeSeq]("A WADL with rax:roles but no references" -> raxRolesWADLNoRef,
                                     "A WADL with rax:roles and method references" -> raxRolesWADLRef)

    for ((desc, inWADL) <- wadls) {
      scenario ("The WADL contains rax:roles, but rax:roles checks are disabled with "+desc) {
        Given (desc)
        When ("The wadl is translated with rax:roles disabled")
        val config = raxRolesDisabled
        val checker = builder.build (inWADL, config)
        Then("Header checks should not be set")
        assert (checker, "exactly-one(chk:checker/chk:grammar[@type='W3C_XML']/xsd:schema/xsd:simpleType[@name='yesno'])")
        assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY']) = 0")
        assert (checker, """every $s in chk:checker/chk:step[@type='URLXSD'] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@match, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@match, $s)) = 'yesno'""")
        assert (checker, """every $s in chk:checker/chk:step[@type='URL_FAIL' and @notTypes] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@notTypes, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@notTypes, $s)) = 'yesno'""")
        And("URLs and Methods should be validated as always")
        assert (checker, Start, URL("a"), Method("PUT"), Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"), Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"), Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("POST"), Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"), Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"), Accept)
      }

      scenario ("The WADL contains rax:roles and rax:roles checks are enabled without RemoveDups with "+desc) {
        Given (desc)
        When ("The wadl is translated with rax:roles disabled")
        val config = raxRolesEnabled
        val checker = builder.build (inWADL, config)
        Then("Header checks should be set for each method")
        assert (checker, "exactly-one(chk:checker/chk:grammar[@type='W3C_XML']/xsd:schema/xsd:simpleType[@name='yesno'])")
        assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @code='403']) = 17")
        assert (checker, "count(chk:checker/chk:step[@type='METHOD_FAIL']) = 5")
        assert (checker, "count(chk:checker/chk:step[@type='URL_FAIL']) = 3")
        assert (checker, """every $s in chk:checker/chk:step[@type='URLXSD'] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@match, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@match, $s)) = 'yesno'""")
        assert (checker, """every $s in chk:checker/chk:step[@type='URL_FAIL' and @notTypes] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@notTypes, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@notTypes, $s)) = 'yesno'""")
        And("URLs and Methods should be validated as well as headers")
        assert (checker, Start, URL("a"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), Method("PUT"),
                HeaderAny("X-ROLES","a:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"),
                HeaderAny("X-ROLES","b:creator","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
                HeaderAny("X-ROLES","b:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
          Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
                HeaderAny("X-ROLES","b:creator","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
                HeaderAny("X-ROLES","b:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
                HeaderAny("X-ROLES","b:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
                HeaderAny("X-ROLES","b:creator","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("POST"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                HeaderAny("X-ROLES","b:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), Method("PUT"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), ContentFail)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("POST"), ContentFail)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"), ContentFail)
        assert (checker, Start, MethodFail)
        assert (checker, Start, URL("c"), ContentFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), URLFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("GET|POST"))
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"), HeaderAny("X-ROLES","a:admin"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"), ContentFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"), HeaderAny("X-ROLES","a:admin"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"), HeaderAny("X-ROLES","a:observer"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"), ContentFail)
      }

      scenario ("The WADL contains rax:roles and rax:roles checks are enabled without RemoveDups with 403s masked and with "+desc) {
        Given (desc)
        When ("The wadl is translated with rax:roles disabled")
        val config = raxRolesMaskEnabled
        val checker = builder.build (inWADL, config)
        Then("Header checks should be set at the begining of each path")
        assert (checker, "exactly-one(chk:checker/chk:grammar[@type='W3C_XML']/xsd:schema/xsd:simpleType[@name='yesno'])")
        assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY']) = 5")
        assert (checker, "count(chk:checker/chk:step[@type='METHOD_FAIL']) = 30")
        assert (checker, "count(chk:checker/chk:step[@type='URL_FAIL']) = 30")
        assert (checker, """every $s in chk:checker/chk:step[@type='URLXSD'] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@match, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@match, $s)) = 'yesno'""")
        assert (checker, """every $s in chk:checker/chk:step[@type='URL_FAIL' and @notTypes] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@notTypes, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@notTypes, $s)) = 'yesno'""")
        And("URLs and Methods should be validated as well as headers")
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), URLFail("b"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), URLFailT("tst:yesno"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URLFail("a|c"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), MethodFail("DELETE|GET|POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), MethodFail("POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URLFail("a|c"))
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), MethodFail("DELETE|GET|POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLFail("b"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLFailT("tst:yesno"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), MethodFail("DELETE|GET|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLXSD("tst:yesno"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), MethodFail("DELETE|GET"))
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), MethodFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), ContentFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("GET|POST"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("POST"))
        assert (checker, Start, URLFail("a"))
        assert (checker, Start, URL("a"), URLFail("b"))
        assert (checker, Start, URL("a"), MethodFail)
        assert (checker, Start, URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), MethodFail("GET"))
        assert (checker, Start, URL("a"), URL("b"), URLFail)
        assert (checker, Start, MethodFail)
      }

      scenario ("The WADL contains rax:roles and rax:roles checks are enabled with RemoveDups with "+desc) {
        Given (desc)
        When ("The wadl is translated with rax:roles disabled")
        val config = raxRolesEnabledRemoveDups
        val checker = builder.build (inWADL, config)
        Then("Header checks should be set for each method")
        assert (checker, "exactly-one(chk:checker/chk:grammar[@type='W3C_XML']/xsd:schema/xsd:simpleType[@name='yesno'])")
        assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @code='403']) = 6")
        assert (checker, "count(chk:checker/chk:step[@type='METHOD_FAIL']) = 5")
        assert (checker, "count(chk:checker/chk:step[@type='URL_FAIL']) = 3")
        assert (checker, """every $s in chk:checker/chk:step[@type='URLXSD'] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@match, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@match, $s)) = 'yesno'""")
        assert (checker, """every $s in chk:checker/chk:step[@type='URL_FAIL' and @notTypes] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@notTypes, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@notTypes, $s)) = 'yesno'""")
        And("URLs and Methods should be validated as well as headers")
        assert (checker, Start, URL("a"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin|a:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"),
                HeaderAny("X-ROLES","a:admin|b:creator","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin|b:creator|b:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"),
                HeaderAny("X-ROLES","a:admin|b:creator|b:observer|b:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("POST"),
                HeaderAny("X-ROLES","a:admin","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                HeaderAny("X-ROLES","a:admin|b:observer","You are forbidden to perform the operation", 403),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("a"), Method("PUT"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("POST"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ContentFail)
        assert (checker, Start, URL("a"), URL("b"), Method("DELETE"), ContentFail)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("POST"), ContentFail)
        assert (checker, Start, URL("a"), URLXSD("tst:yesno"), Method("PUT"), ContentFail)
        assert (checker, Start, MethodFail)
        assert (checker, Start, URL("c"), ContentFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), URLFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("GET|POST"))
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"), HeaderAny("X-ROLES","a:admin"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"), ContentFail)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"), HeaderAny("X-ROLES","a:admin|a:observer"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"), ContentFail)
      }

      scenario ("The WADL contains rax:roles and rax:roles checks are enabled with RemoveDups with 403s masked and with "+desc) {
        Given (desc)
        When ("The wadl is translated with rax:roles disabled")
        val config = raxRolesMaskEnabledRemoveDups
        val checker = builder.build (inWADL, config)
        Then("Header checks should be set for each method")
        assert (checker, "exactly-one(chk:checker/chk:grammar[@type='W3C_XML']/xsd:schema/xsd:simpleType[@name='yesno'])")
        assert (checker, "count(chk:checker/chk:step[@type='HEADER_ANY']) = 5")
        assert (checker, "count(chk:checker/chk:step[@type='METHOD_FAIL']) = 9")
        assert (checker, "count(chk:checker/chk:step[@type='URL_FAIL']) = 5")
        assert (checker, """every $s in chk:checker/chk:step[@type='URLXSD'] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@match, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@match, $s)) = 'yesno'""")
        assert (checker, """every $s in chk:checker/chk:step[@type='URL_FAIL' and @notTypes] satisfies
                             namespace-uri-from-QName(resolve-QName($s/@notTypes, $s)) = 'test://schema/a' and
                             local-name-from-QName(resolve-QName($s/@notTypes, $s)) = 'yesno'""")
        And("URLs and Methods should be validated as well as headers")
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), URLFail("b"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"),URL("a"), URLFailT("tst:yesno"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URLFail("a|c"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), MethodFail("DELETE|GET|POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("a"), URLXSD("tst:yesno"), MethodFail("POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"),URL("a"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URLFail("a|c"))
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), MethodFail("DELETE|GET|POST|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:creator"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLFail("b"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLFailT("tst:yesno"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), MethodFail("DELETE|GET|PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLXSD("tst:yesno"), Method("PUT"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:observer"), URL("a"), URLXSD("tst:yesno"), MethodFail("PUT"))
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), Method("DELETE"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'b:admin', 'b:creator', 'b:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), MethodFail("DELETE|GET"))
        assert (checker, Start, HeaderAny("X-ROLES","b:admin"), URL("a"), URL("b"), URLFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), MethodFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), ContentFail)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:admin"), URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("GET|POST"))
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URL("c"), Header("X-Auth-Token", "(?s).*"), Method("POST"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('a:admin', 'a:observer')) then $h else ()"),
                Accept)
        assert (checker, Start, HeaderAny("X-ROLES","a:observer"), URL("c"), Header("X-Auth-Token", "(?s).*"), MethodFail("POST"))
        assert (checker, Start, URLFail("a"))
        assert (checker, Start, URL("a"), URLFail("b"))
        assert (checker, Start, URL("a"), MethodFail)
        assert (checker, Start, URL("a"), URL("b"), Method("GET"),
                RaxCaptureHeader("X-RELEVANT-ROLES", "req:headers('X-ROLES', true())"),
                Accept)
        assert (checker, Start, URL("a"), URL("b"), MethodFail("GET"))
        assert (checker, Start, URL("a"), URL("b"), URLFail)
        assert (checker, Start, MethodFail)
      }
    }
  }
}
