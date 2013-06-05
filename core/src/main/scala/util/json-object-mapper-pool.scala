package com.rackspace.com.papi.components.checker.util

import com.fasterxml.jackson.databind.ObjectMapper

import com.yammer.metrics.scala.Instrumented

/*
 * Actually, this is only a pool for legacy reasons.
 * we converted from JSONSimple to Jackson and Jackson
 * has a threadsafe object mapper.
 */
object ObjectMapperPool extends Instrumented {
  private val om = new ObjectMapper()
  private val activeGauge = metrics.gauge("Active")(numActive)
  private val idleGauge = metrics.gauge("Idle")(numIdle)

  def borrowParser : ObjectMapper = om
  def returnParser (parser : ObjectMapper) : Unit = {}
  def numActive : Int = 1
  def numIdle : Int = 0
}
