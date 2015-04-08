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

class AcceptResult(message: String,
                   context : StepContext,
                   stepId : String,
                   priority : Long)  extends Result(message, true, true, context, stepId, priority)

class BadContentResult(message : String,
                       codeP : Int = 400,
                       context : StepContext,
                       stepId : String,
                       priority : Long) extends ErrorResult(message, codeP, context, stepId, priority)

class URLFailResult(message : String,
                    context : StepContext,
                    stepId : String,
                    priority: Long) extends ErrorResult(message, 404, context, stepId, priority)

class MethodFailResult(message: String,
                       context : StepContext,
                       stepId : String,
                       priority : Long,
                       headers : Map[String,String]) extends ErrorResult(message, 405, context, stepId,
                                                                         priority, headers )

class BadMediaTypeResult(message: String,
                         context : StepContext,
                         stepId : String,
                         priority : Long) extends ErrorResult(message, 415, context, stepId, priority)

class MismatchResult(message: String,
                     context : StepContext,
                     stepId : String) extends Result(message, false, false, context, stepId) {
  override protected val startPath = "("
  override protected val endPath = ")"
}


class NoResultsException( val message : String ) extends Exception( message )

//
// This class is a wrapper around priorityqueue of several results, delegating the Result methods to the Result
// at the head of the data structure.
//
class MultiFailResult(val fails : Array[Result], stepId : String) extends ErrorResult ( "Multiple possible errors", -1, StepContext(-1), stepId, -1 ) with LazyLogging {

  if ( fails.isEmpty ) throw new NoResultsException( "Input array must be non-empty." )

  private val results = new PriorityQueue[Result]()

  results ++= fails

  //
  // delegate all method calls to the Result with the highest priority
  //
  override def message = results.head.message
  override def context = results.head.context
  override def valid = results.head.valid
  override def terminal = results.head.terminal
  override def priority = results.head.priority

  override def code = asErrorResult(results.head).code
  override def headers = asErrorResult(results.head).headers

  private def asErrorResult (r : Result) : ErrorResult = {
    try {
      r.asInstanceOf[ErrorResult]
    } catch {
      case cce : ClassCastException  => logger.error(
        "Internal Error: A non[ErrorResult] has bubbled up as the top priority error in a [MultiFailResult]. "+
        "This should never happen. Perhaps there is something wrong with the priority-map.xml in the CLASSPATH?")
        throw cce
    }
  }

  //
  // Define new Traversable instance which calls foreach on each MultiFailResult
  // and the function on each non-MultiFailResult (i.e., each leaf node in the
  // Result-tree).  The main result is encountered first in this traversal.
  //
  override def allResults : Traversable[Result] = {

    new Traversable[Result] {

      def foreach[U]( f: Result => U ) = {
        results.foreach(
          ( r : Result) => {
            r match {
              case m : MultiFailResult => r.allResults.foreach( f )
              case r : Result => f( r )
            }
          }
        )
      }
    }
  }

  override protected val startPath = "{"
  override protected val endPath = "}"

  private def selectId (r : Result) : String = {
    r match {
      case m : MultiFailResult => selectId(m.results.head)
      case r : Result => r.stepId
    }
  }

  override def path : String = startPath+stepIDs.reduceLeft(_+" "+_)+" " + fails.map( _.path ).reduceLeft( _ + " " + _ ) + endPath+"*"+selectId(this)+"*"
}
