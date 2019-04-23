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
package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.com.papi.components.checker.{LogAssertions, Config}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class WADLCheckerHeaderNameSpec extends BaseCheckerSpec {

  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  val tstConfig = {
    val c = new Config
    c.checkHeaders = true
    c.removeDups = true
    c
  }

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple headers")
    info ("without worring about the case of the header name")

    scenario ("The WADL contains header single with different cases (remove dups is enabled)") {
      Given("A WADL that contains multiple header checks with the same name written in different cases")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:xs="http://www.w3.org/2001/XMLSchema">
          <resources>
        <resource path="/path/to/resource">
            <param name="X-FOO" fixed="FOO" style="header" repeating="false" required="true"/>
            <param name="X-BAR" fixed="BAR" style="header" repeating="false" required="true"/>
            <param name="X-BaR" fixed="CAR" style="header" repeating="false" required="true"/>
            <param name="X-Bar" fixed="SCAR" style="header" repeating="false" required="true"/>
            <param name="x-bar" fixed="bar" style="header" repeating="false" required="true"/>
            <param name="X-FoO" fixed="FoO" style="header" repeating="false" required="true"/>
            <method name="GET"/>
      </resource>
      </resources>
      </application>
      When ("the WADL is translated")
      val checker = builder.build(inWADL, tstConfig)
      Then ("Headers with the same name should be treated equally")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_SINGLE' and @name='X-FOO']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_SINGLE' and @name='X-FOO']/@match ='FOO|FoO'")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_SINGLE' and @name='X-BAR']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_SINGLE' and @name='X-BAR']/@match ='BAR|CAR|SCAR|bar'")
    }


    scenario ("The WADL contains header any with different cases (remove dups is enabled)") {
      Given("A WADL that contains multiple header checks with the same name written in different cases")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:rax="http://docs.rackspace.com/api"
             xmlns:xs="http://www.w3.org/2001/XMLSchema">
          <resources>
        <resource path="/path/to/resource">
            <param name="X-FOO" fixed="FOO" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <param name="X-BAR" fixed="BAR" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <param name="X-BaR" fixed="CAR" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <param name="X-Bar" fixed="SCAR" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <param name="x-bar" fixed="bar" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <param name="X-FoO" fixed="FoO" style="header" repeating="true" required="true" rax:anyMatch="true"/>
            <method name="GET"/>
      </resource>
      </resources>
      </application>
      When ("the WADL is translated")
      val checker = builder.build(inWADL, tstConfig)
      Then ("Headers with the same name should be treated equally")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_ANY' and @name='X-FOO']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_ANY' and @name='X-FOO']/@match ='FOO|FoO'")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_ANY' and @name='X-BAR']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_ANY' and @name='X-BAR']/@match ='BAR|CAR|SCAR|bar'")
    }


    scenario ("The WADL contains header all with different cases (remove dups is enabled)") {
      Given("A WADL that contains multiple header checks with the same name written in different cases")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
             xmlns:xs="http://www.w3.org/2001/XMLSchema">
          <resources>
        <resource path="/path/to/resource">
            <param name="X-FOO" fixed="FOO" style="header" repeating="true" required="true"/>
            <param name="X-BAR" fixed="BAR" style="header" repeating="true" required="true"/>
            <param name="X-BaR" fixed="CAR" style="header" repeating="true" required="true"/>
            <param name="X-Bar" fixed="SCAR" style="header" repeating="true" required="true"/>
            <param name="x-bar" fixed="bar" style="header" repeating="true" required="true"/>
            <param name="X-FoO" fixed="FoO" style="header" repeating="true" required="true"/>
            <method name="GET"/>
      </resource>
      </resources>
      </application>
      When ("the WADL is translated")
      val checker = builder.build(inWADL, tstConfig)
      Then ("Headers with the same name should be treated equally")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_ALL' and @name='X-FOO']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_ALL' and @name='X-FOO']/@matchRegEx ='FOO|FoO'")
      assert (checker, "count(/chk:checker/chk:step[@type='HEADER_ALL' and @name='X-BAR']) = 1")
      assert (checker, "/chk:checker/chk:step[@type='HEADER_ALL' and @name='X-BAR']/@matchRegEx ='BAR|CAR|SCAR|bar'")
    }

  }

}
