package com.rackspace.com.papi.components.checker.util

import javax.xml.transform.Transformer

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class IdentityTransformPoolSuite extends FunSuite {

  test("The ID transform pool should successfully create a transformer") {
    var transformer : Transformer = null
    try {
      transformer = IdentityTransformPool.borrowTransformer
      assert (transformer != null)
    }finally {
      if (transformer != null) IdentityTransformPool.returnTransformer(transformer)
    }
  }

  test("NumIdle should not be zero soon after returning a transformer") {
    var transformer : Transformer = null
    try {
      transformer = IdentityTransformPool.borrowTransformer
    }finally {
      if (transformer != null) IdentityTransformPool.returnTransformer(transformer)
      assert (IdentityTransformPool.numIdle != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new transformers") {
    val NUM_INCREASE = 5

    val initActive = IdentityTransformPool.numActive
    val initIdle   = IdentityTransformPool.numIdle

    val transformers = new Array[Transformer](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      transformers(i) = IdentityTransformPool.borrowTransformer
    }

    assert (IdentityTransformPool.numActive >= initActive+NUM_INCREASE)

    val fullActive = IdentityTransformPool.numActive

    for (i <- 0 to NUM_INCREASE-1) {
      IdentityTransformPool.returnTransformer (transformers(i))
    }

    assert (IdentityTransformPool.numActive <= fullActive-NUM_INCREASE)
  }
}
