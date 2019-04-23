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
package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.TestConfig
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerOptDupsSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("chk","http://www.rackspace.com/repose/wadl/checker")
  register ("xsd", "http://www.w3.org/2001/XMLSchema")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which utilizes rax roles and the remove dups option into ")
    info ("a description of a machine that can handle the correct header validations in checker format")
    info ("so that an API validator can process the checker format to validate the roles")

    //
    //  Note that we are testing a very specific scenario that forces
    //  a failure if the remove dups operation is implemented incorrectly.
    //
    //  More comperhensive tests of RaxRoles and Remove Dups
    //  operations are in other Specs.
    //

    val raxRolesWADL = <application xmlns:rax="http://docs.rackspace.com/api"
         xmlns:xsd="http://www.w3.org/2001/XMLSchema"
         xmlns="http://wadl.dev.java.net/2009/02"
    >
  <resources base="http://localhost">
    <resource id="_0be44e11-68e6-44d4-acae-ec6a366282ba" path="/path/to/this">
      <method name="GET"    id="_0be44e11-68e6-44d4-acae-ec6a366282ba-GET"    rax:roles="role1 role2 role3 role4"/>
    </resource>
    <resource id="_0e126d22-d8d2-4ece-8667-b19875ae1e12" path="/path/to/this">
      <method name="PUT"    id="_0e126d22-d8d2-4ece-8667-b19875ae1e12-PUT"    rax:roles="role1 role2 role3"/>
    </resource>
    <resource id="_b3d2229d-cc90-4d53-9f05-c1c60117af07" path="/path/to/this">
      <method name="POST"   id="_b3d2229d-cc90-4d53-9f05-c1c60117af07-POST"   rax:roles="role1 role2"/>
    </resource>
    <resource id="_4724d9ab-d560-4668-bc18-3690cc7faa76" path="/path/to/this">
      <method name="DELETE" id="_4724d9ab-d560-4668-bc18-3690cc7faa76-DELETE" rax:roles="role1"/>
    </resource>
    <resource id="_bed37f49-c275-474b-8681-a3a48918ddb9" path="/path/to/that">
      <method name="GET"    id="_bed37f49-c275-474b-8681-a3a48918ddb9-GET"    />
      <method name="PUT"    id="_bed37f49-c275-474b-8681-a3a48918ddb9-PUT"    />
    </resource>
    <resource id="_6fdeaf12-ca1d-443e-bd77-25c5cb8b0284" path="/path/to/that">
      <method name="GET"    id="_6fdeaf12-ca1d-443e-bd77-25c5cb8b0284-GET"    rax:roles="role1"/>
      <method name="PUT"    id="_6fdeaf12-ca1d-443e-bd77-25c5cb8b0284-PUT"    rax:roles="role1"/>
      <method name="POST"   id="_6fdeaf12-ca1d-443e-bd77-25c5cb8b0284-POST"   rax:roles="role1"/>
      <method name="DELETE" id="_6fdeaf12-ca1d-443e-bd77-25c5cb8b0284-DELETE" rax:roles="role1"/>
    </resource>
  </resources>
    </application>

    val config = {
      val tc = TestConfig()
      tc.removeDups = true
      tc.enableRaxRolesExtension = true
      tc
    }

    scenario ("The WADL contains rax:roles with remove dups optimization enabled and a flow which allows a method call with and without roles") {
      Given("A WADL contains rax:roles with remove dups optimization enabled and a flow which allows a method call with and without roles")
      When ("The wadl is translated")
      val checker = builder.build(raxRolesWADL, config)
      Then("The following assertions should hold...")
      assert(checker, "count(chk:checker/chk:step[@type='METHOD' and @match='GET'])    = 3")
      assert(checker, "count(chk:checker/chk:step[@type='METHOD' and @match='POST'])   = 2")
      assert(checker, "count(chk:checker/chk:step[@type='METHOD' and @match='PUT'])    = 3")
      assert(checker, "count(chk:checker/chk:step[@type='METHOD' and @match='DELETE']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @match='role1']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @match='role1|role2']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @match='role1|role2|role3']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='HEADER_ANY' and @match='role1|role2|role3|role4']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='CAPTURE_HEADER' and @path='for $h in req:headers(''X-ROLES'', true()) return if ($h = (''role1'')) then $h else ()']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='CAPTURE_HEADER' and @path='for $h in req:headers(''X-ROLES'', true()) return if ($h = (''role1'', ''role2'')) then $h else ()']) = 1")
      assert(checker, "count(chk:checker/chk:step[@type='CAPTURE_HEADER' and @path='for $h in req:headers(''X-ROLES'', true()) return if ($h = (''role1'', ''role2'', ''role3'')) then $h else ()']) = 1")
      assert(checker,
             "count(chk:checker/chk:step[@type='CAPTURE_HEADER' and @path='for $h in req:headers(''X-ROLES'', true()) return if ($h = (''role1'', ''role2'', ''role3'', ''role4'')) then $h else ()']) = 1")

      List("GET","PUT").foreach (m => {
        assert(checker, Start, URL("path"), URL("to"), URL("that"), Method(m), Accept)
      })

      List("GET","PUT","POST","DELETE").foreach ( m => {
        assert(checker, Start, URL("path"), URL("to"), URL("that"), Method(m), HeaderAny("X-ROLES","role1"),
               RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('role1')) then $h else ()"),
               Accept)
      })

      assert(checker, Start, URL("path"), URL("to"), URL("this"), Method("DELETE"), HeaderAny("X-ROLES","role1"),
             RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('role1')) then $h else ()"),
             Accept)

      assert(checker, Start, URL("path"), URL("to"), URL("this"), Method("POST"), HeaderAny("X-ROLES","role1|role2"),
             RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('role1', 'role2')) then $h else ()"),
             Accept)

      assert(checker, Start, URL("path"), URL("to"), URL("this"), Method("PUT"), HeaderAny("X-ROLES","role1|role2|role3"),
             RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('role1', 'role2', 'role3')) then $h else ()"),
             Accept)

      assert(checker, Start, URL("path"), URL("to"), URL("this"), Method("GET"), HeaderAny("X-ROLES","role1|role2|role3|role4"),
             RaxCaptureHeader("X-RELEVANT-ROLES", "for $h in req:headers('X-ROLES', true()) return if ($h = ('role1', 'role2', 'role3', 'role4')) then $h else ()"),
             Accept)
    }
  }
}
