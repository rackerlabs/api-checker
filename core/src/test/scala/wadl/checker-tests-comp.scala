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

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._

import org.apache.logging.log4j.Level

import com.rackspace.com.papi.components.checker.TestConfig
import com.rackspace.com.papi.components.checker.LogAssertions

//
//  Test the direct loading of checker format
//


@RunWith(classOf[JUnitRunner])
class WADLCheckerCompSpec extends BaseCheckerSpec with LogAssertions {
  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  val creatorString = {
    val title = getClass.getPackage.getImplementationTitle
    val version = getClass.getPackage.getImplementationVersion
    s"$title ($version)"
  }

  val config = {
    val c = TestConfig()
    c.validateChecker = true
    c
  }

  feature ("The WADLCheckerBuilder can directly load a document in checker format") {

    info ("As a developer")
    info ("I want to be able to load a checker document directly into memory")
    info ("so that I don't have to continually transform WADLs into checker format")
    info ("and I can get faster load times")

    //
    //  A simple checker that accepts a GET on /foo
    //
    val fooGETChecker =  <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>{creatorString}</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
                                }
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>

     scenario ("Load a plain checker document that ends with .checker extension") {
       Given("a plain checker document that ends in .checker")
       val in = ("foo.checker", fooGETChecker)
       When("the document is loaded...")
       val goodCheckerLog = log (Level.WARN) {
         val checker = builder.build (in,config)
         Then ("The checker should be loaded unchanged")
         canon(checker) should equal (canon(fooGETChecker))
       }
       And ("No WARN messages should be emmited.")
       assertEmpty(goodCheckerLog)
     }

    scenario ("Should fail to load a plain checker document that *does not* end with a .checker extension") {
      Given ("a plain checker document that does not end in .checker")
      val in = ("foo.xml", fooGETChecker)
      When("the document is loaded...")
      Then ("An error should be thrown")
      intercept[WADLException] {
        val checker = builder.build (in,config)
      }
    }

    scenario ("Load a plain checker document with a non-matching created-by section") {
       Given("a plain checker document with a non-matching created-by section")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>SomethingElse (1.1.11)</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
                                }
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val wrongVersionLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the version mismatch")
       assert(wrongVersionLog, "This checker was compiled with a different version of api-checker.")
       assert(wrongVersionLog, "The checker was compiled with: SomethingElse (1.1.11)")
       assert(wrongVersionLog, "The current version is: null (null)")
    }

    scenario ("Load a plain checker document with a malformed created-by section") {
       Given("a plain checker document with a malformed created-by section")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>Wooga Booga</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
                                }
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val badVersionLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the version mismatch and a bad version number warning")
       assert(badVersionLog, "Strange could not extract version number from: Wooga Booga")
       assert(badVersionLog, "This checker was compiled with a different version of api-checker.")
       assert(badVersionLog, "The checker was compiled with: Wooga Booga")
       assert(badVersionLog, "The current version is: null (null)")
    }

    scenario ("Load a plain checker document with missing created-by section") {
       Given("a plain checker document with missing created-by section")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
                                }
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val missingVersionLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the version mismatch")
       assert(missingVersionLog, "This checker was compiled with a different version of api-checker.")
       assert(missingVersionLog, "The checker was compiled with: Unknown")
       assert(missingVersionLog, "The current version is: null (null)")
    }

    scenario ("Load a plain checker document with an unknown config option") {
       Given("a plain checker document with an unknown config option")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>{creatorString}</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.map(c => <config option={c._1} value={c._2.toString} />)
                                }
                                <config option="foo" value="bar"/>
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val configUnkonwnLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the unknown config option")
       assert(configUnkonwnLog, "The Compiled checker has option foo set to 'bar'.")
       assert(configUnkonwnLog, "The Current checker does not have a value for foo set.")
    }

    scenario ("Load a plain checker document with a missing option") {
       Given("a plain checker document with a missing config option")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>{creatorString}</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.filter(f => f._1 != "joinXPathChecks").map(c => <config option={c._1} value={c._2.toString} />)
                                }
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val missingConfigLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the missing config option")
       assert(missingConfigLog, "The Current checker has option joinXPathChecks set to 'false'.")
       assert(missingConfigLog, "The Compiled checker does not have a value for joinXPathChecks set.")
    }

    scenario ("Load a plain checker document with a mismatch option") {
       Given("a plain checker document with a mismatch config option")
       val in = ("foo.checker", <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
                             <meta>
                                <built-by>Test</built-by>
                                <created-by>{creatorString}</created-by>
                                <created-on>2014-11-10T10:00:07.811-06:00</created-on>
                                {
                                  config.checkerMetaMap.filter(f => f._1 != "joinXPathChecks").map(c => <config option={c._1} value={c._2.toString} />)
                                }
                                <config option="joinXPathChecks" value="true"/>
                             </meta>
                             <step id="S0" type="START" next="F1"/>
                             <step id="F1" type="URL" match="foo" next="G1"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                         </checker>)
       When("the document is loaded...")
       val mismatchConfigLog = log (Level.WARN) {
         val checker = builder.build (in,config)
       }
       Then ("The checker should warn about the mismatch config option")
       assert(mismatchConfigLog, "The Current checker has option joinXPathChecks set to 'false'.")
       assert(mismatchConfigLog, "The Compiled checker has option joinXPathChecks set to 'true'.")
       assert(mismatchConfigLog, "The compiled option will take effect, the other will be ignored.")
    }
  }
}
