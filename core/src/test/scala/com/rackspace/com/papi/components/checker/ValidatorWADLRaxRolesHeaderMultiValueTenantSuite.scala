/***
 *   Copyright 2018 Rackspace US, Inc.
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

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import com.rackspace.com.papi.components.checker.step.results.Result
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidatorWADLRaxRolesHeaderMultiValueTenantSuite extends ValidatorWADLRaxRolesHeaderTenantBase with VaryTestSuite {
  //
  // Test WADLs
  //
  val headerTenant : TestWADL = ("Header Multi Tenant (Header)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
            </resource>
        </resource>
    </resources>
      </application>)

  val headerAllTenant : TestWADL = ("Header Multi Tenant (ALL)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true"/>
            <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                   repeating="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
          </resource>
        </resource>
    </resources>
      </application>)

  val headerAnyTenant : TestWADL = ("Header Multi Tenant (ANY)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true" rax:anyMatch="true"/>
            <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                   repeating="true" rax:anyMatch="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
            </resource>
        </resource>
    </resources>
      </application>)

   val headerTenantExplicit : TestWADL = ("Header Multi Tenant (Header, Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true" rax:isTenant="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
            </resource>
        </resource>
    </resources>
      </application>)

  val headerAllTenantExplicit : TestWADL = ("Header Multi Tenant (ALL, Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true" rax:isTenant="true"/>
            <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                   repeating="true" rax:isTenant="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
            </resource>
        </resource>
    </resources>
      </application>)

  val headerAnyTenantExplicit : TestWADL = ("Header Multi Tenant (ANY, Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource"
                  rax:roles="a:admin/{X-TENANT}">
            <param name="X-TENANT" style="header" required="true" type="xsd:int"
                   repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
            <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                   repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}"/>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}"/>
            <method name="DELETE"/>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}"/>
                <method name="GET"  rax:roles="#all"/>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}"/>
                <method name="DELETE"/>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}"/>
            </resource>
        </resource>
    </resources>
      </application>)

  val headerAtMethodTenant : TestWADL = ("Header Tenant in a method",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                    </request>
                </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
      </application>)

  val headerAllAtMethodTenant : TestWADL = ("Header All Tenant in a method",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true"/>

                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true"/>
                    </request>
                  </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
                  </application>)

  val headerAnyAtMethodTenant : TestWADL = ("Header Any Tenant in a method",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true"/>

                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:anyMatch="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true"/>
                  </request>
                </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:anyMatch="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
      </application>)

  val headerAtMethodTenantExplicit : TestWADL = ("Header Tenant in a method (Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
      </application>)

  val headerAllAtMethodTenantExplicit : TestWADL = ("Header All Tenant in a method (Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:isTenant="true"/>

                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:isTenant="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                  </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:isTenant="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
                  </application>)

  val headerAnyAtMethodTenantExplicit : TestWADL = ("Header Any Tenant in a method (Explicit)",
    <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema">
      <resources base="https://test.api.openstack.com">
        <resource path="/v1/resource" rax:roles="a:admin/{X-TENANT}">
            <param name="X-OTHER" style="header" required="true" type="xsd:string"
                   repeating="false"/>
            <method name="POST" rax:roles="a:creator/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="GET"  rax:roles="a:observer/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>

                </request>
            </method>
            <method name="PUT" rax:roles="a:updater/{X-TENANT}">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                </request>
            </method>
            <method name="DELETE">
                <request>
                    <param name="X-TENANT" style="header" required="true" type="xsd:int"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                           repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                </request>
            </method>
            <resource path="other">
                <method name="POST" rax:roles="a:creator/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="GET"  rax:roles="#all">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="PUT"  rax:roles="a:updater/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    </request>
                </method>
                <method name="DELETE">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    </request>
                  </method>
                <method name="PATCH" rax:roles="role&#xA0;with&#xA0;spaces/{X-TENANT} a:patcher/{X-TENANT}">
                    <request>
                        <param name="X-TENANT" style="header" required="true" type="xsd:int"
                              repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                        <param name="X-TENANT" style="header" required="true" type="xsd:boolean"
                               repeating="true" rax:anyMatch="true" rax:isTenant="true"/>
                    </request>
                </method>
            </resource>
        </resource>
      </resources>
      </application>)

  //
  // Suites
  //


  //
  //  These should fail regardless of configs they are simple sanity
  //  tests on the validator.
  //

  def sanity(desc : String, validator : Validator) : Unit = {
    test(s"$desc : Should fail with a 405 on patch /v1/resource") {
      assertResultFailed(validator.validate(request("PATCH","/v1/resource", null, "", false,
        Map("X-TENANT"->List("1", "5"),
            "X-OTHER"->List("other"))), response, chain), 405)
    }

    test(s"$desc : Should fail with 404 on GET /v2/resoruce"){
      assertResultFailed(validator.validate(request("GET","/v2/resource", null, "", false,
        Map("X-TENANT"->List("1", "5"),
            "X-OTHER"->List("other"))), response, chain), 404)
    }
  }

  def sanityHeader(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url when appropriate headers, but a boolean tenant"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("1", "false")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          400, List("X-TENANT"))
      }
    })
  }

  def sanityAnyHeader(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "false" : ["a:admin"],
         "booga" : ["a:admin"],
         "wooga" : ["a:admin"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url when appropriate headers, but booga, wooga as tenants"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("booga", "wooga")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should succeed on $method $url on AnyMatch tenant when appropriate headers are set (tenants 1, booga, wooga)"){
        validateHeaderRequest(validator,r, Some(List("1", "booga", "wooga")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
      }

      test(s"$desc : Should succeed on $method $url on AnyMatch tenant when appropriate headers are set (tenants false, booga, wooga)"){
        validateHeaderRequest(validator,r, Some(List("false", "booga", "wooga")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
      }
    })
  }

  def sanityAllHeader(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "false" : ["a:admin"],
         "booga" : ["a:admin"],
         "wooga" : ["a:admin"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url on AllMatch when appropriate headers, but booga, wooga as tenants"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("booga", "wooga")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should fail on $method $url on AllMatch tenant when appropriate headers are set (tenants 1, booga, wooga)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("1", "booga", "wooga")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          400, List("X-TENANT"))
      }

      test(s"$desc : Should succed on $method $url on AllMatch tenant when appropriate headers are set (tenants false, 1)"){
        validateHeaderRequest(validator,r, Some(List("false", "1")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue)))
      }
    })
  }

  def happyWhenRaxRolesIsEnabledMultiHeader(desc : String, validator : Validator) : Unit = {
    val mapHeaderValue = b64Encode("""
      {
         "1" : ["a:admin","foo","bar"],
         "2" : ["a:creator", "foo", "a:observer"],
         "3" : ["a:updater", "bar", "biz", "a:creator"],
         "4" : ["a:observer"],
         "5" : ["a:admin", "bar", "biz", "a:creator"],
         "6" : ["biz", "baz"],
         "7" : ["role with spaces", "biz"],
         "8" : ["a:patcher"]
      }
    """)

    AllRequests.foreach (r => {
      val method = r._1
      val url = r._2

      //
      //  All requests should pass if headers are correctly set and
      //  all tenants map to admin, they should all set a Releveant
      //  role of a:admin. Except AnyRequest which passes all roles
      //  through as relevant roles.
      //
      r match {
        case r : Request if (AnyRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenants 1, 5 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), Some(List("foo", "bar")))
          }
        case _ =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenants 1, 5 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), Some(List("a:admin/{X-TENANT}")))
          }
      }

      //
      //  All requests should fail if there is a mismatch with the
      //  tenant. Except AnyRequests which are open to the world.
      //
      r match {
        case r : Request if (AnyRequests.contains(r)) =>
          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenants 1, 5, 7 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5", "7")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), Some(List("foo", "bar")))
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenants 1, 5, 6 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5", "6")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), Some(List("foo", "bar")))
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (no role info)"){
            validateHeaderRequest(validator,r, Some(List("5", "1")), Some(List("other")), None, None)
          }

        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but there's no tenant access (tenant, 1, 5, 7 selected)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("1", "5", "7")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }

          test(s"$desc : Should fail on $method $url when appropriate headers are set but there's no tenant access (tenant, 1, 5, 6 selected)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("1", "5", "6")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }

          test(s"$desc : Should fail on $method $url when appropriate headers are set but there's no tenant access (no role info)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("5", "1")), Some(List("other")), None, None),
              403)
          }
      }

      //
      //  Multi tenant match of observer should only succeed on
      //  observer (and Any) match
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || ObserverRequests.contains(r)) =>
          val rroles = {
            if (AnyRequests.contains(r)) {
              Some(List("foo","bar"))
            } else {
              Some(List("a:observer/{X-TENANT}"))
            }
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 4 selected)"){
            validateHeaderRequest(validator,r, Some(List("4")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 4, 2 selected)"){
            validateHeaderRequest(validator,r, Some(List("4", "2")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

        case _ =>
          test(s"$desc : Should fail on $method $url when appropriate headers are set but it's an observer only tenant"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("4")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }

          test(s"$desc : Should fail on $method $url when appropriate headers are set but it's an observer only tenant (tenant 4, 2 selected)"){
            assertResultFailed(
              validateHeaderRequest(validator,r, Some(List("4", "2")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
              403)
          }
      }

      //
      //  Multi tenant match of creator should succeed
      //  creator, admin, and AnyMatch.
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || CreatorRequests.contains(r)) =>
          val rroles = {
            if (AnyRequests.contains(r)) {
              Some(List("foo","bar"))
            } else {
              Some(List("a:creator/{X-TENANT}", "a:admin/{X-TENANT}"))
            }
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 1, 2, 3 selected)"){
            validateHeaderRequest(validator,r, Some(List("1","2","3")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 5, 2 selected)"){
            validateHeaderRequest(validator,r, Some(List("5", "2")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }
        case _ => /* Ignore */
      }

      //
      //  Multi tenant match of updater should succeed
      //  updater, admin, and AnyMatch.
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || UpdaterRequests.contains(r)) =>
          val rroles = {
            if (AnyRequests.contains(r)) {
              Some(List("foo","bar"))
            } else {
              Some(List("a:updater/{X-TENANT}", "a:admin/{X-TENANT}"))
            }
          }

          val rroles2 = {
            if (AnyRequests.contains(r)) {
              Some(List("foo","bar"))
            } else {
              Some(List("a:admin/{X-TENANT}"))
            }
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 1, 3 selected)"){
            validateHeaderRequest(validator,r, Some(List("1","3")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 5, 3 selected)"){
            validateHeaderRequest(validator,r, Some(List("5", "3")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (tenant 1, 5 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles2)
          }
        case _ => /* Ignore */
      }

      //
      // Admin only requests should succeed only with Adimn tenants.
      //
      r match {
        case r : Request if (AnyRequests.contains(r) || AdminOnlyRequests.contains(r)) =>
          val rroles = {
            if (AnyRequests.contains(r)) {
              Some(List("foo","bar"))
            } else {
              Some(List("a:admin/{X-TENANT}"))
            }
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (adimn only tenant 1 selected)"){
            validateHeaderRequest(validator,r, Some(List("1")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

          test(s"$desc : Should succeed on $method $url when appropriate headers are set (admin only tenant 1, 5 selected)"){
            validateHeaderRequest(validator,r, Some(List("1", "5")), Some(List("other")), Some(List("foo","bar")),
              Some(List(mapHeaderValue)), rroles)
          }

        case _ => /* Ignore */
      }


    })

    //
    // On Creator only requests, tenants with non-creator role, should
    // fail.
    //
    CreatorRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url when appropriate headers are set but a tenant contains observer only"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("4","2")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }

      test(s"$desc : Should fail on $method $url when appropriate headers are set but there is a tenant role mismatch"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("6","3")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }
    })

    //
    // On Updater only requests, tenants with non-updater role, should
    // fail.
    //
    UpdaterRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url when appropriate headers are set but a tenant contains observer only (4, 3)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("4","3")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }

      test(s"$desc : Should fail on $method $url when appropriate headers are set but there is a tenant role mismatch (2, 1)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("2","1")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }
    })

    //
    // On AdminOnly only requests, tenants with non-admin role, should
    // fail.
    //
    AdminOnlyRequests.foreach (r => {
      val method = r._1
      val url = r._2

      test(s"$desc : Should fail on $method $url when appropriate headers are set but a tenant contains observer only (4, 1)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("4","1")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }

      test(s"$desc : Should fail on $method $url when appropriate headers are set but there is a tenant role mismatch (5, 2)"){
        assertResultFailed(
          validateHeaderRequest(validator,r, Some(List("5","2")), Some(List("other")), Some(List("foo","bar")), Some(List(mapHeaderValue))),
          403)
      }
    })

    //
    //  Special case, roles with spaces
    //
    test(s"$desc : Should succeed on PATCH in /v1/resource/other if a role with a space is specified") {
      validateHeaderRequest(validator,("PATCH", "/v1/resource/other"), Some(List("7")), Some(List("other")), Some(List("foo","bar")),
        Some(List(mapHeaderValue)), Some(List("role with spaces/{X-TENANT}")))
    }

    test(s"$desc : Should succeed on PATCH in /v1/resource/other if a role with a:patcher is specified") {
      validateHeaderRequest(validator,("PATCH", "/v1/resource/other"), Some(List("7", "8")), Some(List("other")), Some(List("foo","bar")),
        Some(List(mapHeaderValue)), Some(List("role with spaces/{X-TENANT}","a:patcher/{X-TENANT}")))
    }

    test(s"$desc : Should fail on PATCH in /v1/resource/other if a tenant without an appropriate role is specified") {
      assertResultFailed(
        validateHeaderRequest(validator,("PATCH", "/v1/resource/other"), Some(List("7", "8", "4")), Some(List("other")), Some(List("foo","bar")),
          Some(List(mapHeaderValue)), None), 403)
    }

  }

  //
  // Run testcases
  //
  val disabledHeaderTestCase : TestCase = (
    List(headerTenant, headerAllTenant, headerAnyTenant),  // WADLs
    List(raxRolesDisabled, raxRolesDisabledRemoveDups),    // Configs
    List(sanity)                                           // Suites
  )
  run(disabledHeaderTestCase)

  val enabledMultiHeaderSanityTestCase : TestCase = (
    List(headerTenant, headerTenantExplicit,
      headerAtMethodTenant,
      headerAtMethodTenantExplicit),                      // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),          // Configs
    List(sanityHeader)                                    // Suites
  )
  run(enabledMultiHeaderSanityTestCase)

  val enabledMultiHeaderAnyTestCase : TestCase = (
    List(headerAnyTenant,
      headerAnyTenantExplicit,
      headerAnyAtMethodTenant,
      headerAnyAtMethodTenantExplicit),                   // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),          // Configs
    List(sanityAnyHeader)                                 // Suites
  )
  run(enabledMultiHeaderAnyTestCase)

  val enabledMultiHeaderAllTestCase : TestCase = (
    List(headerAllTenant,
      headerAllTenantExplicit,
      headerAllAtMethodTenant,
      headerAllAtMethodTenantExplicit),                   // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),          // Configs
    List(sanityAllHeader)                                 // Suites
  )
  run(enabledMultiHeaderAllTestCase)

  val enabledMultiHeaderTestCase : TestCase = (
    List(headerTenant, headerAllTenant, headerAnyTenant,
      headerTenantExplicit,
      headerAllTenantExplicit,
      headerAnyTenantExplicit,
      headerAtMethodTenant,
      headerAllAtMethodTenant,
      headerAnyAtMethodTenant,
      headerAtMethodTenantExplicit,
      headerAnyAtMethodTenantExplicit,
      headerAllAtMethodTenantExplicit),                   // WADLs
    List(raxRolesEnabled, raxRolesEnabledRemoveDups,
      raxRolesEnabledIsTenantEnabled,
      raxRolesEnabledIsTenantEnabledRemoveDups),          // Configs
    List(sanity, happyWhenRaxRolesIsEnabledMultiHeader)   // Suites
  )
  run(enabledMultiHeaderTestCase)

}
