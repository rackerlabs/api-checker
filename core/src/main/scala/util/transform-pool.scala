package com.rackspace.com.papi.components.checker.util

import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import javax.xml.transform.Templates
import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer

import net.sf.saxon.Controller
import net.sf.saxon.serialize.MessageWarner

object IdentityTransformPool {
  private val tf = TransformerFactory.newInstance()
  private val pool = new SoftReferenceObjectPool[Transformer](new IdentityTransformerFactory(tf))

  def borrowTransformer : Transformer = pool.borrowObject()
  def returnTransformer (transformer : Transformer) : Unit = pool.returnObject(transformer)
  def numActive : Int = pool.getNumActive()
  def numIdle : Int = pool.getNumIdle()
}

object TransformPool {
  private val transformPools : Map[Templates, SoftReferenceObjectPool[Transformer]] = new HashMap[Templates, SoftReferenceObjectPool[Transformer]]
  private def pool (templates : Templates) : SoftReferenceObjectPool[Transformer] =
    transformPools.getOrElseUpdate (templates, new SoftReferenceObjectPool[Transformer](new XSLTransformerFactory(templates)))

  def borrowTransformer (templates : Templates) = pool(templates).borrowObject
  def returnTransformer (templates : Templates, transformer : Transformer) = pool(templates).returnObject(transformer)
  def numActive (templates : Templates) : Int  = pool(templates).getNumActive()
  def numIdle (templates : Templates) : Int = pool(templates).getNumIdle()
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
