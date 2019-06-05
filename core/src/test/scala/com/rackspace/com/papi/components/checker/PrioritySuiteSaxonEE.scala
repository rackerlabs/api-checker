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

import com.rackspace.cloud.api.wadl.test.SchemaAsserter
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

import scala.xml._

@RunWith(classOf[JUnitRunner])
class PrioritySuiteSaxonEE extends FunSuite {

  val priorityMapAsserter = new SchemaAsserter(getClass.getClassLoader.getResource("xsd/priority-map.xsd"), true)

  test("Make sure priorites validate...") {
    priorityMapAsserter.assert(XML.load(getClass.getClassLoader.getResource("xsl/priority-map.xml")))
  }
}
