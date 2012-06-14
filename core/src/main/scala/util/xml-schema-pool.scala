package com.rackspace.com.papi.components.checker.util

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

import javax.xml.validation.Schema
import javax.xml.validation.Validator
import javax.xml.validation.ValidatorHandler

object ValidatorPool {
  private val validatorPools : Map[Schema, SoftReferenceObjectPool[Validator]] = new HashMap[Schema, SoftReferenceObjectPool[Validator]]
  private def pool(schema : Schema) : SoftReferenceObjectPool[Validator] = validatorPools.getOrElseUpdate(schema, new SoftReferenceObjectPool[Validator](new ValidatorFactory(schema)))

  def borrowValidator(schema : Schema) : Validator = pool(schema).borrowObject()
  def returnValidator(schema : Schema, validator : Validator) : Unit = pool(schema).returnObject(validator)
  def numActive(schema : Schema) : Int = pool(schema).getNumActive()
  def numIdle(schema : Schema) : Int = pool(schema).getNumIdle()
}

object ValidatorHandlerPool {
  private val validatorHandlerPools : Map[Schema, SoftReferenceObjectPool[ValidatorHandler]] = new HashMap[Schema, SoftReferenceObjectPool[ValidatorHandler]]
  private def pool(schema : Schema) : SoftReferenceObjectPool[ValidatorHandler] =
    validatorHandlerPools.getOrElseUpdate(schema, new SoftReferenceObjectPool[ValidatorHandler](new ValidatorHandlerFactory(schema)))

  def borrowValidatorHandler(schema : Schema) : ValidatorHandler = pool(schema).borrowObject()
  def returnValidatorHandler(schema : Schema, validatorHandler : ValidatorHandler) : Unit = pool(schema).returnObject(validatorHandler)
  def numActive(schema : Schema) : Int = pool(schema).getNumActive()
  def numIdle(schema : Schema) : Int = pool(schema).getNumIdle()
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
