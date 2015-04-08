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
package com.rackspace.com.papi.components.checker.step.results

import java.util.{HashMap, Map}
import com.rackspace.com.papi.components.checker.step.base.StepContext
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.immutable.List
import scala.collection.mutable.PriorityQueue

class ErrorResult(private val messageP : String,
                  private val codeP : Int,
                  context : StepContext,
                  stepId : String,
                  priority : Long,
                  private val headersP : Map[String,String] = new HashMap()) extends Result(messageP, false, true, context,
                                                                                   stepId, priority) {

  //
  // In MultiErrorResult, we forward the following calls to another object.  You cannot override val with a def call
  // since val implies a stable & immutable guarentee which def does not contain.
  //
  // We declare private vals and manually create accessors
  //
  def code = codeP
  def headers = headersP

  override def toString : String = path+" "+code+" : "+message
  override def cmpString : String = super.cmpString+" "+code
}
