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
package com.rackspace.com.papi.components.checker.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XQueryEvaluator
import net.sf.saxon.s9api.XQueryExecutable
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XdmAtomicValue

import net.sf.saxon.om.Sequence


import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool


object JSONConverter {
  private val mapper = new ObjectMapper
  private val processor   = new Processor(false)
  private val compiler    = {
    val c = processor.newXQueryCompiler()
    c.setLanguageVersion("3.1")
    c
  }
  private val executable  = compiler.compile(getClass.getResourceAsStream("/xq/load-json.xq"))
  private val pool = new SoftReferenceObjectPool[XQueryEvaluator](new JSONParseEvaluatorFactory(executable))

  /**
   * Convert a Jackson JsonNode into a Sequence that can be used by Saxon.
   *
   */
  def convert (node : JsonNode) : Sequence = {
    //
    // The process of converting a JsonNode needed by Jackson and
    // tools like the JSONSchema validator to a Sequence needed by
    // Saxon in complicated.
    //
    // There are efforts to make the process simpler, and these should
    // be released in upcomming verisons of Saxon.  While we wait, the
    // easy thing to do is to simply have Saxon reparse the JSON
    // rather than doing an actual conversion.
    //
    var evaluator : XQueryEvaluator = null;
    try {
      evaluator = pool.borrowObject()
      evaluator.setExternalVariable(new QName("__JSON__"), new XdmAtomicValue(mapper.writeValueAsString(node)))
      evaluator.evaluate().getUnderlyingValue()
    } finally {
      if (evaluator != null) pool.returnObject(evaluator)
    }
  }
}

private class JSONParseEvaluatorFactory(val executable : XQueryExecutable)  extends PoolableObjectFactory[XQueryEvaluator] {
  def makeObject = executable.load()
  def validateObject  (xe : XQueryEvaluator) : Boolean = xe != null
  def passivateObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
  def activateObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
  def destroyObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
}
