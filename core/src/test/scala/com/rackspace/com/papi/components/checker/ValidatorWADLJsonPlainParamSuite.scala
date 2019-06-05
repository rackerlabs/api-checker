/***
 *   Copyright 2016 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker


import com.rackspace.com.papi.components.checker.RunAssertionsHandler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.collection.JavaConversions._

import scala.xml.Elem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

@RunWith(classOf[JUnitRunner])
class ValidatorWADLJsonPlainParamSuite extends BaseValidatorSuite {
  ///
  ///  Configs
  ///
  val baseConfig = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithPlainParams = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithCaptureHeaders = {
    val c = TestConfig()
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = true
    c
  }

  val baseWithRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPaths = {
    val c = TestConfig()
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPathsAndRemoveDups = {
    val c = TestConfig()
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }


//  RaxRoles Configs

  val baseWithPlainParamsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }


  val baseWithRemoveDupsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPathsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }


//  RaxRoles Configs Masked

  val baseWithPlainParamsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }


  val baseWithRemoveDupsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPathsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }

  val baseWithJoinXPathsAndRemoveDupsRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = true
    c.checkPlainParams = true
    c.enableCaptureHeaderExtension = false
    c
  }





  val WADL_withJSONParams =  <application xmlns="http://wadl.dev.java.net/2009/02"
                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/json">
                         <!-- Ensure that all types map -->
                         <param id="map"     style="plain" path="$body instance of map(*)" required="true"/>
                         <param id="string"  style="plain" path="$body?firstName instance of xsd:string" required="true"/>
                         <param id="map2"    style="plain" path="$body?stuff instance of map(*)" required="true"/>
                         <param id="bool"    style="plain" path="$body?stuff?thing instance of xsd:boolean" required="true"/>
                         <param id="array"   style="plain" path="$body?stuff?array instance of array(*)" required="true"/>
                         <param id="map3"    style="plain" path="$body?stuff?obj instance of map(*)" required="true"/>
                         <param id="int"     style="plain" path="$body?stuff?array?1 instance of xsd:double" required="true"/>
                         <param id="decimal" style="plain" path="$body?stuff?array2?1 instance of xsd:double" required="true"/>
                         <param id="string2" style="plain" path="$body?stuff?string instance of xsd:string" required="true"/>
                         <param id="string3" style="plain" path="$body?stuff?obj?a instance of xsd:string" required="true"/>
                         <param id="null"    style="plain" path="empty($body?stuff?null)" required="true"/>
                      </representation>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/json">
                          <param id="firstName" style="plain" path="$body?firstName" required="true"/>
                          <param id="lastName"  style="plain" path="$body?lastName"  required="true"/>
                          <param id="age"       style="plain" path="$body?age"       required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
           </resource>
        </resources>
    </application>

  val WADL_withJSONParams2 =  <application xmlns="http://wadl.dev.java.net/2009/02"
                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                              xmlns:rax="http://docs.rackspace.com/api"
                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/json">
                         <!-- Ensure that all types map -->
                         <param id="map"     style="plain" path="$_ instance of map(*)" required="true"/>
                         <param id="string"  style="plain" path="$_?firstName instance of xsd:string" required="true"/>
                         <param id="map2"    style="plain" path="$_?stuff instance of map(*)" required="true"/>
                         <param id="bool"    style="plain" path="$_?stuff?thing instance of xsd:boolean" required="true"/>
                         <param id="array"   style="plain" path="$_?stuff?array instance of array(*)" required="true"/>
                         <param id="map3"    style="plain" path="$_?stuff?obj instance of map(*)" required="true"/>
                         <param id="int"     style="plain" path="$_?stuff?array?1 instance of xsd:double" required="true"/>
                         <param id="decimal" style="plain" path="$_?stuff?array2?1 instance of xsd:double" required="true"/>
                         <param id="string2" style="plain" path="$_?stuff?string instance of xsd:string" required="true"/>
                         <param id="string3" style="plain" path="$_?stuff?obj?a instance of xsd:string" required="true"/>
                         <param id="null"    style="plain" path="empty($_?stuff?null)" required="true"/>
                      </representation>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/json">
                          <param id="firstName" style="plain" path="$_?firstName" required="true"/>
                          <param id="lastName"  style="plain" path="$_?lastName"  required="true"/>
                          <param id="age"       style="plain" path="$_?age"       required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json">
                            <param id="captureJSON" style="plain" path="serialize($_, map {'method' : 'json', 'indent' : false()})" required="true" rax:captureHeader="X-JSON"/>
                      </representation>
                  </request>
               </method>
           </resource>
        </resources>
    </application>

  val WADL_withJSONParamsCodeMessage =  <application xmlns="http://wadl.dev.java.net/2009/02"
                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                              xmlns:rax="http://docs.rackspace.com/api"
                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/json">
                         <!-- Ensure that all types map -->
                         <param id="map"     style="plain" path="$_ instance of map(*)" required="true" rax:code="401" rax:message="Expecting an object"/>
                         <param id="string"  style="plain" path="$_?firstName instance of xsd:string" required="true" rax:code="401"
                                rax:message="Expecting a string for firstName"/>
                         <param id="map2"    style="plain" path="$_?stuff instance of map(*)" required="true" rax:code="401" rax:message="Expecting an object for stuff"/>
                         <param id="bool"    style="plain" path="$_?stuff?thing instance of xsd:boolean" required="true" rax:code="401"
                                rax:message="Expecting a boolean for stuff?thing"/>
                         <param id="array"   style="plain" path="$_?stuff?array instance of array(*)" required="true" rax:code="401"
                                rax:message="Expecting an array for stuff?array "/>
                         <param id="map3"    style="plain" path="$_?stuff?obj instance of map(*)" required="true" rax:code="401"
                                rax:message="Expecting an object for stuff?obj "/>
                         <param id="int"     style="plain" path="$_?stuff?array?1 instance of xsd:double" required="true" rax:code="401"
                                rax:message="Expecting a double for stuff?array?1 "/>
                         <param id="decimal" style="plain" path="$_?stuff?array2?1 instance of xsd:double" required="true" rax:code="401"
                                rax:message="Expecting a double for stuff?array2?1"/>
                         <param id="string2" style="plain" path="$_?stuff?string instance of xsd:string" required="true" rax:code="401"
                                rax:message="Expecting a string for stuff?string"/>
                         <param id="string3" style="plain" path="$_?stuff?obj?a instance of xsd:string" required="true" rax:code="401"
                                rax:message="Expecting a string for stuff?obj?a"/>
                         <param id="null"    style="plain" path="empty($_?stuff?null)" required="true" rax:code="401"
                                rax:message="Expecting null for stuff?null "/>
                      </representation>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/json">
                          <param id="firstName" style="plain" path="$_?firstName" required="true" rax:code="401"
                                rax:message="Expecting a firstName"/>
                          <param id="lastName"  style="plain" path="$_?lastName"  required="true" rax:code="401"
                                rax:message="Expecting a lastName"/>
                          <param id="age"       style="plain" path="$_?age"       required="true" rax:code="401"
                                rax:message="Expecting an age "/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
           </resource>
        </resources>
    </application>


  val WADL_withRAXRolesJSONParams =  <application xmlns="http://wadl.dev.java.net/2009/02"
                              xmlns:rax="http://docs.rackspace.com/api"
                              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                              xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT" rax:roles="admin ab:admin">
                  <request>
                      <representation mediaType="application/json">
                         <!-- Ensure that all types map -->
                         <param id="map"     style="plain" path="$body instance of map(*)" required="true"/>
                         <param id="string"  style="plain" path="$body?firstName instance of xsd:string" required="true"/>
                         <param id="map2"    style="plain" path="$body?stuff instance of map(*)" required="true"/>
                         <param id="bool"    style="plain" path="$body?stuff?thing instance of xsd:boolean" required="true"/>
                         <param id="array"   style="plain" path="$body?stuff?array instance of array(*)" required="true"/>
                         <param id="map3"    style="plain" path="$body?stuff?obj instance of map(*)" required="true"/>
                         <param id="int"     style="plain" path="$body?stuff?array?1 instance of xsd:double" required="true"/>
                         <param id="decimal" style="plain" path="$body?stuff?array2?1 instance of xsd:double" required="true"/>
                         <param id="string2" style="plain" path="$body?stuff?string instance of xsd:string" required="true"/>
                         <param id="string3" style="plain" path="$body?stuff?obj?a instance of xsd:string" required="true"/>
                         <param id="null"    style="plain" path="empty($body?stuff?null)" required="true"/>
                      </representation>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/json">
                          <param id="firstName" style="plain" path="$body?firstName" required="true"/>
                          <param id="lastName"  style="plain" path="$body?lastName"  required="true"/>
                          <param id="age"       style="plain" path="$body?age"       required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST" rax:roles="admin c:admin">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
           </resource>
        </resources>
    </application>


  val jsonPlainWADLs = Map[String, Elem]("WADL with $body json params"->WADL_withJSONParams,
                                         "WADL with $_ json params"->WADL_withJSONParams2,
                                         "WADL with rax:roles -- but disabled"->WADL_withRAXRolesJSONParams)
  val jsonMessageWADLs = Map[String, Elem]("WADL with code and message json params"->WADL_withJSONParamsCodeMessage)
  val jsonPlainConfigs = Map[String, Config]("base config with plain params"->baseWithPlainParams,
                                             "base with capture headers" -> baseWithCaptureHeaders,
                                             "base with remove dups"->baseWithRemoveDups,
                                             "base with join xpath"->baseWithJoinXPaths,
                                             "base with remove dups and join xpaths"->baseWithJoinXPathsAndRemoveDups)
  val jsonPlainDisabledConfigs = Map[String,Config]("base config plain params disabled"->baseConfig)

  val jsonCaptureWADL   = Map[String, Elem]("WADL with capture json"->WADL_withJSONParams2)
  val jsonCaptureConfig = Map[String, Config]("base with capture headers" -> baseWithCaptureHeaders)

  val jsonPlainRaxRolesWADL   = Map[String, Elem]("WADL with json plain params rax:roles enabled"->WADL_withRAXRolesJSONParams)
  val jsonPlainRaxRolesConfigs = Map[String, Config]("base config with plain params rax:roles"->baseWithPlainParamsRaxRoles,
                                             "base with remove dups rax:roles enabled"->baseWithRemoveDupsRaxRoles,
                                             "base with join xpath rax:roles enabled"->baseWithJoinXPathsRaxRoles,
                                             "base with remove dups and join xpaths rax:roles enabled"->baseWithJoinXPathsAndRemoveDupsRaxRoles)
  val jsonPlainRaxRolesMaskConfigs = Map[String, Config]("base config with plain params rax:roles (mask)"->baseWithPlainParamsRaxRolesMask,
                                                         "base with remove dups rax:roles enabled (mask)"->baseWithRemoveDupsRaxRolesMask,
                                                         "base with join xpath rax:roles enabled (mask)"->baseWithJoinXPathsRaxRolesMask,
                                                         "base with remove dups and join xpaths rax:roles enabled (mask)"->baseWithJoinXPathsAndRemoveDupsRaxRolesMask)

  //
  //  Assertions!
  //

  def happyPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a/b should validate with goodJSON on $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /a/b should validate with goodJSON_Schema1 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema1, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /a/b should validate with goodJSON_Schema2 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema2, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /a/b should validate with goodJSON_Schema3 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema3, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /c should validate with goodJSON on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json",goodJSON, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /c should validate with goodJSON_Schema1 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json",goodJSON_Schema1, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /c should validate with goodJSON_Schema2 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /c should validate with goodJSON_Schema3 on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json",goodJSON_Schema3, false, Map[String,List[String]]()), response, chain)
    }

    test (s"A POST on /c should validate with string on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","\"A String\"", false, Map[String,List[String]]()), response, chain)
    }
    test (s"A POST on /c should validate with boolean on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","false", false, Map[String,List[String]]()), response, chain)
    }
    test (s"A POST on /c should validate with number on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","123.4", false, Map[String,List[String]]()), response, chain)
    }
    test (s"A POST on /c should validate with null on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","null", false, Map[String,List[String]]()), response, chain)
    }
    test (s"A POST on /c should validate with object on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","""{"true" : true }""", false, Map[String,List[String]]()), response, chain)
    }
    test (s"A POST on /c should validate with array on $wadlDesc with $configDesc") {
      validator.validate(request("POST", "/c", "application/json","""[true]""", false, Map[String,List[String]]()), response, chain)
    }
  }

def happyPathRaxRolesAssertions(validator : Validator, wadlDesc : String, configDesc : String) {

  test (s"A PUT on /a/b should validate with goodJSON on $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }

  test (s"A PUT on /a/b should validate with goodJSON on $wadlDesc with $configDesc (ab:admin)") {
    validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false, Map[String,List[String]]("X-ROLES"->List("ab:admin"))), response, chain)
  }

  test (s"A POST on /a/b should validate with goodJSON_Schema1 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema1, false, Map[String,List[String]]()), response, chain)
  }

  test (s"A POST on /a/b should validate with goodJSON_Schema2 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema2, false, Map[String,List[String]]()), response, chain)
  }

  test (s"A POST on /a/b should validate with goodJSON_Schema3 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json",goodJSON_Schema3, false, Map[String,List[String]]()), response, chain)
  }

  test (s"A POST on /c should validate with goodJSON on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json",goodJSON, false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }

  test (s"A POST on /c should validate with goodJSON_Schema1 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json",goodJSON_Schema1, false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }

  test (s"A POST on /c should validate with goodJSON_Schema2 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }

  test (s"A POST on /c should validate with goodJSON_Schema3 on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json",goodJSON_Schema3, false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }

  test (s"A POST on /c should validate with string on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","\"A String\"", false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }
  test (s"A POST on /c should validate with boolean on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","false", false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }
  test (s"A POST on /c should validate with number on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","123.4", false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }
  test (s"A POST on /c should validate with null on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","null", false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }
  test (s"A POST on /c should validate with object on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","""{"true" : true }""", false, Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
  }
  test (s"A POST on /c should validate with array on $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/c", "application/json","""[true]""", false, Map[String,List[String]]("X-ROLES"->List("c:admin"))), response, chain)
  }
}


def BadAccessRaxRolesAssertions(validator : Validator, wadlDesc : String, configDesc : String) {

  test (s"A PUT on /a/b should fail with bad access on bad role goodJSON on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false,
                                                  Map[String,List[String]]("X-ROLES"->List("bdmin"))), response, chain),
                       403, List("forbidden"))
  }

  test (s"A POST on /c should fail with bad access on bad role goodJSON_Schema2 on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false,
                                                  Map[String,List[String]]("X-ROLES"->List("user", "nobody"))), response, chain),
                       403, List("forbidden"))
  }

  test (s"A PUT on /a/b should fail with bad access on no role goodJSON on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false,
                                                  Map[String,List[String]]()), response, chain),
                       403, List("forbidden"))
  }

  test (s"A POST on /c should fail with bad access  on no role goodJSON_Schema2 on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false,
                                                  Map[String,List[String]]()), response, chain),
                       403, List("forbidden"))
  }
}


def BadAccessRaxRolesMaskedAssertions(validator : Validator, wadlDesc : String, configDesc : String) {

  test (s"A PUT on /a/b should fail with bad access on bad role goodJSON on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false,
                                                  Map[String,List[String]]("X-ROLES"->List("bdmin"))), response, chain),
                       405, List("Bad method"))
  }

  test (s"A POST on /c should fail with bad access on bad role goodJSON_Schema2 on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false,
                                                  Map[String,List[String]]("X-ROLES"->List("user", "nobody"))), response, chain),
                       404, List("not found"))
  }

  test (s"A PUT on /a/b should fail with bad access on no role goodJSON on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",goodJSON, false,
                                                  Map[String,List[String]]()), response, chain),
                       405, List("Bad method"))
  }

  test (s"A POST on /c should fail with bad access  on no role goodJSON_Schema2 on $wadlDesc with $configDesc") {
    assertResultFailed(validator.validate(request("POST", "/c", "application/json",goodJSON_Schema2, false,
                                                  Map[String,List[String]]()), response, chain),
                       404, List("not found"))
  }
}




def sadPathPassingAssertions (validator : Validator, wadlDesc : String, configDesc : String) {
  test (s"A PUT on /a/b should pass with array body $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json",""" [1, 2, 3] """,
                               false, Map[String,List[String]]()),
                       response, chain)
  }

  test (s"A PUT on /a/b should pass with a bad firstName $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json",""" {  "firstName" : [1, 2, 3] }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }

  test (s"A PUT on /a/b should pass with a bad stuff $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : true
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }

  test (s"A PUT on /a/b should pass with a bad stuff?thing $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : [3, 4]
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?array $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : {"false" : null}
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?obj $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [1,2,3],
                               "obj"   : "not!"
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?array?1 $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [false,2,3],
                               "obj"   : {}
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?array2?1 $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [1,2,3],
                               "array2" : ["hello",2,3],
                               "obj"   : {}
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?string $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [1,2,3],
                               "array2" : [1,2,3],
                               "obj"   : {},
                               "string" : null
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A PUT on /a/b should pass with a bad stuff?obj?a $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [1,2,3],
                               "array2" : [1,2,3],
                               "obj"   : {
                                 "b" : "B",
                               "c" : "C"
                               },
                               "string" : "foo"
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }

  test (s"A PUT on /a/b should pass with a bad stuff?null $wadlDesc with $configDesc") {
    validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                               "stuff" : {
                                 "thing" : true,
                               "array" : [1,2,3],
                               "array2" : [1,2,3],
                               "obj"   : {
                                 "a" : "A",
                               "b" : "B",
                               "c" : "C"
                               },
                               "string" : "foo",
                               "null" : "bar"
                               }
                                                                   }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A POST on /a/b should pass with a missing ?fistName $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json","""{
      "name" : "foo"
    }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A POST on /a/b should pass with a missing ?lastName $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json","""{
      "firstName" : "foo"
    }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A POST on /a/b should pass with a missing ?age $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json","""{
      "firstName" : "foo",
                               "lastName" : "bar"
    }""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }


  test (s"A POST on /a/b should pass with a bad ? $wadlDesc with $configDesc") {
    validator.validate(request("POST", "/a/b", "application/json","""true""",
                               false, Map[String,List[String]]()),
                       response, chain)
  }

}


  def sadPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a/b should fail with array body $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" [1, 2, 3] """,
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","instance of map(*)"))
    }

    test (s"A PUT on /a/b should fail with a bad firstName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" {  "firstName" : [1, 2, 3] }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?firstName"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : true
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?stuff", "instance of map(*)"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?thing $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : [3, 4]
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?thing", "instance of xsd:boolean"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : {"false" : null}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?array", "instance of array(*)"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "obj"   : "not!"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?obj", "instance of map(*)"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [false,2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?array?1", "instance of xsd:double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array2?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : ["hello",2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?array2?1", "instance of xsd:double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?string $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {},
                                                                                              "string" : null
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?string", "instance of xsd:string"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj?a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?obj?a", "instance of xsd:string"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?null $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "a" : "A",
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo",
                                                                                              "null" : "bar"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","stuff?null", "empty"))
    }


    test (s"A POST on /a/b should fail with a missing ?fistName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "name" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?firstName"))
    }


    test (s"A POST on /a/b should fail with a missing ?lastName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?lastName"))
    }


    test (s"A POST on /a/b should fail with a missing ?age $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo",
                                                                                         "lastName" : "bar"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?age"))
    }


    test (s"A POST on /a/b should fail with a bad ? $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""true""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 400, List("Expecting","?firstName", "supplied value has item type xs:boolean"))
    }
  }

   def sadPathRaxRolesAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a/b should fail with array body $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" [1, 2, 3] """,
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","instance of map(*)"))
    }

    test (s"A PUT on /a/b should fail with a bad firstName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" {  "firstName" : [1, 2, 3] }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?firstName"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : true
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?stuff", "instance of map(*)"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?thing $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : [3, 4]
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?thing", "instance of xsd:boolean"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : {"false" : null}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?array", "instance of array(*)"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "obj"   : "not!"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?obj", "instance of map(*)"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [false,2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?array?1", "instance of xsd:double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array2?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : ["hello",2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?array2?1", "instance of xsd:double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?string $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {},
                                                                                              "string" : null
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?string", "instance of xsd:string"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj?a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?obj?a", "instance of xsd:string"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?null $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "a" : "A",
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo",
                                                                                              "null" : "bar"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","stuff?null", "empty"))
    }


    test (s"A POST on /a/b should fail with a missing ?fistName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "name" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?firstName"))
    }


    test (s"A POST on /a/b should fail with a missing ?lastName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?lastName"))
    }


    test (s"A POST on /a/b should fail with a missing ?age $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo",
                                                                                         "lastName" : "bar"
                                                                                        }""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?age"))
    }


    test (s"A POST on /a/b should fail with a bad ? $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""true""",
                                                    false, Map[String,List[String]]("X-ROLES"->List("admin"))),
                                            response, chain), 400, List("Expecting","?firstName", "supplied value has item type xs:boolean"))
    }
  }


  def sadPathCodeMessageAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /a/b should fail with array body $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" [1, 2, 3] """,
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","object"))
    }

    test (s"A PUT on /a/b should fail with a bad firstName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json",""" {  "firstName" : [1, 2, 3] }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","firstName"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : true
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting an","stuff", "object"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?thing $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : [3, 4]
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?thing", "boolean"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : {"false" : null}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?array", "an array"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "obj"   : "not!"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?obj", "object"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [false,2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?array?1", "a double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?array2?1 $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : ["hello",2,3],
                                                                                              "obj"   : {}
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?array2?1", "a double"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?string $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {},
                                                                                              "string" : null
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?string", "a string"))
    }


    test (s"A PUT on /a/b should fail with a bad stuff?obj?a $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","stuff?obj?a", "a string"))
    }

    test (s"A PUT on /a/b should fail with a bad stuff?null $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/a/b", "application/json","""{  "firstName" : "Jorge",
                                                                                            "stuff" : {
                                                                                              "thing" : true,
                                                                                              "array" : [1,2,3],
                                                                                              "array2" : [1,2,3],
                                                                                              "obj"   : {
                                                                                                "a" : "A",
                                                                                                "b" : "B",
                                                                                                "c" : "C"
                                                                                              },
                                                                                              "string" : "foo",
                                                                                              "null" : "bar"
                                                                                            }
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting","stuff?null"))
    }


    test (s"A POST on /a/b should fail with a missing ?fistName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "name" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","firstName"))
    }


    test (s"A POST on /a/b should fail with a missing ?lastName $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","lastName"))
    }


    test (s"A POST on /a/b should fail with a missing ?age $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""{
                                                                                         "firstName" : "foo",
                                                                                         "lastName" : "bar"
                                                                                        }""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting an","age"))
    }


    test (s"A POST on /a/b should fail with a bad ? $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("POST", "/a/b", "application/json","""true""",
                                                    false, Map[String,List[String]]()),
                                            response, chain), 401, List("Expecting a","firstName", "supplied value has item type xs:boolean"))
    }
  }


def jsonCaptureAssertions (validator : Validator, wadlDesc : String, configDesc : String) {
  test (s"A POST on /c with an atomic type should cature that type $wadlDesc with $configDesc") {
    val req = request("POST", "/c", "application/json","""52""",
                      false, Map[String,List[String]]())
    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct header should be set...
      assert(csReq.getHeaders("X-JSON").toList == List("52"))

    })
    validator.validate(req, response, chain)
  }

  test (s"A POST on /c with a null  should cature that type $wadlDesc with $configDesc") {
    val req = request("POST", "/c", "application/json","""null""",
                      false, Map[String,List[String]]())
    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct header should be set...
      assert(csReq.getHeaders("X-JSON").toList == List("null"))

    })
    validator.validate(req, response, chain)
  }


  test (s"A POST on /c with an array type should cature that array $wadlDesc with $configDesc") {
    val req = request("POST", "/c", "application/json","""[52, 43, 78, 90]""",
                      false, Map[String,List[String]]())
    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct header should be set...
      assert(csReq.getHeaders("X-JSON").toList == List("[52,43,78,90]"))

    })
    validator.validate(req, response, chain)
  }

  test (s"A POST on /c with an object type should cature that array $wadlDesc with $configDesc") {
    val req = request("POST", "/c", "application/json","""{ "52" :  43, "78" : 90}]""",
                      false, Map[String,List[String]]())
    req.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
      // Correct header should be set...
      val objectMapper = new ObjectMapper()
      val jsonNode = objectMapper.readTree (csReq.getHeaders("X-JSON").toList.head)

      assert (jsonNode.findValue("52").asInt == 43)
      assert (jsonNode.findValue("78").asInt == 90)
    })
    validator.validate(req, response, chain)
  }

}



 //  With plain params disabled
 //
 for ((wadlDesc, wadl) <- jsonPlainWADLs) {
   for ((configDesc, config) <- jsonPlainDisabledConfigs) {
     val validator = Validator(wadl, config)
     happyPathAssertions(validator, wadlDesc, configDesc)
     sadPathPassingAssertions(validator, wadlDesc, configDesc)
   }
 }

 //
 //  With actual plain params
 //
 for ((wadlDesc, wadl) <- jsonPlainWADLs) {
   for ((configDesc, config) <- jsonPlainConfigs) {
     val validator = Validator(wadl, config)

     happyPathAssertions(validator, wadlDesc, configDesc)
     sadPathAssertions(validator, wadlDesc, configDesc)
   }
 }

 //
 //  With code and message plain params
 //
 for ((wadlDesc, wadl) <- jsonMessageWADLs) {
   for ((configDesc, config) <- jsonPlainConfigs) {
     val validator = Validator(wadl, config)

     happyPathAssertions(validator, wadlDesc, configDesc)
     sadPathCodeMessageAssertions(validator, wadlDesc, configDesc)
   }
 }

 //
 // With capture JSON
 //

 for ((wadlDesc, wadl) <- jsonCaptureWADL) {
   for ((configDesc, config) <- jsonCaptureConfig) {
     val validator = Validator(wadl, config)

     happyPathAssertions(validator, wadlDesc, configDesc)
     jsonCaptureAssertions(validator, wadlDesc, configDesc)
   }
 }


  //
  //  With rax:roles
  //
 for ((wadlDesc, wadl) <- jsonPlainRaxRolesWADL) {
   for ((configDesc, config) <- jsonPlainRaxRolesConfigs) {
     val validator = Validator(wadl, config)

     happyPathRaxRolesAssertions(validator, wadlDesc, configDesc)
     sadPathRaxRolesAssertions(validator, wadlDesc, configDesc)
     BadAccessRaxRolesAssertions (validator, wadlDesc, configDesc)
   }
 }

  //
  //  With rax:roles masked
  //

 for ((wadlDesc, wadl) <- jsonPlainRaxRolesWADL) {
   for ((configDesc, config) <- jsonPlainRaxRolesMaskConfigs) {
     val validator = Validator(wadl, config)

     happyPathRaxRolesAssertions(validator, wadlDesc, configDesc)
     sadPathRaxRolesAssertions(validator, wadlDesc, configDesc)
     BadAccessRaxRolesMaskedAssertions (validator, wadlDesc, configDesc)
   }
 }


}
