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

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.LinkedList

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import javax.xml.transform.Templates
import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer

import net.sf.saxon.Controller
import net.sf.saxon.serialize.MessageWarner

import com.yammer.metrics.core.Gauge
import com.yammer.metrics.scala.Instrumented

object IdentityTransformPool extends Instrumented  {
  //
  //  We purposly use Xalan-C for identity transform, it's fast and we
  //  avoid licence check in SaxonEE, which for some reason is always
  //  trigged by id transform.
  //
  private val tf = TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", this.getClass.getClassLoader)
  private val pool = new SoftReferenceObjectPool[Transformer](new IdentityTransformerFactory(tf))
  private val activeGauge = metrics.gauge("Active")(numActive)
  private val idleGauge = metrics.gauge("Idle")(numIdle)

  def borrowTransformer : Transformer = pool.borrowObject()
  def returnTransformer (transformer : Transformer) : Unit = pool.returnObject(transformer)
  def numActive : Int = pool.getNumActive()
  def numIdle : Int = pool.getNumIdle()
}

private class IdentityTransformerFactory(private val tf : TransformerFactory) extends PoolableObjectFactory[Transformer] {
  def makeObject = tf.newTransformer()
  def validateObject (transformer : Transformer) : Boolean = transformer != null
  def passivateObject (transformer : Transformer) : Unit = transformer.reset()
  def activateObject (transformer : Transformer) : Unit = {
    //
    // No need to activate the transformer should be ready to go.
    //
  }
  def destroyObject (transformer : Transformer) : Unit = {
    //
    //  Not needed...
    //
  }
}
