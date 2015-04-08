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

//
//  Base class for all checker results
//
abstract class Result(private val messageP : String,   // A message describing the result
                      private val validP : Boolean,    // Was the http req/res valid?
                      private val terminalP : Boolean, // Are we at a terminal node in the machine
                      private val contextP : StepContext,     // The context at the error
                      val stepId : String, // The ID of the machine at the error
                      private val priorityP : Long = 1) extends Ordered[Result] {

  private var stepIDsP : List[String] = List[String]() // ID of the linear steps it took to get
  // to this result

  def addStepId(stepId : String) : Unit = {
    stepIDsP = stepId +: stepIDsP
  }

  addStepId(stepId)

  //
  // In MultiErrorResult, we forward the following calls to another object.  You cannot override val with a def call
  // since val implies a stable & immutable guarantee which def does not contain.
  //
  // We declare private vals and manually create accessors
  //
  def message = messageP
  def valid = validP
  def terminal = terminalP
  def context = contextP
  def priority = priorityP
  def stepIDs = stepIDsP

  def allResults : Traversable[Result] = List( this )

  //
  // Orders Results by priority
  //
  def compare( o : Result ) = (priority - o.priority).asInstanceOf[Int]

  protected val startPath = "["
  protected val endPath = "]"

  def path : String = startPath+stepIDs.reduceLeft(_+" "+_)+endPath
  def cmpString : String = message+" "+path+" "+terminal+" "+context.uriLevel
  override def toString : String = path
  override def hashCode : Int = cmpString.hashCode
  override def equals (any : Any) : Boolean = {
    any match {
      case r : Result => this.hashCode() == r.hashCode()
      case _ => false
    }
  }
}
