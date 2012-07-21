package com.rackspace.com.papi.components.checker.util

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer

object IdentityTransformPool {
  private val tf = TransformerFactory.newInstance()
  private val pool = new SoftReferenceObjectPool[Transformer](new IdentityTransformerFactory(tf))

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
