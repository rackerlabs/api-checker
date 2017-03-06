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

import com.codahale.metrics.{Gauge, MetricRegistry}
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.{HashMap, MutableList, Map}

import net.sf.saxon.s9api.XQueryExecutable
import net.sf.saxon.s9api.XQueryEvaluator

object XQueryEvaluatorPool extends Instrumented {

  private val evaluators : Map[String, SoftReferenceObjectPool[XQueryEvaluator]] = new HashMap[String,
                                                                                               SoftReferenceObjectPool[XQueryEvaluator]]

  private val activeGauges = new MutableList[Gauge[Int]]
  private val idleGauges = new MutableList[Gauge[Int]]

  private def addXQueryEvaluatorPool(expression : String, exec : XQueryExecutable) : SoftReferenceObjectPool[XQueryEvaluator] = {
    val pool = new SoftReferenceObjectPool[XQueryEvaluator](new XQueryEvaluatorFactory(exec))
    val registryClassName = getRegistryClassName(getClass)
    activeGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Active", expression))(pool.getNumActive)
    idleGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Idle", expression))(pool.getNumIdle)
    pool
  }

  def borrowEvaluator (expression : String, exec : XQueryExecutable) : XQueryEvaluator = {
    evaluators.getOrElseUpdate(expression, addXQueryEvaluatorPool(expression, exec)).borrowObject
  }

  def returnEvaluator(expression : String, evaluator : XQueryEvaluator) : Unit = {
    evaluators(expression).returnObject(evaluator)
  }

  def numActive (expression : String) : Int = {
    evaluators(expression).getNumActive
  }

  def numIdle (expression : String) : Int = {
    evaluators(expression).getNumIdle
  }
}

private class XQueryEvaluatorFactory (val exec : XQueryExecutable) extends PoolableObjectFactory[XQueryEvaluator] {
  def makeObject = exec.load()
  def validateObject  (xe : XQueryEvaluator) : Boolean = xe != null
  def passivateObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
  def activateObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
  def destroyObject (xe : XQueryEvaluator) : Unit = { /* Ignore */ }
}
