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

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.LinkedList

import javax.xml.validation.Schema
import javax.xml.validation.Validator
import javax.xml.validation.ValidatorHandler

import com.saxonica.jaxp.SchemaReference

import com.yammer.metrics.core.Gauge
import com.yammer.metrics.scala.Instrumented

object ValidatorPool extends Instrumented {
  private val validatorPools : Map[Schema, SoftReferenceObjectPool[Validator]] = new HashMap[Schema, SoftReferenceObjectPool[Validator]]
  private def pool(schema : Schema) : SoftReferenceObjectPool[Validator] = validatorPools.getOrElseUpdate(schema, addPool(schema))

  private val activeGauges = new LinkedList[Gauge[Int]]
  private val idleGauges = new LinkedList[Gauge[Int]]

  private def addPool(schema : Schema) : SoftReferenceObjectPool[Validator] = {
    val pool = new SoftReferenceObjectPool[Validator](new ValidatorFactory(schema))
    activeGauges :+ metrics.gauge("Active", Integer.toHexString(schema.hashCode()))(pool.getNumActive)
    idleGauges :+ metrics.gauge("Idle", Integer.toHexString(schema.hashCode()))(pool.getNumIdle)
    pool
  }

  //
  //  Unfortunetly, SAXON validators cannot be pooled.  We detect this
  //  and always create a new handler in this case.
  //

  def borrowValidator(schema : Schema) : Validator = {
    var ret : Validator = null

    if (schema.isInstanceOf[SchemaReference]) {
      ret = schema.newValidator
    } else {
      ret = pool(schema).borrowObject()
      ret.setFeature("http://apache.org/xml/features/validation/schema/element-default", true)
    }

    ret
  }

  def returnValidator(schema : Schema, validator : Validator) : Unit = {
    if (!schema.isInstanceOf[SchemaReference]) {
      pool(schema).returnObject(validator)
    }
  }

  def numActive(schema : Schema) : Int = {
    var ret = 0
    if (!schema.isInstanceOf[SchemaReference]) {
      ret = pool(schema).getNumActive()
    }
    ret
  }

  def numIdle(schema : Schema) : Int = {
    var ret = 0
    if (!schema.isInstanceOf[SchemaReference]) {
      ret = pool(schema).getNumIdle()
    }
    ret
  }
}

object ValidatorHandlerPool extends Instrumented {
  private val validatorHandlerPools : Map[Schema, SoftReferenceObjectPool[ValidatorHandler]] = new HashMap[Schema, SoftReferenceObjectPool[ValidatorHandler]]
  private def pool(schema : Schema) : SoftReferenceObjectPool[ValidatorHandler] = validatorHandlerPools.getOrElseUpdate(schema, addPool(schema))

  private val activeGauges = new LinkedList[Gauge[Int]]
  private val idleGauges = new LinkedList[Gauge[Int]]

  private def addPool(schema : Schema) : SoftReferenceObjectPool[ValidatorHandler] = {
    val pool = new SoftReferenceObjectPool[ValidatorHandler](new ValidatorHandlerFactory(schema))
    activeGauges :+ metrics.gauge("Active", Integer.toHexString(schema.hashCode()))(pool.getNumActive)
    idleGauges :+ metrics.gauge("Idle", Integer.toHexString(schema.hashCode()))(pool.getNumIdle)
    pool
  }

  //
  //  Unfortunetly, SAXON schema handlers cannot be pooled.  We detect
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
      ret = pool(schema).getNumActive()
    }
    ret
  }

  def numIdle(schema : Schema) : Int = {
    var ret = 0
    if (!schema.isInstanceOf[SchemaReference]) {
      ret = pool(schema).getNumIdle()
    }
    ret
  }
}

private class ValidatorFactory(private val schema : Schema) extends PoolableObjectFactory[Validator] {
  def makeObject = schema.newValidator

  def activateObject (validator : Validator) : Unit = {
    //
    //  No need to activate the parser should be ready to go.
    //
  }

  def validateObject (validator : Validator) : Boolean = validator != null

  def passivateObject (validator : Validator) : Unit = {
    validator.reset()
  }

  def destroyObject (validator : Validator) : Unit = {
    //
    //  Not needed...
    //
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
