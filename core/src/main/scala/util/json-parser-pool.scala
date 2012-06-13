package com.rackspace.com.papi.components.checker.util

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.SoftReferenceObjectPool

import org.json.simple.parser.JSONParser

object JSONParserPool {
  private val pool = new SoftReferenceObjectPool[JSONParser](new JSONParserFactory)

  def borrowParser : JSONParser = pool.borrowObject()
  def returnParser (parser : JSONParser) : Unit = pool.returnObject(parser)
  def numActive : Int = pool.getNumActive()
  def numIdle : Int = pool.getNumIdle()
}

private class JSONParserFactory extends PoolableObjectFactory[JSONParser] {

  def makeObject = new JSONParser()

  def validateObject (parser : JSONParser) : Boolean = parser != null

  def activateObject (parser : JSONParser) : Unit = {
    //
    //  No need to activate the parser should be ready to go.
    //
  }

  def passivateObject (parser : JSONParser) : Unit = {
    //
    //  Not needed...
    //
  }

  def destroyObject (parser : JSONParser) : Unit = {
    //
    //  Not needed...
    //
  }
}
