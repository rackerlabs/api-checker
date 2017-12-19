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

import com.rackspace.cloud.api.wadl.Converters._

import java.io.File
import scala.xml._

trait VaryTestSuite {
  type TestWADL     = (String, NodeSeq) /* Descrption, WADL */
  type TestWADLList = List[TestWADL]    /* A list of test wadls */
  type CaseConfig   = (String, Config)  /* Description, TestConfig */
  type ConfigList   = List[CaseConfig]  /* A list of test configs */
  type Suite        = (String, Validator) => Unit  /* A Function that runs tests, given a validator and a description */
  type SuiteList    = List[Suite]        /* A list of tests */

  type TestCase = (TestWADLList, ConfigList, SuiteList)

  val localVaryWADLURI = (new File(System.getProperty("user.dir"),"myvarywadl.wadl")).toURI.toString

  def run(t : TestCase) : Unit = {
    val testWADLList   : TestWADLList = t._1
    val configList : ConfigList = t._2
    val suiteList : SuiteList = t._3

    testWADLList.foreach (w => {
      configList.foreach ( c => {
        val validator = Validator((localVaryWADLURI, w._2), c._2)
        suiteList.foreach (t => {
          t(w._1+" : "+c._1, validator)
        })
      })
    })
  }
}
