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

import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.ObjectMapper

/*
 * Actually, this is only a pool for legacy reasons.
 * we converted from JSONSimple to Jackson and Jackson
 * has a threadsafe object mapper.
 */
object ObjectMapperPool extends Instrumented {
  private val om = new ObjectMapper()
  val registryClassName = getRegistryClassName(getClass)
  gaugeOrAdd(MetricRegistry.name(registryClassName, "Active"))(numActive)
  gaugeOrAdd(MetricRegistry.name(registryClassName, "Idle"))(numIdle)

  def borrowParser : ObjectMapper = om
  def returnParser (parser : ObjectMapper) : Unit = {}
  def numActive : Int = 1
  def numIdle : Int = 0
}
