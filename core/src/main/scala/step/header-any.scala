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
package com.rackspace.com.papi.components.checker.step

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.HeaderUtil._
import javax.servlet.FilterChain

import scala.collection.JavaConversions._

class HeaderAny(id : String, label : String, val name : String, val value : Regex,
                val message : Option[String], val code : Option[Int], val priority : Long,
                next : Array[Step]) extends ConnectedStep(id, label, next) {

  def this(id : String, label : String, name : String, value : Regex, priority : Long,
           next : Array[Step]) = this(id, label, name, value, None, None, priority, next)

  override val mismatchMessage : String = {
    if (message == None) {
      "Expecting an HTTP header "+name+" to have a value matching "+value.toString()
    } else {
      message.get
    }
  }

  val mismatchCode : Int = {
    if (code == None) {
      400
    } else {
      code.get
    }
  }

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    val headers : Iterator[String] = getHeaders(req, name)

    //
    //  If there exists at least one header matching the the name AND
    //  matching the value regex, then return the uriLevel otherwise
    //  set an error and return -1
    //
    if (headers.exists(v => v match { case value() => true ; case _ => false })) {
      uriLevel
    } else {
      req.contentError(new Exception(mismatchMessage), mismatchCode, priority)
      -1
    }
  }
}
