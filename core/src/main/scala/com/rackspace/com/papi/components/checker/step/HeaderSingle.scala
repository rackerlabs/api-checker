/***
 *   Copyright 2016 Rackspace US, Inc.
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

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.HeaderUtil._

import scala.collection.JavaConversions._
import scala.util.matching.Regex

class HeaderSingle(id : String, label : String, val name : String, val value : Regex,
             val message : Option[String], val code : Option[Int],
             val captureHeader : Option[String], val priority : Long,
             next : Array[Step]) extends ConnectedStep(id, label, next) {

  override val mismatchMessage : String = {
    if (message.isEmpty) {
      "Expecting an HTTP header "+name+" to have a value matching "+value.toString
    } else {
      message.get
    }
  }

  val numMessage : String = {
    if (message.isEmpty) {
      s"Expecting 1 and only 1 instance of Header $name"
    } else {
      message.get
    }
  }

  val mismatchCode : Int = {
    if (code.isEmpty) {
      400
    } else {
      code.get
    }
  }

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    //
    //  If there exists one and only one header matching the the name AND
    //  the value of the header matches the regex, then
    //  return a valid context otherwise set an error and return None
    //
    getNonSplitHeaders(context, req, name) match {
      case Nil => req.contentError(new Exception(mismatchMessage),mismatchCode, priority)
                  None
      case header :: Nil => header match {
              case value() => captureHeader match {
                  case None => Some(context)
                  case Some(h) => Some(context.copy(requestHeaders = context.requestHeaders.addHeader(h, header)))
              }
              case _ => req.contentError(new Exception(mismatchMessage),mismatchCode, priority)
                        None
      }
      case _ => req.contentError(new Exception(numMessage), mismatchCode, priority)
                None
    }
  }
}
