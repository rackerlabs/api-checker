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

import java.util.Enumeration
import javax.servlet.http.HttpServletRequest

import scala.collection.JavaConversions._

import com.rackspace.com.papi.components.checker.step.base.StepContext

object HeaderUtil {


  /**
   * Returns true if a header by the given name is available
   *
   * The context and the request are both seached.
   */
  def hasHeader (context : StepContext, request : HttpServletRequest, name : String) : Boolean = context.requestHeaders.get(name) match {
    case Some(v :: _) => true
    case _ => request.getHeader(name) != null
  }

  /**
   * Search the context for the header first, if it's not there,
   * then look at the request.
   *
   * We assume the header is reapeating so if it's split by a comma,
   * then take only the first value.
   *
   */
  def getHeader (context : StepContext, request : HttpServletRequest, name : String) : Option[String] = {

    val value = context.requestHeaders.get(name) match {
      case Some(v :: _) => v
      case _            => request.getHeader(name)
    }

    value match {
      case null => None
      case _ =>  Some(value.split(",")(0).trim)
    }
  }

  /**
   * Return all headers.. assume the headers are repeating so do
   * the comma split thing.
   *
   * We return context header values before request header values.
   *
   */
  def getHeaders (context : StepContext, request : HttpServletRequest, name : String) : List[String] = {
    val req_headers = request.getHeaders(name) match {
      case null => List[String]()
      case e : Enumeration[String] =>  e.toList
    }

    val all_headers = context.requestHeaders.get(name) match {
      case Some(Nil) => req_headers
      case Some(l) => l ++ req_headers
      case _ => req_headers
    }

    var list : List[String] = List()
    all_headers.foreach(i => list = list ++ i.split(",").map(j => j.trim))
    list
  }

  /**
   * Like getHeaders, but we don't split header values by comma.
   * We return all header values, context header values are returned
   * before  request header values.
   *
   */
  def getNonSplitHeaders(context : StepContext, request : HttpServletRequest, name : String) : List[String] = {
    val req_headers = request.getHeaders(name) match {
      case null => List[String]()
      case e : Enumeration[String] =>  e.toList
    }

    context.requestHeaders.get(name) match {
      case Some(Nil) => req_headers
      case Some(l) => l ++ req_headers
      case _ => req_headers
    }
  }
}
