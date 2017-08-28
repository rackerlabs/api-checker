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

import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.util.HeaderMap

import net.sf.saxon.s9api.XdmValue
import net.sf.saxon.s9api.XdmMap
import net.sf.saxon.Configuration

import collection.JavaConverters._

//
//  Used to keep context about the current request
//
case class StepContext(uriLevel : Int = 0, requestHeaders : HeaderMap = new HeaderMap, handler: Option[ResultHandler] = None) {
  def asXdmValue : XdmValue = {
    val headers : java.util.Map[String, java.util.List[String]] = new java.util.HashMap[String,java.util.List[String]]()
    requestHeaders.foreach { case (k : String, l : List[String]) =>  headers.put (k, l.asJava) }
    val jcontext : java.util.Map[String, Object] = new java.util.HashMap[String, Object]()
    jcontext.put("uriLevel", uriLevel.asInstanceOf[Object])
    jcontext.put("headers", headers)
    XdmMap.makeMap(jcontext)
  }
}
