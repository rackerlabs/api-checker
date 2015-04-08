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
