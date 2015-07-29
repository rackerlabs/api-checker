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
package com.rackspace.com.papi.components.checker.step.base

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.results.{MismatchResult, MultiFailResult, Result}

import scala.collection.mutable.ListBuffer

//
//  Steps that ore connected to other steps
//
abstract class ConnectedStep(id : String, label : String, val next : Array[Step]) extends Step (id, label) {

  //
  //  Check the currest step.  If the step is a match return a new
  //  updated context, if not then return None
  //
  def checkStep(req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = None

  //
  //  Check the currest step.  If the step is a match return the next
  //  URI level if not then return -1.  This method is written for
  //  compatability reasons, there was a time where the only context
  //  was the URI level.
  //
  final def checkStep(req : CheckerServletRequest, resp : HttpServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    checkStep(req, resp, chain, StepContext(uriLevel)) match {
      case Some(context) => context.uriLevel
      case None => -1
    }
  }

  //
  //  The error message when there is a step mismatch.
  //
  val mismatchMessage : String = "Step Mismatch"

  //
  //  Go to the next step.
  //
  def nextStep (req : CheckerServletRequest,
                resp : HttpServletResponse,
                chain : FilterChain,
                context : StepContext) : Array[Result] = {

    val resultBuffer = new ListBuffer[Result]
    for (i <- 0 to next.length-1) {
      val oresult = next(i).check(req, resp, chain, context)
      if (oresult.isDefined) {
        val result = oresult.get
        if (result.valid) {
          return Array(result)
        } else {
          resultBuffer += result
        }
      }
    }
    return resultBuffer.toArray
  }

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : CheckerServletRequest,
                     resp : HttpServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = None
    val nextContext = checkStep(req, resp, chain, context)

    if (nextContext != None) {
      val results : Array[Result] =
          nextStep (req, resp, chain, nextContext.get.handler.map{ handler => handler.inStep(this, req, resp, nextContext.get) }.getOrElse(nextContext.get))
      if (results.size == 1) {
        results(0).addStepId(id)
        result = Some(results(0))
      } else {
        result = Some(new MultiFailResult (results, id))
      }

    } else {
      result = Some( new MismatchResult( mismatchMessage, context, id) )
    }

    return result
  }
}
