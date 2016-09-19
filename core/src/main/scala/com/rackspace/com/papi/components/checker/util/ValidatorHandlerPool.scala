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

import javax.xml.validation.{Schema, ValidatorHandler}

import com.codahale.metrics.{Gauge, MetricRegistry}
import com.saxonica.ee.jaxp.SchemaReference
import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.{HashMap, MutableList, Map}

object ValidatorHandlerPool extends Instrumented {
  private val validatorHandlerPools : Map[Schema, SoftReferenceObjectPool[ValidatorHandler]] = new HashMap[Schema, SoftReferenceObjectPool[ValidatorHandler]]
  private def pool(schema : Schema) : SoftReferenceObjectPool[ValidatorHandler] = validatorHandlerPools.getOrElseUpdate(schema, addPool(schema))

  private val activeGauges = new MutableList[Gauge[Int]]
  private val idleGauges = new MutableList[Gauge[Int]]

  private def addPool(schema : Schema) : SoftReferenceObjectPool[ValidatorHandler] = {
    val pool = new SoftReferenceObjectPool[ValidatorHandler](new ValidatorHandlerFactory(schema))
    val registryClassName = getRegistryClassName(getClass)
    val hash = Integer.toHexString(schema.hashCode())
    activeGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Active", hash))(pool.getNumActive)
    idleGauges :+ gaugeOrAdd(MetricRegistry.name(registryClassName, "Idle", hash))(pool.getNumIdle)
    pool
  }

  //
  //  Unfortunately, SAXON schema handlers cannot be pooled.  We detect
  //  this and always create a new handler in this case.
  //

  def borrowValidatorHandler(schema : Schema) : ValidatorHandler = {
    var ret : ValidatorHandler = null

    if (schema.isInstanceOf[SchemaReference]) {
      ret = schema.newValidatorHandler
    } else {
      ret = pool(schema).borrowObject()
    }
    ret
  }

  def returnValidatorHandler(schema : Schema, validatorHandler : ValidatorHandler) : Unit = {
    if (!schema.isInstanceOf[SchemaReference]) {
      pool(schema).returnObject(validatorHandler)
    }
  }

  def numActive(schema : Schema) : Int = {
    var ret = 0
    if (!schema.isInstanceOf[SchemaReference]) {
      ret = pool(schema).getNumActive
    }
    ret
  }

  def numIdle(schema : Schema) : Int = {
    var ret = 0
    if (!schema.isInstanceOf[SchemaReference]) {
      ret = pool(schema).getNumIdle
    }
    ret
  }
}

private class ValidatorHandlerFactory(private val schema : Schema) extends PoolableObjectFactory[ValidatorHandler] {
  def makeObject = schema.newValidatorHandler

  def activateObject (validatorHandler : ValidatorHandler) : Unit = {
    //
    //  No need to activate the parser should be ready to go.
    //
  }

  def validateObject (validatorHandler : ValidatorHandler) : Boolean = validatorHandler != null

  def passivateObject (validatorHandler : ValidatorHandler) : Unit = {
    //
    //  Not needed, the validatorHandler is reset on startDocument
    //
  }

  def destroyObject (validator : ValidatorHandler) : Unit = {
    //
    //  Not needed...
    //
  }
}
