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

import com.rackspace.com.papi.components.checker.{LogAssertions, TestConfig}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class WADLCheckerRaxRepStepSpec extends BaseCheckerSpec with LogAssertions {
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
  val raxRepDisabled = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = false
    tc.removeDups = false
    tc.checkPlainParams = false
    tc.checkElements = false
    tc
  }

  val raxRepEnabled = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = false
    tc.checkPlainParams = true
    tc.checkElements = true
    tc
  }

  val raxRepEnabledRemoveDups = {
    val tc = TestConfig()
    tc.enableRaxRepresentationExtension = true
    tc.removeDups = true
    tc.checkPlainParams = true
    tc.checkElements = true
    tc
  }


  //
  //  WADLs...
  //
  val raxRepAtRepresentationLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2"/>
                    </representation>
                </request>
            </method>
        </resource>
    </resources>
  </application>

  val raxRepAtRepresentationMultiLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a first name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
        </resource>
    </resources>
  </application>

  val raxRepAtRepresentationMultiLevelAndMethodLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
           </method>
        </resource>
    </resources>
  </application>

  val raxRepAtRepresentationMultiLevelAndMethodResourceLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
            </method>
            <resource path="b">
                <method name="PUT">
                    <request>
                        <representation mediaType="application/xml" element="tst:other_xml"/>
                        <representation mediaType="application/json" />
                        <representation mediaType="text/yaml"/>
                    </request>
                </method>
            </resource>
            <rax:representation mediaType="application/xml" name="xmlUser"
                                path="req:header('X-XML-USER')" element="tst:user">
                <param name="test5" style="plain"
                       path="tst:user/@firstName" required="true"
                       rax:message="Need a first name"
                       rax:code="403"/>
                <param name="test6" style="plain"
                       path="tst:user/@lastName" required="true"
                       rax:message="Need a last name"
                       rax:code="403"/>
            </rax:representation>
        </resource>
    </resources>
  </application>


  val raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenFalse =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
            </method>
            <resource path="b">
                <method name="PUT">
                    <request>
                        <representation mediaType="application/xml" element="tst:other_xml"/>
                        <representation mediaType="application/json" />
                        <representation mediaType="text/yaml"/>
                    </request>
                </method>
            </resource>
            <rax:representation mediaType="application/xml" name="xmlUser"
                                path="req:header('X-XML-USER')" element="tst:user"
                                applyToChildren="false">
                <param name="test5" style="plain"
                       path="tst:user/@firstName" required="true"
                       rax:message="Need a first name"
                       rax:code="403"/>
                <param name="test6" style="plain"
                       path="tst:user/@lastName" required="true"
                       rax:message="Need a last name"
                       rax:code="403"/>
            </rax:representation>
        </resource>
    </resources>
  </application>

  val raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenTrue =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
            </method>
            <resource path="b">
                <method name="PUT">
                    <request>
                        <representation mediaType="application/xml" element="tst:other_xml"/>
                        <representation mediaType="application/json" />
                        <representation mediaType="text/yaml"/>
                    </request>
                </method>
            </resource>
            <rax:representation mediaType="application/xml" name="xmlUser"
                                path="req:header('X-XML-USER')" element="tst:user"
                                applyToChildren="true">
                <param name="test5" style="plain"
                       path="tst:user/@firstName" required="true"
                       rax:message="Need a first name"
                       rax:code="403"/>
                <param name="test6" style="plain"
                       path="tst:user/@lastName" required="true"
                       rax:message="Need a last name"
                       rax:code="403"/>
            </rax:representation>
        </resource>
    </resources>
  </application>


  val raxRepAtRepresentationMultiLevelAndMethodResourceAndResourcesLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
            </method>
            <resource path="b">
                <method name="PUT">
                    <request>
                        <representation mediaType="application/xml" element="tst:other_xml"/>
                        <representation mediaType="application/json" />
                        <representation mediaType="text/yaml"/>
                    </request>
                </method>
            </resource>
            <rax:representation mediaType="application/xml" name="xmlUser"
                                path="req:header('X-XML-USER')" element="tst:user">
                <param name="test5" style="plain"
                       path="tst:user/@firstName" required="true"
                       rax:message="Need a first name"
                       rax:code="403"/>
                <param name="test6" style="plain"
                       path="tst:user/@lastName" required="true"
                       rax:message="Need a last name"
                       rax:code="403"/>
            </rax:representation>
        </resource>
        <rax:representation mediaType="application/xml" name="xmlUser"
                            path="req:header('X-XML-USER2')" element="tst:user2">
            <param name="test5" style="plain"
                   path="tst:user2/@fn" required="true"
                   rax:message="Need a first name"
                   rax:code="403"/>
            <param name="test6" style="plain"
                   path="tst:user2/@ln" required="true"
                   rax:message="Need a last name"
                   rax:code="403"/>
        </rax:representation>
    </resources>
  </application>


  val raxRepAtRepresentationMultiLevelAndMethodResourceApplyChildrenTrueAndResourcesLevel =
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:tst="test.org">
      <resources base="https://test.api.openstack.com">
        <resource path="/a">
            <method name="PUT">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml">
                        <rax:representation
                            mediaType="application/myApp+json"
                            path="/tst:some_xml/tst:json"
                            name="jsonContent">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                         </rax:representation>
                         <rax:representation
                            mediaType="application/myApp+json"
                            path="/tst:some_xml/tst:json2"
                            name="jsonContent2">
                            <param name="test" style="plain"
                                   path="$_?firstName" required="true"
                                   rax:message="Need a first name" rax:code="403"/>
                        </rax:representation>

                    </representation>
                    <representation mediaType="application/json">
                        <param name="test" style="plain"
                              path="$_?firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                        <rax:representation
                            mediaType="application/myApp+xml"
                            path="$body('xml')"
                            name="xmlContent" element="tst2:user" xmlns:tst2="test.org/2">
                          <rax:representation
                              mediaType="application/myApp+json"
                              path="/tst2:user/tst2:json">
                               <param name="test3" style="plain"
                                 path="$_?lastName" required="true"
                                 rax:message="Need a last name" rax:code="403"/>
                               />
                          </rax:representation>
                        </rax:representation>
                    </representation>
                </request>
            </method>
            <method name="POST">
                <request>
                    <representation mediaType="application/xml" element="tst:some_xml"/>
                    <representation mediaType="application/json" />
                    <representation mediaType="text/yaml"/>
                    <rax:representation mediaType="application/myApp+json" name="jsonUser"
                                        path="req:header('X-JSON-USER')">
                        <param name="test3" style="plain"
                               path="$_?firstName" required="true"
                               rax:message="Need a first name"
                               rax:code="403"/>
                        <param name="test4" style="plain"
                               path="$_?lastName" required="true"
                               rax:message="Need a last name" rax:code="403"/>
                    </rax:representation>
                </request>
            </method>
            <resource path="b">
                <method name="PUT">
                    <request>
                        <representation mediaType="application/xml" element="tst:other_xml"/>
                        <representation mediaType="application/json" />
                        <representation mediaType="text/yaml"/>
                    </request>
                </method>
            </resource>
            <rax:representation mediaType="application/MyApp+xml" name="xmlUser"
                                path="req:header('X-XML-USER')" element="tst:user"
                                applyToChildren="true">
                <param name="test5" style="plain"
                       path="tst:user/@firstName" required="true"
                       rax:message="Need a first name"
                       rax:code="403"/>
                <param name="test6" style="plain"
                       path="tst:user/@lastName" required="true"
                       rax:message="Need a last name"
                       rax:code="403"/>
            </rax:representation>
        </resource>
        <rax:representation mediaType="application/myApp+xml" name="xmlUser"
                            path="req:header('X-XML-USER2')" element="tst:user2">
            <param name="test5" style="plain"
                   path="tst:user2/@fn" required="true"
                   rax:message="Need a first name"
                   rax:code="403"/>
            <param name="test6" style="plain"
                   path="tst:user2/@ln" required="true"
                   rax:message="Need a last name"
                   rax:code="403"/>
        </rax:representation>
    </resources>
  </application>


  feature ("The WADLCheckerBuilder can correctly transform a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references rax:representation extensions into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process validate representations in different areas of the request")

    scenario ("The WADL contains a misplaced rax:representation") {
      Given("A WADL with a misplaced rax:representation")
      val inWADL = <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
         <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                    <representation mediaType="application/xml"/>
                    <representation mediaType="application/json"/>
                  </request>
                  <!-- Notice the representation at method level, but not request level -->
                  <rax:representation
                       mediaType="application/xml"
                       path="$body('xml')"
                       name="xmlContent">
                       <param name="test" style="plain"
                              path="/firstName" required="true"
                              rax:message="Need a first name" rax:code="403"/>
                  </rax:representation>
               </method>
            </resource>
           </resources>
          </application>

      When("the wadl is translated")
      val checkerLog = log (Level.WARN) {
        val checker = builder.build (inWADL, raxRepEnabled)
        Then("No push or pop reqs should exist in the checker format")
        assert(checker,"count(/chk:checker/chk:step[@type=('PUSH_XML_REP','PUSH_JSON_REP','POP_REP')]) = 0")
      }
      And ("There should be a warning in the log that denotes that denotes the bad placement")
      assert(checkerLog, "bad placement for <rax:representation")
    }

    scenario ("The WADL contains a rax:representation with unsupported media type") {
      Given("A WADL with an unsupported media type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
            <resources base="https://test.api.openstack.com">
                <resource path="/a/b">
                    <method name="POST">
                        <request>
                            <representation mediaType="application/xml"/>
                            <representation mediaType="application/json"/>
                            <!--
                                Note that YAML is not a supported mediaType!
                            -->
                            <rax:representation
                                mediaType="text/yaml"
                                path="$body('xml')"
                                name="xmlContent">
                                <param name="test" style="plain"
                                       path="/firstName" required="true"
                                       rax:message="Need a first name" rax:code="403"/>
                            </rax:representation>
                        </request>
                    </method>
                </resource>
            </resources>
        </application>

      When("the wadl is translated")
      val checkerLog = log (Level.ERROR) {
        Then("A WADLException is thrown")
        intercept[WADLException] {
          val checker = builder.build (inWADL, raxRepEnabled)
        }
      }
      And ("There should be an error in the log that denotes that the mediatype is not supported")
      assert(checkerLog, "Only XML and JSON mediaTypes are allowed")
    }

    scenario ("The WADL contains rax:rep at the representation level (rax:representation disabled)") {
      Given("A WADL with rax:representation at the representation level, but rax:representation disabled")
      val inWADL = raxRepAtRepresentationLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepDisabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type=('PUSH_XML_REP','PUSH_JSON_REP','POP_REP')]) = 0")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), Accept)
    }

    scenario ("The WADL contains rax:rep at the representation level (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation level, but rax:representation enabled")
      val inWADL = raxRepAtRepresentationLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 2")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst:')]) = 'tst'")
      assert(checker, "namespace-uri-for-prefix('tst', /chk:checker/chk:step[contains(@match,'tst:')]) = 'test.org'")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst2:')]) = 'tst2'")
      assert(checker, "namespace-uri-for-prefix('tst2', /chk:checker/chk:step[contains(@match,'tst2:')]) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation level (rax:representation enabled, removeDups enabled)") {
      Given("A WADL with rax:representation at the representation level, but rax:representation enabled, removeDups enabled")
      val inWADL = raxRepAtRepresentationLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 2")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 1")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst:')]) = 'tst'")
      assert(checker, "namespace-uri-for-prefix('tst', /chk:checker/chk:step[contains(@match,'tst:')]) = 'test.org'")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst2:')]) = 'tst2'")
      assert(checker, "namespace-uri-for-prefix('tst2', /chk:checker/chk:step[contains(@match,'tst2:')]) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 3")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 3")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst:')]) = 'tst'")
      assert(checker, "namespace-uri-for-prefix('tst', /chk:checker/chk:step[contains(@match,'tst:')]) = 'test.org'")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst2:')]) = 'tst2'")
      assert(checker, "namespace-uri-for-prefix('tst2', /chk:checker/chk:step[contains(@match,'tst2:')]) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels (rax:representation enabled, removeDups enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, but rax:representation enabled, removeDups enabled")
      val inWADL = raxRepAtRepresentationMultiLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 3")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst:')]) = 'tst'")
      assert(checker, "namespace-uri-for-prefix('tst', /chk:checker/chk:step[contains(@match,'tst:')]) = 'test.org'")
      assert(checker, "in-scope-prefixes(/chk:checker/chk:step[contains(@match,'tst2:')]) = 'tst2'")
      assert(checker, "namespace-uri-for-prefix('tst2', /chk:checker/chk:step[contains(@match,'tst2:')]) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels and at method with multiple reps (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels and at method with multiple reps but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 6")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels and at method with multiple reps (rax:representation enabled, romoveDupsEnabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels and at method with multiple reps but rax:representation enabled, removeDupsEnabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 1")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level  but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 6")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level (rax:representation enabled, removeDups)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level  but rax:representation enabled, removeDups")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 2")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, applyChildrenFalse  (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, applyChlidrenFalse but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 6")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, applyChildrenFalse (rax:representation enabled, removeDups)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, applyChildredFalse  but rax:representation enabled, removeDups")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenFalse
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 2")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, applyChildrenTrue  (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, applyChlidrenTrue but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 9")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 9")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, applyChildrenTrue (rax:representation enabled, removeDups)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, applyChildredTrue  but rax:representation enabled, removeDups")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceLevelApplyChildrenTrue
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 2")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, and resources level  (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, and resources level but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceAndResourcesLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 14")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 9")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, and resources level  (rax:representation enabled, removeDups)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, and resources level but rax:representation enabled, removeDups")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceAndResourcesLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 3")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, and resources level applyChildren  (rax:representation enabled)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, and resources level applyChildren but rax:representation enabled")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceApplyChildrenTrueAndResourcesLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabled)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 17")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 6")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 9")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
    }

    scenario ("The WADL contains rax:rep at the representation at multiple levels, at method with multiple reps, and at the resource level, and resources level applyChildren  (rax:representation enabled, removeDups)") {
      Given("A WADL with rax:representation at the representation at multiple levels, method with multiple reps, and at the resource level, and resources level applyChildren but rax:representation enabled, removeDups")
      val inWADL = raxRepAtRepresentationMultiLevelAndMethodResourceApplyChildrenTrueAndResourcesLevel
      When("the wadl is translated")
      val checker = builder.build (inWADL, raxRepEnabledRemoveDups)
      Then ("The following assertions should hold")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_XML_REP']) = 3")
      assert(checker,"count(/chk:checker/chk:step[@type='PUSH_JSON_REP']) = 4")
      assert(checker,"count(/chk:checker/chk:step[@type='POP_REP']) = 2")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies in-scope-prefixes($s) = 'tst'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst:')] satisfies namespace-uri-for-prefix('tst', $s) = 'test.org'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies in-scope-prefixes($s) = 'tst2'")
      assert(checker, "every $s in /chk:checker/chk:step[contains(@match,'tst2:')] satisfies namespace-uri-for-prefix('tst2', $s) = 'test.org/2'")
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent","/tst:some_xml/tst:json"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonContent2","/tst:some_xml/tst:json2"),
        JsonXPath("$_?firstName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlContent", "$body('xml')"),
        XPath("/tst2:user"), RaxPushJSON("/tst2:user/tst2:json"), JsonXPath("$_?lastName"), RaxPopRep, RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, JsonXPath("$_?firstName"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushJSON("jsonUser","req:header('X-JSON-USER')"),
        JsonXPath("$_?firstName"), JsonXPath("$_?lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:some_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), Method("POST"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER2')"),
        XPath("/tst:user2"), XPath("tst:user2/@fn"), XPath("tst:user2/@ln"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/xml)(;.*)?"), WellXML, XPath("/tst:other_xml"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(application/json)(;.*)?"), WellJSON, RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
      assert(checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("(text/yaml)(;.*)?"), RaxPushXML("xmlUser","req:header('X-XML-USER')"),
        XPath("/tst:user"), XPath("tst:user/@firstName"), XPath("tst:user/@lastName"), RaxPopRep, Accept)
    }

  }
}
