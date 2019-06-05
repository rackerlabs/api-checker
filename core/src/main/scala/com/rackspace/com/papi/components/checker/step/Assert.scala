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
package com.rackspace.com.papi.components.checker.step

import javax.servlet.FilterChain

import org.xml.sax.SAXParseException

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.XQueryEvaluatorPool._

import net.sf.saxon.om.GroundedValue
import net.sf.saxon.om.Sequence

import net.sf.saxon.s9api.XQueryEvaluator

import com.typesafe.scalalogging.LazyLogging

class Assert(id : String, label : String, val expression : String, val message : Option[String],
             val code : Option[Int], val nc : ImmutableNamespaceContext, val version : Int,
             val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) with LazyLogging {

  override val mismatchMessage : String = message.getOrElse (s"Expecting $expression")
  val mismatchCode : Int = code.getOrElse(400)

  private val exec = XPathStepUtil.xqueryExecutableForExpression(expression, nc)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var ret : Option[StepContext] = None
    var eval : XQueryEvaluator = null

    try {
      eval = borrowEvaluator(expression, exec)
      XPathStepUtil.setupXQueryEvaluator(eval, req, context)
      val res : Boolean = eval.evaluate.getUnderlyingValue match {
        case groundedValue : GroundedValue => groundedValue.effectiveBooleanValue
        case s : Sequence => s.head != null
      }
      if (res) {
        ret = Some(context)
      } else {
        req.contentError(new SAXParseException (mismatchMessage, null), mismatchCode, priority)
      }
    } catch {
      case e : Exception => req.contentError(new SAXParseException(mismatchMessage+" : "+e.getMessage, null, e), mismatchCode, priority)
    }finally {
      returnEvaluator (expression, eval)
    }
    ret
  }
}
