/***
 *   Copyright 2017 Rackspace US, Inc.
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


import java.io.ByteArrayInputStream

import java.nio.charset.StandardCharsets

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.step.XPathStepUtil
import com.rackspace.com.papi.components.checker.step.results.{MismatchResult, MultiFailResult, Result}
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.XQueryEvaluatorPool._

import net.sf.saxon.s9api.XQueryEvaluator

import net.sf.saxon.value.AtomicValue
import net.sf.saxon.value.StringValue

import net.sf.saxon.om.Function

import com.typesafe.scalalogging.LazyLogging

abstract class PushRepresentation(id : String, label : String, val name : String, val expression : String,
                                  val nc : ImmutableNamespaceContext, val version : Int, val priority : Long,
                                  val allowAllAtomic : Boolean, val repCommonName : String, next : Array[Step])
    extends ConnectedStep(id, label, next) with LazyLogging {

  override val mismatchMessage : String = s"Could not find well formed $repCommonName at $expression"

  private val exec = XPathStepUtil.xqueryExecutableForExpression(expression, nc)

  //
  //  The function should push the representation given in the current
  //  string into the CheckerServletRequest.
  //
  //
  def pushRepresentation(req : CheckerServletRequest, rep : String) : Unit

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {

    var ret : Option[StepContext] = None
    var eval : XQueryEvaluator = null

    try {
      eval = borrowEvaluator(expression, exec)
      XPathStepUtil.setupXQueryEvaluator(eval, req, context)
      val res = eval.evaluate
      //
      // Check the size of the result
      //
      res.size match {
        case 0 => throw new RepresentationException(s"Expecting $repCommonName at $expression, but got an empty sequence")
        case 1 => // This is what we want, a single result
        case _ => throw new RepresentationException(s"Expecting $repCommonName at $expression, but got a sequence of size > 1")
      }

      //
      //  We always expect the result to be a string, we do a bit of
      //  error checking based on the type of the result to avoid
      //  trying to call the parser unnecessarily and avoid weird
      //  parse errors.
      //
      //  An open question is whether atomic types outside of a string
      //  can be parsed (int, dateTime, URLs). For representations
      //  like XML the answer is no, for JSON the answer is yes.
      //
      val item = res.getUnderlyingValue.head
      item match {
        case sv : StringValue =>
          //
          //  A string atomic type, may always be a representation so
          //  we'll accept that.
          //
        case av : AtomicValue if (!allowAllAtomic) =>
          //
          //  An atomic type other that a string should fail, if other
          //  atomic representations are not allowed.
          //
          val strValue = av.toString
          throw new RepresentationException(s"The atomic value '$strValue' cannot be converted into $repCommonName at $expression")
        case fun : Function =>
          val itemDesc = fun match {
            case f if (fun.isArray) => "An array"
            case f if (fun.isMap) => "A map"
            case _ => "A Function"
          }
          throw new RepresentationException(s"$itemDesc cannot be converted into $repCommonName at $expression")
        case _ =>
          //
          //  Other types (such as nodes) will be treated as strings,
          //  by getting their string values.
          //
      }

      val stringResult = res.itemAt(0).getStringValue

      //
      //  We can't parse empty strings so don't try...
      //
      if (stringResult.trim == "") {
        throw new RepresentationException (s"Expecting $repCommonName at $expression, but got an empty string")
      }

      //
      //  Attempt to parse the string and push the representation
      //
      pushRepresentation(req, stringResult)
      ret = Some(context)
    } catch {
      case e : Exception => req.contentError = e
                            req.contentErrorPriority = priority
    }
    finally {
      returnEvaluator (expression, eval)
    }
    ret
  }

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = None
    val nextContext = checkStep(req, resp, chain, context)

    if (nextContext.isDefined) {
      //
      //  if we succeeded the representation in the request should be
      //  the one we set!
      //
      val setRep = req.parsedRepresentation
      val results : Array[Result] =
          nextStep (req, resp, chain, nextContext.get.handler.map{ handler => handler.inStep(this, req, resp, nextContext.get) }.getOrElse(nextContext.get))
      if (results.length == 1) {
        results(0).addStepId(id)
        result = Some(results(0))
      } else {
        result = Some(new MultiFailResult (results, id))
      }

      //
      // If there is an error associated with my particular
      // representation, then we need to POP the representation out.
      //
      if (!result.get.valid && (setRep == req.parsedRepresentation)) {
        req.popRepresentation
      }

    } else {
      result = Some( new MismatchResult( mismatchMessage, context, id) )
    }

    result
  }

}
