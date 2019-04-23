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

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

//
//   Very simple tests here.  The assertions here suck, basically
//   we're just making sure things don't blow up. Need to figure out a
//   good way of parsing dot in java in order to come up with some
//   sane assertions.
//
//   Looked at the following with varius levels of success:
//
//   1. Zest 2.x (http://wiki.eclipse.org/Zest#Zest_2.x) very cool,
//   but way too many dependecies, it's bundled as an eclipse plugin
//   which adds complexity.
//
//   2. iDot (http://code.google.com/p/idot/) very easy to use but the
//   parser is very simplistic, and I could easly break it :-(
//   Integration with prefuse is nice though, the fact that it lacks a
//   build environment of any kind blows.
//
//   3. JPGD (http://www.alexander-merz.com/graphviz/) shows a lot of
//   promise, but the parser blows up with our input.
//
//   4. gv.3java (http://www.graphviz.org/pdf/gv.3java.pdf), didn't
//   really try this one, but it may be the way to go....Looks like it
//   binds directly to the graphviz C library.
//
//   Need to invesitgate further to see what will work.
//
//

@RunWith(classOf[JUnitRunner])
class WADLDotSpec extends BaseDotSpec {
    feature ("The WADLDotBuilder can correctly transforma a WADL into dot format") {

      info ("As a developer")
      info ("I want to be able to transform a WADL which references multiple XSDs into a ")
      info ("a description of a machine that can validate the API in dot format")
      info ("so that I can visualize the machine.")

      scenario("The WADL rendered in nfa mode with sinks") {
        Given("a WADL")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
        When("the wadl is translated with nfa mode and sinks")
        val dot = builder.build (inWADL, config, false, true)
        println(dot)
        Then ("It shouldn't blow up")
      }

    scenario("The WADL rendered in nfa mode with no sinks") {
        Given("a WADL")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
        When("the wadl is translated with nfa mode and sinks")
        val dot = builder.build (inWADL, config, true, true)
        println(dot)
        Then ("It shouldn't blow up")
      }

    scenario("The WADL rendered in simple mode with sinks") {
        Given("a WADL")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
        When("the wadl is translated with nfa mode and sinks")
        val dot = builder.build (inWADL, config, false, false)
        println(dot)
        Then ("It shouldn't blow up")
      }

    scenario("The WADL rendered in simple mode with no sinks") {
        Given("a WADL")
        val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
        When("the wadl is translated with nfa mode and sinks")
        val dot = builder.build (inWADL, config, true, false)
        println(dot)
        Then ("It shouldn't blow up")
    }
  }
}
