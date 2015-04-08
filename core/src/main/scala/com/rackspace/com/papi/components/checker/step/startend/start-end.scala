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
package com.rackspace.com.papi.components.checker.step.startend

import java.util.HashMap
import javax.servlet.FilterChain
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.XSDStringValidator
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results._

import scala.util.matching.Regex

//
//  The start step
//
class Start(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override def checkStep(req : CheckerServletRequest,
                         resp : CheckerServletResponse,
                         chain : FilterChain,
                         context : StepContext ) : Option[StepContext] = Some(context)

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    // If we have a malformed URI, then we can't even start the
    // machine, so return a 400 result.
    //
    req.parsedRequestURI match {
      case (_, Some(e)) => Some(new ErrorResult(e.getMessage(), 400, context, id, Long.MaxValue))
      case _ => super.check(req, resp, chain, context)
    }
  }

  override val mismatchMessage : String = "Bad Request?"
}

//
//  The accept state, send the request over
//
class Accept(id : String, label : String, val priority : Long) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    //  Send any request headers in the current context
    //
    req.addHeaders(context.requestHeaders)

    //
    //  For now, accept always send out to the chain
    //
    chain.doFilter(req, resp)

    //
    //  Send request...
    //
    return Some( new AcceptResult( "", context, id, priority ) )
  }
}

//
//  The URLFail state, return a 404
//
class URLFail(id : String, label : String, val priority : Long) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    //  If there is stuff in the path, then this error is
    //  applicable. Generate the error, commit the message. No URI
    //  stuff, then return None.
    //
    var result : Option[URLFailResult] = None

    if (context.uriLevel < req.URISegment.size) {
      val path = (for (i <- 0 until (context.uriLevel)) yield req.URISegment(i)).foldLeft("")(_ + "/" + _)+"/{"+req.URISegment(context.uriLevel)+"}"
      val ufr = new URLFailResult("Resource not found: "+path, context, id, priority)
      result = Some(ufr)
    }

    return result
  }
}

//
//  Like URLFail, but fails only if the current uri path is not matched
//  against the uri regex
//
class URLFailMatch(id : String, label : String, val uri : Regex, priority : Long) extends URLFail(id, label, priority) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = super.check (req, resp, chain, context)
    if (result != None) {
      req.URISegment(context.uriLevel) match {
        case uri() => result = None
        case _ => result = Some(new URLFailResult (result.get.message+". The URI segment does not match the pattern: '"+uri+"'", context, id, priority)) // Augment our parents result with match info
      }
    }
    result
  }
}

//
//  Like URLFailMatch, but fails only if the current uri path is not
//  matched against any of a number of simple XSD types
//
class URLFailXSDMatch(id : String, label : String, uri : Regex, types : Array[QName], schema : Schema, priority : Long) extends URLFailMatch(id, label, uri, priority) {
  //
  //  XSD validators
  //
  val validators : Array[XSDStringValidator] = types.map (t => new XSDStringValidator(t, schema, id))

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = super.check (req, resp, chain, context)
    if (result != None) {
      val in = req.URISegment(context.uriLevel)
      val errors = for (validator <- validators) yield {
        val e = validator.validate(in)
        if (e == None) return None
        e.get.getMessage()
      }

      val message = errors.foldLeft(result.get.message)(_ + " and "+_)
      result = Some(new URLFailResult (message, context, id, priority))
    }
    result
  }
}

//
//  Fail with a 415 if the request content type doesn't match one of
//  the accepted types
//
class ReqTypeFail(id : String, label : String, val types : Regex, val priority : Long) extends Step(id, label) {

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    var result : Option[BadMediaTypeResult] = None
    req.getContentType() match {
      case types() => result = None
      case _ => result = Some(new BadMediaTypeResult("The content type did not match the pattern: '"+types.toString.replaceAll("\\(\\?i\\)","")+"'", context, id, priority))
    }
    result
  }
}

//
//  Like URLFail, but fails only if the current uri path is not
//  matched by any of the simple XSD types.
//
class URLFailXSD(id : String, label : String, types : Array[QName], schema : Schema, priority : Long) extends URLFail(id, label, priority) {
  //
  //  XSD validators
  //
  val validators : Array[XSDStringValidator] = types.map (t => new XSDStringValidator(t, schema, id))


  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {

    var result : Option[Result] = super.check (req, resp, chain, context)
    if (result != None) {
      val in = req.URISegment(context.uriLevel)
      val errors = for (validator <- validators) yield {
        val e = validator.validate(in)
        if (e == None) return None
        e.get.getMessage()
      }

      val message = errors.foldLeft(result.get.message)(_ + " "+_)
      result = Some(new URLFailResult (message, context, id, priority))
    }
    result
  }
}

//
// Method fail state
//
class MethodFail(id : String, label : String, val priority : Long) extends Step(id, label) {
  private val allowHeaders = new HashMap[String,String](1)
  allowHeaders.put("Allow","")

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    //  If there is URL stuff return NONE.  Otherwise generate an
    //  error, commit the message.
    //
    var result : Option[MethodFailResult] = None

    if (context.uriLevel >= req.URISegment.size) {
      val mfr = new MethodFailResult("Bad method: "+req.getMethod(), context, id, priority, allowHeaders.clone().asInstanceOf[java.util.Map[String,String]] )
      result = Some(mfr)
    }

    return result
  }
}

//
//  Like MethodFail, but fails only if the current method is not
//  matched against the uri regex
//
class MethodFailMatch(id : String, label : String, val method : Regex, priority : Long) extends MethodFail(id, label, priority) {
  private val allowHeaders = new HashMap[String, String](1)
  allowHeaders.put("Allow", method.toString.replaceAll("\\|",", "))

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    var result : Option[Result] = super.check(req, resp, chain, context)
    if (result != None) {
      req.getMethod() match {
        case method() => result = None
        case _ => result = Some(new MethodFailResult (result.get.message+". The Method does not match the pattern: '"+method+"'",
                                                      context,
                                                      id,
                                                      priority,
                                                      allowHeaders.clone.asInstanceOf[java.util.Map[String,String]])) // Augment our parents result with match info
      }
    }
    result
  }
}

//
// Content fail state
//
class ContentFail(id : String, label : String, val priority : Long) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     context : StepContext) : Option[Result] = {
    //
    //  If there is a contentError in the request, return it,
    //  otherwise return NONE.
    //
    var result : Option[Result] = None

    if (req.contentError != null) {
      val msg = {
        var m = req.contentError.getMessage()
        if (m == null) {
          m = req.contentError.toString()
        }
        m
      }

      val prepend = if ( req.contentErrorCode == 400 ) "Bad Content: " else ""

      val actualPriority : Long = {
        if (req.contentErrorPriority != -1) {
          req.contentErrorPriority
        } else {
          priority
        }
      }

      // I'm assuming everything generated within this step is a content error
      val bcr = new BadContentResult ( prepend + msg, req.contentErrorCode, context, id, actualPriority)

      result = Some(bcr)
    }

    return result
  }
}
