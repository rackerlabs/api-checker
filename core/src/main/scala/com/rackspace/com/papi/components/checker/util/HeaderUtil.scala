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

object HeaderUtil {

  def getHeader (request : HttpServletRequest, name : String) = {
    val value = request.getHeader(name)
    value match {
      case null => null
      case _ =>  name.split(",")(0).trim
    }
  }

  def getHeaders (request : HttpServletRequest, name : String) : Enumeration[String] = {
    val headers = request.getHeaders(name)
    headers match {
      case null => List[String]().iterator
      case _ => var list : List[String] = List()
        headers.foreach(i => list = list ++ i.split(",").map(j => j.trim))
        list.iterator
    }
  }
}
