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

import com.rackspace.com.papi.components.checker.{LogAssertions, TestConfig}
import org.apache.logging.log4j.Level
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WADLCheckerCompSpecSaxonEE extends BaseCheckerSpec with LogAssertions {
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
    c.xsdEngine="SaxonEE"
    c
  }

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
                             <step id="S0" type="START" next="F1 notFoo noMETHOD"/>
                             <step id="F1" type="URL" match="foo" next="G1 noURL notGET"/>
                             <step id="G1" type="METHOD" match="GET" label="GET Foo" next="A"/>
                             <step id="A" type="ACCEPT" priority="19999"/>
                             <step id="notFoo" type="URL_FAIL" notMatch="foo" />
                             <step id="notGET" type="METHOD_FAIL" notMatch="GET" />
                             <step id="noMETHOD" type="METHOD_FAIL" />
                             <step id="noURL" type="URL_FAIL" />
                         </checker>

  scenario ("Load a plain checker document that ends with .checker extension (check SaxonEE)") {
    Given("a plain checker document that ends in .checker")
    val in = ("foo.checker", fooGETChecker)
      When("the document is loaded...")
    val goodCheckerLog = log (Level.DEBUG) {
      val checker = builder.build (in,config)
      assert(checker, "/chk:checker/chk:step[@type='START']/@next = 'F1 notFoo noMETHOD'")
      assert(checker, "/chk:checker/chk:step[@type='URL']/@match = 'foo'")
      assert(checker, "/chk:checker/chk:step[@type='URL']/@next = 'G1 noURL notGET'")
      assert(checker, "/chk:checker/chk:step[@type='METHOD']/@next = 'A'")
      assert(checker, "/chk:checker/chk:step[@type='METHOD']/@match = 'GET'")
      assert(checker, "/chk:checker/chk:step[@type='ACCEPT']")
      assert(checker, "/chk:checker/chk:step[@type='URL_FAIL']/@notMatch='foo'")
      assert(checker, "/chk:checker/chk:step[@type='URL_FAIL' and not(@notMatch)]")
      assert(checker, "/chk:checker/chk:step[@type='METHOD_FAIL']/@notMatch='GET'")
      assert(checker, "/chk:checker/chk:step[@type='METHOD_FAIL' and not(@notMatch)]")
    }
    Then ("An appropriate DEBUG messages should be emmited.")
    assert(goodCheckerLog,"Using SaxonEE for checker validation")
  }
}
