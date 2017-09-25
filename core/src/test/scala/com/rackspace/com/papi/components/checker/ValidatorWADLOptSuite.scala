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
package com.rackspace.com.papi.components.checker

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source

//
//  Test optimizations in complex setups.
//

@RunWith(classOf[JUnitRunner])
class ValidatorWADLOptSuite extends BaseValidatorSuite {
  //
  //  WADL with resources that have shared XPath checks, the following
  //  tests will use this WADL.
  //
  val sharedXPathWADL = scala.xml.XML.loadString(Source.fromInputStream(this.getClass.getResourceAsStream("/wadl/sharedXPath.wadl")).mkString)

  val shardXPathNoRemoveDups = Validator(sharedXPathWADL, TestConfig(false, false, true, true, true, 1, true, true, false, "Xalan", false))
  val shardXPathNoDups = Validator(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", false))
  val shardXPathNoDupsJoinXPath = Validator(sharedXPathWADL, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
  val shardXPathNoDupsJoinXPathMethodLabels = Validator(sharedXPathWADL, {
    val tc = TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true)
    tc.preserveMethodLabels = true
    tc
  })

  val good_usage16 =
<atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/usage/nova/ips" only_usage_up_down="true">
  <usage>
    <up>
      <down/>
    </up>
  </usage>
</atom:entry>

  val good_usage17 =
<atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/event/nova/host" only_usage_up_down="true">
  <usage>
    <up>
      <down/>
    </up>
  </usage>
</atom:entry>

  val good_rhel = 
<atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/event/RHEL" only_usage="true">
  <usage/>
</atom:entry>

  val bad_usage = <foo/>

  test("POST of RHEL should work on nova/entries on shardXPathNoRemoveDups") {
    shardXPathNoRemoveDups.validate(request("POST", "nova/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on nova/entries on shardXPathNoDups") {
    shardXPathNoDups.validate(request("POST", "nova/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on nova/entries on shardXPathNoDupsJoinXPath") {
    shardXPathNoDupsJoinXPath.validate(request("POST", "nova/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on nova/entries on shardXPathNoDupsJoinXPathMethodLabels") {
    shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "nova/entries", "application/atom+xml", good_rhel), response, chain)
  }


  test("POST of RHEL should work on servers/entries on shardXPathNoRemoveDups") {
    shardXPathNoRemoveDups.validate(request("POST", "servers/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on servers/entries on shardXPathNoDups") {
    shardXPathNoDups.validate(request("POST", "servers/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on servers/entries on shardXPathNoDupsJoinXPath") {
    shardXPathNoDupsJoinXPath.validate(request("POST", "servers/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of RHEL should work on servers/entries on shardXPathNoDupsJoinXPathMethodLabels") {
    shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "servers/entries", "application/atom+xml", good_rhel), response, chain)
  }

  test("POST of good_usage17 should work on servers/entries on shardXPathNoRemoveDups") {
    shardXPathNoRemoveDups.validate(request("POST", "servers/entries", "application/atom+xml", good_usage17), response, chain)
  }

  test("POST of good_usage17 should work on servers/entries on shardXPathNoDups") {
    shardXPathNoDups.validate(request("POST", "servers/entries", "application/atom+xml", good_usage17), response, chain)
  }

  test("POST of good_usage17 should work on servers/entries on shardXPathNoDupsJoinXPath") {
    shardXPathNoDupsJoinXPath.validate(request("POST", "servers/entries", "application/atom+xml", good_usage17), response, chain)
  }

  test("POST of good_usage17 should work on servers/entries on shardXPathNoDupsJoinXPathMethodLabels") {
    shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "servers/entries", "application/atom+xml", good_usage17), response, chain)
  }

  test("POST of good_usage16 should work on nova/entries on shardXPathNoRemoveDups") {
    shardXPathNoRemoveDups.validate(request("POST", "nova/entries", "application/atom+xml", good_usage16), response, chain)
  }

  test("POST of good_usage16 should work on nova/entries on shardXPathNoDups") {
    shardXPathNoDups.validate(request("POST", "nova/entries", "application/atom+xml", good_usage16), response, chain)
  }

  test("POST of good_usage16 should work on nova/entries on shardXPathNoDupsJoinXPath") {
    shardXPathNoDupsJoinXPath.validate(request("POST", "nova/entries", "application/atom+xml", good_usage16), response, chain)
  }

  test("POST of good_usage16 should work on nova/entries on shardXPathNoDupsJoinXPathMethodLabels") {
    shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "nova/entries", "application/atom+xml", good_usage16), response, chain)
  }


  test("POST of good_usage16 on servers/entries should fail on shardXPathNoRemoveDups") {
    assertResultFailed(shardXPathNoRemoveDups.validate(request("POST", "servers/entries", "application/atom+xml",
                                                               good_usage16), response, chain), 400)
  }

  test("POST of good_usage16 onservers/entries  should fail on shardXPathNoDups") {
    assertResultFailed(shardXPathNoDups.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         good_usage16), response, chain), 400)
  }

  test("POST of good_usage16 onservers/entries  should fail on shardXPathNoDupsJoinXPath") {
    assertResultFailed(shardXPathNoDupsJoinXPath.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         good_usage16), response, chain), 400)
  }

  test("POST of good_usage16 on servers/entries  should fail on shardXPathNoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         good_usage16), response, chain), 400)
  }

  test("POST of good_usage17 on nova/entries should fail on shardXPathNoRemoveDups") {
    assertResultFailed(shardXPathNoRemoveDups.validate(request("POST", "nova/entries", "application/atom+xml",
                                                               good_usage17), response, chain), 400)
  }

  test("POST of good_usage17 on servers/entries  should fail on shardXPathNoDups") {
    assertResultFailed(shardXPathNoDups.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         good_usage17), response, chain), 400)
  }

  test("POST of good_usage17 on servers/entries  should fail on shardXPathNoDupsJoinXPath") {
    assertResultFailed(shardXPathNoDupsJoinXPath.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         good_usage17), response, chain), 400)
  }

  test("POST of good_usage17 on nova/entries  should fail on shardXPathNoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         good_usage17), response, chain), 400)
  }


  test("POST of bad_usage on nova/entries should fail on shardXPathNoRemoveDups") {
    assertResultFailed(shardXPathNoRemoveDups.validate(request("POST", "nova/entries", "application/atom+xml",
                                                               bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on nova/entries  should fail on shardXPathNoDups") {
    assertResultFailed(shardXPathNoDups.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on nova/entries  should fail on shardXPathNoDupsJoinXPath") {
    assertResultFailed(shardXPathNoDupsJoinXPath.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on nova/entries  should fail on shardXPathNoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "nova/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on servers/entries should fail on shardXPathNoRemoveDups") {
    assertResultFailed(shardXPathNoRemoveDups.validate(request("POST", "servers/entries", "application/atom+xml",
                                                               bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on servers/entries  should fail on shardXPathNoDups") {
    assertResultFailed(shardXPathNoDups.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on servers/entries  should fail on shardXPathNoDupsJoinXPath") {
    assertResultFailed(shardXPathNoDupsJoinXPath.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }

  test("POST of bad_usage on servers/entries  should fail on shardXPathNoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPathNoDupsJoinXPathMethodLabels.validate(request("POST", "servers/entries", "application/atom+xml",
                                                         bad_usage), response, chain), 400)
  }


    val sharedXPathWADL2 =
<application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:foo="http://www.rackspace.com/foo/bar"
             xmlns:xs="http://www.w3.org/2001/XMLSchema"
             xmlns:atom="http://www.w3.org/2005/Atom">
    <resources base="http://localhost/">
        <resource path="y" type="#FOO"/>
        <resource path="x" type="#FOO #BAR"/>
    </resources>
    <resource_type id="FOO">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:bar">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:bar/@junk"/>
                </representation>
            </request>
        </method>
    </resource_type>
    <resource_type id="BAR">
        <method name="POST">
            <request>
                <representation mediaType="application/xml" element="foo:foo">
                    <param name="stuff"
                           style="plain"
                           required="true"
                           path="/foo:foo/@junk"/>
                </representation>
            </request>
        </method>
    </resource_type>
</application>

  val shardXPath2NoRemoveDups = Validator(sharedXPathWADL2, TestConfig(false, false, true, true, true, 1, true, true, false, "Xalan", false))
  val shardXPath2NoDups = Validator(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", false))
  val shardXPath2NoDupsJoinXPath = Validator(sharedXPathWADL2, TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true))
  val shardXPath2NoDupsJoinXPathMethodLabels = Validator(sharedXPathWADL2, {
    val tc=TestConfig(true, false, true, true, true, 1, true, true, false, "Xalan", true)
    tc.preserveMethodLabels = true
    tc
  })

  val good_bar = <bar xmlns="http://www.rackspace.com/foo/bar" junk="true"/>
  val good_foo = <foo xmlns="http://www.rackspace.com/foo/bar" junk="true"/>
  val bad_bar  = <bar xmlns="http://www.rackspace.com/foo/bar" />
  val bad_foo  = <foo xmlns="http://www.rackspace.com/foo/bar" />
  val bad_foo_bar = <just bad="ture"/>

  test("POST of good_bar should work on y on shardXPath2NoRemoveDups") {
    shardXPath2NoRemoveDups.validate(request("POST", "y", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on y on shardXPath2NoDups") {
    shardXPath2NoDups.validate(request("POST", "y", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on y on shardXPath2NoDupsJoinXPath") {
    shardXPath2NoDupsJoinXPath.validate(request("POST", "y", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on y on shardXPath2NoDupsJoinXPathMethodLabels") {
    shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "y", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on x on shardXPath2NoRemoveDups") {
    shardXPath2NoRemoveDups.validate(request("POST", "x", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on x on shardXPath2NoDups") {
    shardXPath2NoDups.validate(request("POST", "x", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on x on shardXPath2NoDupsJoinXPath") {
    shardXPath2NoDupsJoinXPath.validate(request("POST", "x", "application/xml", good_bar), response, chain)
  }

  test("POST of good_bar should work on x on shardXPath2NoDupsJoinXPathMethodLabels") {
    shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "x", "application/xml", good_bar), response, chain)
  }

  test("POST of good_foo should work on x on shardXPath2NoRemoveDups") {
    shardXPath2NoRemoveDups.validate(request("POST", "x", "application/xml", good_foo), response, chain)
  }

  test("POST of good_foo should work on x on shardXPath2NoDups") {
    shardXPath2NoDups.validate(request("POST", "x", "application/xml", good_foo), response, chain)
  }

  test("POST of good_foo should work on x on shardXPath2NoDupsJoinXPath") {
    shardXPath2NoDupsJoinXPath.validate(request("POST", "x", "application/xml", good_foo), response, chain)
  }

  test("POST of good_foo should work on x on shardXPath2NoDupsJoinXPathMethodLabels") {
      shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "x", "application/xml", good_foo), response, chain)
  }

  test("POST of good_foo on y should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "y", "application/xml",
                                                                good_foo), response, chain), 400)
  }

  test("POST of good_foo on y should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "y", "application/xml",
                                                          good_foo), response, chain), 400)
  }

  test("POST of good_foo on y should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "y", "application/xml",
                                                          good_foo), response, chain), 400)
  }

  test("POST of good_foo on y should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "y", "application/xml",
                                                          good_foo), response, chain), 400)
  }


  test("POST of bad_bar on y should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "y", "application/xml",
                                                                bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on y should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "y", "application/xml",
                                                          bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on y should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "y", "application/xml",
                                                          bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on y should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "y", "application/xml",
                                                          bad_bar), response, chain), 400)
  }

  test("POST of bad_foo on y should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "y", "application/xml",
                                                                bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on y should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "y", "application/xml",
                                                          bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on y should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "y", "application/xml",
                                                          bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on y should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "y", "application/xml",
                                                          bad_foo), response, chain), 400)
  }


  test("POST of bad_foo_bar on y should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "y", "application/xml",
                                                                bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on y should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "y", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on y should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "y", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on y should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "y", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }


  test("POST of bad_bar on x should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "x", "application/xml",
                                                                bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on x should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "x", "application/xml",
                                                          bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on x should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "x", "application/xml",
                                                          bad_bar), response, chain), 400)
  }

  test("POST of bad_bar on x should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "x", "application/xml",
                                                          bad_bar), response, chain), 400)
  }


  test("POST of bad_foo on x should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "x", "application/xml",
                                                                bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on x should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "x", "application/xml",
                                                          bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on x should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "x", "application/xml",
                                                          bad_foo), response, chain), 400)
  }

  test("POST of bad_foo on x should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "x", "application/xml",
                                                          bad_foo), response, chain), 400)
  }


  test("POST of bad_foo_bar on x should fail on shardXPath2NoRemoveDups") {
    assertResultFailed(shardXPath2NoRemoveDups.validate(request("POST", "x", "application/xml",
                                                                bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on x should fail on shardXPath2NoDups") {
    assertResultFailed(shardXPath2NoDups.validate(request("POST", "x", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on x should fail on shardXPath2NoDupsJoinXPath") {
    assertResultFailed(shardXPath2NoDupsJoinXPath.validate(request("POST", "x", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }

  test("POST of bad_foo_bar on x should fail on shardXPath2NoDupsJoinXPathMethodLabels") {
    assertResultFailed(shardXPath2NoDupsJoinXPathMethodLabels.validate(request("POST", "x", "application/xml",
                                                          bad_foo_bar), response, chain), 400)
  }
}
