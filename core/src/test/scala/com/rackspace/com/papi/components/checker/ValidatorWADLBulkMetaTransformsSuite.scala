/***
 *   Copyright 2017 Rackspace US, Inc.
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
class ValidatorWADLBulkMetaTransformsSuite extends BaseValidatorSuite {
  ///
  ///  Configs
  ///
  val baseConfig = {
    val c = TestConfig()
    c.enableRaxRolesExtension = false
    c
  }

  val baseWithRaxRoles = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }


  val baseWithRaxRolesRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }


  val baseWithRaxRolesJoinXPaths = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }

  val baseWithRaxRolesMask = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = false
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }


  val baseWithRaxRolesMaskRemoveDups = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }


  val baseWithRaxRolesMaskJoinXPaths = {
    val c = TestConfig()
    c.enableRaxRolesExtension = true
    c.maskRaxRoles403 = true
    c.removeDups = true
    c.joinXPathChecks = true
    c.checkWellFormed = false
    c.checkPlainParams = false
    c.enableCaptureHeaderExtension = false
    c.setParamDefaults = false
    c.enableAssertExtension = false
    c.checkElements = false
    c
  }

  val WADL_metadata = <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
    xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
    <grammars>
       <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
    </grammars>
    <resources base="https://resource.api.rackspace.com">
      <resource id="standardResource" path="standard/{id}" rax:useMetadata="standardMeta">
           <param name="id" style="template" type="tst:UUID" required="true"/>
           <method name="GET"/>
      </resource>
      <resource id="customResource" path="custom" rax:useMetadata="customMeta">
           <method name="GET"/>
      </resource>
    </resources>
    <rax:metadata id="standardMeta">
      <!-- I propose we support very simple patterns * and sameThing: (or anyOther string which will be prepended) -->
      <rax:metaRole name="admin" pattern="*"/>
      <rax:metaRole name="billing:role" pattern="billing:"/>
      <rax:metaRole name="service:role" pattern="service:"/>
    </rax:metadata>
    <rax:metadata id="customMeta">
      <!-- at least one pattern with '*' is required, this is the admin role,
               multiple admins are allowed...-->
      <rax:metaRole name="admin" pattern="*"/>
      <rax:metaRole name="superAdmin" pattern="*"/>
      <!-- pattern is optional, if it is left off then the roleName followed by a colon is assumed.
               In this case the pattern is 'customer:role:' -->
      <rax:metaRole name="customer:role"/>
      <!-- multiple roles are allowed as well with the same pattern.
               One role per metaRole, since this is an edge case...
           -->
      <rax:metaRole name="service:role" pattern="service:"/>
      <rax:metaRole name="another_role" pattern="service:"/>
      <!-- Another edge case regex stuff in the pattern should not mess things up.
               This silly combination of role name and pattern should work.
               The pattern is not a regEx |||: is simply treated as a string.
          -->
      <rax:metaRole name="???" pattern="|||:"/>

      <!-- Here's another weird edge case patterns contain quotes -->
      <rax:metaRole name="customer:quote" pattern="ap'o's:"/>
      <rax:metaRole name="customer:quote" pattern='quo"t"e:'/>
    </rax:metadata>
  </application>

  //
  //  Config combinations
  //
  val mataBulkDisabledRaxRoles = Map[String, Config]("base config with rax roles disabled" -> baseConfig)

  val metaBulkRaxRoles = Map[String, Config](
    "base config with rax roles enabled" -> baseWithRaxRoles,
    "base config with rax roles enabled (remove dups)" -> baseWithRaxRolesRemoveDups,
    "base config with rax roles enabled (remove dups, joinXPath)" -> baseWithRaxRolesJoinXPaths
  )

  val metaBulkRaxRolesMask = Map[String, Config](
    "base config with rax roles mask enabled" -> baseWithRaxRolesMask,
    "base config with rax roles mask enabled (remove dups)" -> baseWithRaxRolesMaskRemoveDups,
    "base config with rax roles mask enabled (remove dups, joinXPath)" -> baseWithRaxRolesMaskJoinXPaths
  )

  //
  //  WADL combinations
  //
  val metadataWADLs = Map[String, Elem]("WADL with metadata"->WADL_metadata)

  //
  //  Assertions
  //
  def happyPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A GET on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462 should succeed on $wadlDesc with $configDesc") {
      validator.validate(request("GET", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462"), response, chain)
    }

    test (s"A get on /custom should succeed on $wadlDesc with $configDesc") {
      validator.validate(request("GET", "/custom"), response, chain)
    }
  }


  def happySadPathAssertions(validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A GET on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462-yyy should fail with 404 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("GET", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462-yyy"), response, chain),
        404, List("UUID"))
    }

    test (s"A get on /cus should fail with 404 on $wadlDesc with $configDesc") {
      assertResultFailed (validator.validate(request("GET", "/cus"), response, chain),
        404, List("custom","standard"))
    }
  }

  def sadWhenRaxRolesAreDisabled (validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A GET on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 404 on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("GET", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata"), response, chain),
        404, List("metadata"))
    }

    test (s"A get on /custom/metadata should fail with 404 on $wadlDesc with $configDesc") {
      assertResultFailed (validator.validate(request("GET", "/custom/metadata"), response, chain),
        404, List("metadata"))
    }
  }


  def happyWhenRaxRolesEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test (s"A PUT on /custom/metadata should succeed regardless of prefix for users with admin role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="service:type">A type</meta>
                                     <meta key="foo">A type</meta>
                                     <meta key="bar:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
    }
    test (s"A PUT on /custom/metadata should succeed regardless of prefix for users with admin role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:type": "A type",
                                       "foo" : "A type",
                                       "bar:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("admin"))), response, chain)
    }
    test (s"A PUT on /custom/metadata should succeed regardless of prefix for users with superAdmin role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="service:type">A type</meta>
                                     <meta key="foo">A type</meta>
                                     <meta key="bar:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","superAdmin"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed regardless of prefix for users with superAdmin role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:type": "A type",
                                       "foo" : "A type",
                                       "bar:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","superAdmin"))), response, chain)
    }


    test (s"A PUT on /custom/metadata should succeed with customer:role: prefix for users with  customer:role role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="customer:role:type2">A name</meta>
                                     <meta key="customer:role:type">A type</meta>
                                     <meta key="customer:role:hey">A type</meta>
                                     <meta key="customer:role:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:role"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed with customer:role: prefix for users with customer:role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "customer:role:type2": "A name",
                                       "customer:role:type": "A type",
                                       "customer:role:hey" : "A type",
                                       "customer:role:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:role"))), response, chain)
    }


    test (s"A PUT on /custom/metadata should succeed with service: prefix for users with  service:role role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="service:type">A type</meta>
                                     <meta key="service:hey">A type</meta>
                                     <meta key="service:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed with service: prefix for users with service:role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:type": "A type",
                                       "service:hey" : "A type",
                                       "service:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain)
    }


    test (s"A PUT on /custom/metadata should succeed with service: prefix for users with another_role role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="service:type">A type</meta>
                                     <meta key="service:hey">A type</meta>
                                     <meta key="service:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","another_role"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed with service: prefix for users with another_role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:type": "A type",
                                       "service:hey" : "A type",
                                       "service:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","another_role"))), response, chain)
    }



    test (s"A PUT on /custom/metadata should succeed with |||: prefix for users with ??? role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="|||:type2">A name</meta>
                                     <meta key="|||:type">A type</meta>
                                     <meta key="|||:hey">A type</meta>
                                     <meta key="|||:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","???"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed with |||: prefix for users with ??? (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "|||:type2": "A name",
                                       "|||:type": "A type",
                                       "|||:hey" : "A type",
                                       "|||:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","???"))), response, chain)
    }




    test (s"A PUT on /custom/metadata should succeed with ap'o's: prefix for users with customer:quote role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="ap'o's:type2">A name</meta>
                                     <meta key="ap'o's:type">A type</meta>
                                     <meta key="ap'o's:hey">A type</meta>
                                     <meta key="ap'o's:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote"))), response, chain)
    }

    test (s"A PUT on /custom/metadata should succeed with ap'o's: prefix for users with customer:quote (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "ap'o's:type2": "A name",
                                       "ap'o's:type": "A type",
                                       "ap'o's:hey" : "A type",
                                       "ap'o's:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote"))), response, chain)
    }


    test (s"""A PUT on /custom/metadata should succeed with quo"t"e: prefix for users with customer:quote role $wadlDesc with $configDesc""") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key='quo"t"e:type2'>A name</meta>
                                     <meta key='quo"t"e:type'>A type</meta>
                                     <meta key='quo"t"e:hey'>A type</meta>
                                     <meta key='quo"t"e:foo'>Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote"))), response, chain)
    }

    test (s"""A PUT on /custom/metadata should succeed with quo"t"e: prefix for users with customer:quote (json) $wadlDesc with $configDesc""") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "quo\"t\"e:type2": "A name",
                                       "quo\"t\"e:type": "A type",
                                       "quo\"t\"e:hey" : "A type",
                                       "quo\"t\"e:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote"))), response, chain)
    }


    test (s"""A PUT on /custom/metadata should succeed with quo"t"e:, ap'o's, customer:role, and service: prefixs for users with the right set of roles $wadlDesc with $configDesc""") {
      validator.validate(request("PUT", "/custom/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key='quo"t"e:type2'>A name</meta>
                                     <meta key="ap'o's:type">A type</meta>
                                     <meta key='customer:role:hey'>A type</meta>
                                     <meta key='service:foo'>Aonther item</meta>
                                     <meta key='|||:foo'>Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote","another_role","customer:role","???"))), response, chain)
    }

    test (s"""A PUT on /custom/metadata should succeed with quo"t"e:, ap'o's, customer:role, and service: prefix for users with the right set of roles (json) $wadlDesc with $configDesc""") {
      validator.validate(request("PUT", "/custom/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "quo\"t\"e:type2": "A name",
                                       "ap'o's:type": "A type",
                                       "customer:role:hey" : "A type",
                                       "service:foo" : "Another item",
                                       "|||:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","customer:quote","service:role","customer:role","???"))), response, chain)
    }

    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with any prefix for users with admin role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="ap'o's:type2">A name</meta>
                                     <meta key="foo:type">A type</meta>
                                     <meta key="baz:hey">A type</meta>
                                     <meta key="biz:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","admin"))), response, chain)
    }

    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with any prefix for users with admin role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "ap'o's:type2": "A name",
                                       "foo:type": "A type",
                                       "baz:hey" : "A type",
                                       "biz:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","admin"))), response, chain)
    }


    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix billing: for users with billing:role role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="billing:type2">A name</meta>
                                     <meta key="billing:type">A type</meta>
                                     <meta key="billing:hey">A type</meta>
                                     <meta key="billing:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","billing:role"))), response, chain)
    }

    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix billing: for users with billing:role role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "billing:type2": "A name",
                                       "billing:type": "A type",
                                       "billing:hey" : "A type",
                                       "billing:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","billing:role"))), response, chain)
    }



        test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix service: for users with service:role role $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="service:type">A type</meta>
                                     <meta key="service:hey">A type</meta>
                                     <meta key="service:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain)
    }

    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix service: for users with service:role role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:type": "A type",
                                       "service:hey" : "A type",
                                       "service:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain)
    }



    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix service: and billing: for users with service:role and billing:role roles $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/xml",
                                 <metadata xmlns="http://docs.rackspace.com/metadata/api">
                                     <meta key="service:type2">A name</meta>
                                     <meta key="billing:type">A type</meta>
                                     <meta key="billing:hey">A type</meta>
                                     <meta key="service:foo">Aonther item</meta>
                               </metadata>
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role","billing:role"))), response, chain)
    }

    test (s"A PUT on /standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata should succeed with prefix billing: and service: for users with service:role and billing:role role (json) $wadlDesc with $configDesc") {
      validator.validate(request("PUT", "/standard/2f8fd169-5ab3-4dd1-b4d7-e1d18d665498/metadata","application/json", """
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "billing:type": "A type",
                                       "billing:hey" : "A type",
                                       "service:foo" : "Another item"
                                     }
                                 }
                               """
                               , false,
                               Map[String,List[String]]("X-ROLES"->List("foo","service:role","billing:role"))), response, chain)
    }

  }


  def commonSadWhenRaxRolesEnabled(validator : Validator, wadlDesc : String, configDesc : String) {
    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 400 if no keys are specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api"/>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role","billing:role"))), response, chain),
        400, List("The message must contain metadata items"))
    }


    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 400 if no keys are specified (json) on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/json","""
        { "metadata" : {}}
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role","billing:role"))), response, chain),
        400, List("The message must contain metadata items"))
    }

    test(s"A PUT on /custom/metadata should fail with 400 if no keys are specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api"/>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role","customer:role","???","customer:quote"))), response, chain),
        400, List("The message must contain metadata items"))
    }


    test(s"A PUT on /custom/metadata should fail with 400 if no keys are specified (json) on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
        { "metadata" : {}}
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","another_role","customer:role","???","customer:quote"))), response, chain),
        400, List("The message must contain metadata items"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix service: if service:role is not specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="service:type2">A name</meta>
          <meta key="service:foo">Aonther item</meta>
          <meta key="billing:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","billing:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix service: if service:role is not specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:foo" : "Another item",
                                       "billing:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","billing:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix billing: if billing:role is not specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="billing:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix billing: if billing:role is not specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "billing:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /custom/metadata should fail with 403 on prefix |||: if an incorrect role is specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="|||:type2">A name</meta>
          <meta key="|||:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /custom/metadata should fail with 403 on prefix |||: if an incorrect role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "|||:type2": "A name",
                                       "|||:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }


    test(s"A PUT on /custom/metadata should fail with 403 on prefix ap'o's: if an incorrect role is specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="ap'o's:type2">A name</meta>
          <meta key="ap'o's:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"A PUT on /custom/metadata should fail with 403 on prefix ap'o's: if an incorrect role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "ap'o's:type2": "A name",
                                       "ap'o's:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"""A PUT on /custom/metadata should fail with 403 on prefix quo"t"e: if an incorrect role is specified on $wadlDesc with $configDesc""") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key='quo"t"e:type2'>A name</meta>
          <meta key='quo"t"e:foo'>Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

    test(s"""A PUT on /custom/metadata should fail with 403 on prefix quo"t"e: if an incorrect role is specified on (json) $wadlDesc with $configDesc""") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "quo\"t\"e:type2": "A name",
                                       "quo\"t\"e:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo","service:role"))), response, chain),
        403, List("not allowed to set metadata items of this type"))
    }

  }

  def sadWhenRaxRolesEnabled(validator : Validator, wadlDesc : String, configDesc : String) {

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix service: if no valid role is  specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="service:type2">A name</meta>
          <meta key="service:foo">Aonther item</meta>
          <meta key="billing:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 403 on prefix service: if no valide role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:foo" : "Another item",
                                       "billing:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        403, List("You are forbidden to perform the operation"))
    }


    test(s"A PUT on /custom/metadata should fail with 403 on prefix service: if no valid role is  specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="service:type2">A name</meta>
          <meta key="service:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        403, List("You are forbidden to perform the operation"))
    }

    test(s"A PUT on /custom/metadata should fail with 403 on prefix service: if no valide role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        403, List("You are forbidden to perform the operation"))
    }
  }


  def sadWhenRaxRolesMaskedEnabled(validator : Validator, wadlDesc : String, configDesc : String) {

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 405 on prefix service: if no valid role is  specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="service:type2">A name</meta>
          <meta key="service:foo">Aonther item</meta>
          <meta key="billing:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        405, List("Bad method: PUT"))
    }

    test(s"A PUT on /standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata should fail with 405 on prefix service: if no valide role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/standard/ff98bf08-3a9c-4964-88cd-987dbc1b6462/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:foo" : "Another item",
                                       "billing:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        405, List("Bad method: PUT"))
    }


    test(s"A PUT on /custom/metadata should fail with 405 on prefix service: if no valid role is  specified on $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/xml",
        <metadata xmlns="http://docs.rackspace.com/metadata/api">
          <meta key="service:type2">A name</meta>
          <meta key="service:foo">Aonther item</meta>
        </metadata>
        ,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        405, List("Bad method: PUT"))
    }

    test(s"A PUT on /custom/metadata should fail with 405 on prefix service: if no valide role is specified on (json) $wadlDesc with $configDesc") {
      assertResultFailed(validator.validate(request("PUT", "/custom/metadata", "application/json","""
                                 {
                                     "metadata": {
                                       "service:type2": "A name",
                                       "service:foo" : "Another item"
                                     }
                                 }
        """,false, Map[String,List[String]]("X-ROLES"->List("foo"))), response, chain),
        405, List("Bad method: PUT"))
    }
  }

  //
  //  Rax-roles disabled
  //
  for ((wadlDesc, wadl) <- metadataWADLs) {
    for ((configDesc, config) <- mataBulkDisabledRaxRoles) {
      val validator = Validator(("wadl.xml",wadl), config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPathAssertions(validator, wadlDesc, configDesc)
      sadWhenRaxRolesAreDisabled(validator, wadlDesc, configDesc)
    }
  }


  //
  //  Rax-Roles enabled
  //
  for ((wadlDesc, wadl) <- metadataWADLs) {
    for ((configDesc, config) <- metaBulkRaxRoles) {
      val validator = Validator(("wadl.xml",wadl), config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPathAssertions(validator, wadlDesc, configDesc)
      happyWhenRaxRolesEnabled(validator, wadlDesc, configDesc)
      commonSadWhenRaxRolesEnabled (validator, wadlDesc, configDesc)
      sadWhenRaxRolesEnabled(validator, wadlDesc, configDesc)
    }
  }

  //
  //  Rax-Roles masked enabled
  //
  for ((wadlDesc, wadl) <- metadataWADLs) {
    for ((configDesc, config) <- metaBulkRaxRolesMask) {
      val validator = Validator(("wadl.xml",wadl), config)

      happyPathAssertions(validator, wadlDesc, configDesc)
      happySadPathAssertions(validator, wadlDesc, configDesc)
      happyWhenRaxRolesEnabled(validator, wadlDesc, configDesc)
      commonSadWhenRaxRolesEnabled (validator, wadlDesc, configDesc)
      sadWhenRaxRolesMaskedEnabled(validator, wadlDesc, configDesc)
    }
  }

}
