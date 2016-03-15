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
package com.rackspace.com.papi.components.checker.util

import javax.xml.transform.{Templates, Transformer}

import com.codahale.metrics.{Gauge, MetricRegistry}
import net.sf.saxon.Controller
import net.sf.saxon.serialize.MessageWarner
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.{HashMap, LinkedList, Map}

object TransformPool extends Instrumented {
  private val transformPools : Map[Templates, SoftReferenceObjectPool[Transformer]] = new HashMap[Templates, SoftReferenceObjectPool[Transformer]]
  private val activeGauges = new LinkedList[Gauge[Int]]
  private val idleGauges = new LinkedList[Gauge[Int]]
  private def pool (templates : Templates) : SoftReferenceObjectPool[Transformer] = transformPools.getOrElseUpdate (templates, addPool(templates))

  private def addPool(templates : Templates) : SoftReferenceObjectPool[Transformer] = {
    val pool = new SoftReferenceObjectPool[Transformer](new XSLTransformerFactory(templates))
    val registryClassName = getRegistryClassName(getClass)
    val hash = Integer.toHexString(templates.hashCode())
    activeGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Active", hash))(pool.getNumActive)
    idleGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Idle", hash))(pool.getNumIdle)
    pool
  }

  def borrowTransformer (templates : Templates) = pool(templates).borrowObject
  def returnTransformer (templates : Templates, transformer : Transformer) = pool(templates).returnObject(transformer)
  def numActive (templates : Templates) : Int  = pool(templates).getNumActive
  def numIdle (templates : Templates) : Int = pool(templates).getNumIdle
}

private class XSLTransformerFactory(private val templates : Templates) extends PoolableObjectFactory[Transformer] {
  def makeObject = templates.newTransformer()

  def activateObject (trans : Transformer) : Unit = {
    //
    //  No need to activate a transformer
    //
  }

  def validateObject (trans : Transformer) : Boolean = {
    val valid = trans != null

    //
    //  Ask Saxon to behave like xalan when emitting messages.
    //
    if (valid && trans.isInstanceOf[Controller]) {
      trans.asInstanceOf[Controller].setMessageEmitter(new MessageWarner)
    }
    valid
  }

  def passivateObject (trans : Transformer) : Unit = {
    trans.reset()
  }

  def destroyObject (trans : Transformer) : Unit = {
    //
    //  Not needed
    //
  }
}
