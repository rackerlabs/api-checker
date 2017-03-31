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
package com.rackspace.com.papi.components.checker.util

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XQueryEvaluator
import net.sf.saxon.s9api.XQueryExecutable

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class XQueryEvaluatorPoolSuite extends FunSuite {

  private val processor   = new Processor(false)
  private val compiler    = {
    val c = processor.newXQueryCompiler()
    c.setLanguageVersion("3.1")
    c
  }
  private val query = "/xq/load-json.xq"
  private val executable  = compiler.compile(getClass.getResourceAsStream(query))


  test("The xquery pool should successfully create an xquery evaluator") {
    var evaluator : XQueryEvaluator = null
    try {
      evaluator = XQueryEvaluatorPool.borrowEvaluator(query, executable)
      assert (evaluator != null)
    }finally {
      if (evaluator != null) XQueryEvaluatorPool.returnEvaluator(query, evaluator)
    }
  }

  test("NumIdle should not be zero soon after returning an evaluator") {
    var evaluator : XQueryEvaluator = null
    try {
      evaluator = XQueryEvaluatorPool.borrowEvaluator(query, executable)
      assert (evaluator != null)
    }finally {
      if (evaluator != null) XQueryEvaluatorPool.returnEvaluator(query, evaluator)
      assert(XQueryEvaluatorPool.numIdle(query) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new evaluators") {
    val NUM_INCREASE = 5

    val initActive = XQueryEvaluatorPool.numActive(query)
    val initIdle   = XQueryEvaluatorPool.numIdle(query)

    val evaluators = new Array[XQueryEvaluator](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      evaluators(i) = XQueryEvaluatorPool.borrowEvaluator(query, executable)
    }

    assert (XQueryEvaluatorPool.numActive(query) >= initActive+NUM_INCREASE)

    val fullActive = XQueryEvaluatorPool.numActive(query)

    for (i <- 0 to NUM_INCREASE-1) {
      XQueryEvaluatorPool.returnEvaluator (query, evaluators(i))
    }

    assert (XQueryEvaluatorPool.numActive(query) <= fullActive-NUM_INCREASE)
  }
}
