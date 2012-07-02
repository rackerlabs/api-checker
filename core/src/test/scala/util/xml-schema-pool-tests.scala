package com.rackspace.com.papi.components.checker.util

import javax.xml.validation.Schema
import javax.xml.validation.Validator
import javax.xml.validation.ValidatorHandler

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

import javax.xml.validation._
import javax.xml.transform.stream._

@RunWith(classOf[JUnitRunner])
class XMLSchemaPoolSuite extends FunSuite {
  System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory")
  private val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1")

  val testSchema = schemaFactory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/test-urlxsd.xsd")))

  test ("The validator pool should successfully create a validator") {
    var validator : Validator = null
    try {
      validator = ValidatorPool.borrowValidator(testSchema)
      assert (validator != null)
    }finally {
      if (validator != null) ValidatorPool.returnValidator(testSchema, validator)
    }
  }

  test ("NumIdle should not be zero soon after returning a validator") {
    var validator : Validator = null
    try {
      validator = ValidatorPool.borrowValidator(testSchema)
    } finally {
      if (validator != null) ValidatorPool.returnValidator(testSchema, validator)
      assert (ValidatorPool.numIdle(testSchema) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new validators") {
    val NUM_INCREASE = 5

    val initActive = ValidatorPool.numActive(testSchema)
    val initIdle   = ValidatorPool.numIdle(testSchema)

    val builders = new Array[Validator](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = ValidatorPool.borrowValidator(testSchema)
    }

    assert (ValidatorPool.numActive(testSchema) >= initActive+NUM_INCREASE)

    val fullActive = ValidatorPool.numActive(testSchema)

    for (i <- 0 to NUM_INCREASE-1) {
      ValidatorPool.returnValidator (testSchema,builders(i))
    }

    assert (ValidatorPool.numActive(testSchema) <= fullActive-NUM_INCREASE)
  }

  test ("The validatorHandler pool should successfully create a validatorHandler") {
    var validatorHandler : ValidatorHandler = null
    try {
      validatorHandler = ValidatorHandlerPool.borrowValidatorHandler(testSchema)
      assert (validatorHandler != null)
    }finally {
      if (validatorHandler != null) ValidatorHandlerPool.returnValidatorHandler(testSchema, validatorHandler)
    }
  }

  test ("NumIdle should not be zero soon after returning a validatorHandler") {
    var validatorHandler : ValidatorHandler = null
    try {
      validatorHandler = ValidatorHandlerPool.borrowValidatorHandler(testSchema)
    } finally {
      if (validatorHandler != null) ValidatorHandlerPool.returnValidatorHandler(testSchema, validatorHandler)
      assert (ValidatorHandlerPool.numIdle(testSchema) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new validatorHandlers") {
    val NUM_INCREASE = 5

    val initActive = ValidatorHandlerPool.numActive(testSchema)
    val initIdle   = ValidatorHandlerPool.numIdle(testSchema)

    val builders = new Array[ValidatorHandler](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      builders(i) = ValidatorHandlerPool.borrowValidatorHandler(testSchema)
    }

    assert (ValidatorHandlerPool.numActive(testSchema) >= initActive+NUM_INCREASE)

    val fullActive = ValidatorHandlerPool.numActive(testSchema)

    for (i <- 0 to NUM_INCREASE-1) {
      ValidatorHandlerPool.returnValidatorHandler (testSchema,builders(i))
    }

    assert (ValidatorHandlerPool.numActive(testSchema) <= fullActive-NUM_INCREASE)
  }

}
