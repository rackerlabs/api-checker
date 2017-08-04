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

import scala.collection.mutable.ListBuffer

import org.xml.sax.SAXParseException

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.XQueryEvaluatorPool._

import net.sf.saxon.om.GroundedValue
import net.sf.saxon.om.Sequence

import net.sf.saxon.s9api.XQueryEvaluator
import net.sf.saxon.s9api.XdmValue

import com.typesafe.scalalogging.slf4j.LazyLogging

class CaptureHeader(id : String, label : String, val name : String, val expression : String,
                    val nc : ImmutableNamespaceContext, val version : Int,
                    next : Array[Step]) extends ConnectedStep(id, label, next) with LazyLogging {

  private val exec = XPathStepUtil.xqueryExecutableForExpression(expression, nc)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {

    var ret : Option[StepContext] = Some(context) // This step is always successful!
    var eval : XQueryEvaluator = null

    try {
      eval = borrowEvaluator(expression, exec)
      XPathStepUtil.setupXQueryEvaluator(eval, req, context)
      val res = eval.evaluate
      if (res.size > 0) {
        //
        //  The result is stored in the request context as a
        //  header. Each item in the result sequence is stored as a
        //  sepearte header value.
        //
        //  Note that it's very possible to add header values that
        //  contain multiple lines of text or that contain characters
        //  outside of the US-ASCII character set.  It is up to the
        //  underlying servlet container to handle these subtleties.
        //
        ret=Some(context.copy(requestHeaders=context.requestHeaders.addHeaders(name,toReqHeaders(res))))
      }
    }finally {
      returnEvaluator (expression, eval)
    }
    ret
  }

  def toReqHeaders (xdmValue : XdmValue) : List[String] = {
    var ret = new ListBuffer[String]()
    val iterator = xdmValue.iterator
    while (iterator.hasNext) {
      ret += iterator.next.getStringValue
    }
    ret.toList
  }
}
