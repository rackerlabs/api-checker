package com.rackspace.com.papi.components.checker.util

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Templates
import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamSource

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class TransformPoolSuite extends FunSuite {
  val factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
  val templates1 = factory.newTemplates (new StreamSource(getClass().getResourceAsStream("/xsl/builder.xsl")))
  val templates2 = factory.newTemplates (new StreamSource(getClass().getResourceAsStream("/xsl/removeDups.xsl")))

  test("The ID transform pool should successfully create a transformer") {
    var transformer : Transformer = null
    try {
      transformer = TransformPool.borrowTransformer(templates1)
      assert (transformer != null)
    }finally {
      if (transformer != null) TransformPool.returnTransformer(templates1, transformer)
    }
  }

  test("NumIdle should not be zero soon after returning a transformer") {
    var transformer : Transformer = null
    try {
      transformer = TransformPool.borrowTransformer(templates1)
    }finally {
      if (transformer != null) TransformPool.returnTransformer(templates1, transformer)
      assert (TransformPool.numIdle(templates1) != 0)
    }
  }

  test("NumActive should increase/decrease as we borrow/return new transformers") {
    val NUM_INCREASE = 5

    val initActive = TransformPool.numActive(templates1)
    val initIdle   = TransformPool.numIdle(templates1)
    val initActiveOther = TransformPool.numActive(templates2)

    val transformers = new Array[Transformer](NUM_INCREASE)
    for (i <- 0 to NUM_INCREASE-1) {
      transformers(i) = TransformPool.borrowTransformer(templates1)
    }

    assert (TransformPool.numActive(templates1) >= initActive+NUM_INCREASE)

    val fullActive = TransformPool.numActive(templates1)

    for (i <- 0 to NUM_INCREASE-1) {
      TransformPool.returnTransformer (templates1, transformers(i))
    }

    assert (TransformPool.numActive(templates1) <= fullActive-NUM_INCREASE)
    //
    //  Other template pools should not have been accessed.
    //
    assert(TransformPool.numActive(templates2) == initActiveOther)
  }
}
