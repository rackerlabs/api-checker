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

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.util.HeaderUtil._

import javax.servlet.FilterChain

import org.xml.sax.SAXParseException

import scala.collection.JavaConversions._

class HeaderXSDAny(id : String, label : String, val name : String, val value : QName, schema : Schema,
                   val message : Option[String], val code : Option[Int], val priority : Long,
                   next : Array[Step]) extends ConnectedStep(id, label, next) {

  def this(id : String, label : String, name : String, value : QName, schema : Schema, priority : Long,
           next : Array[Step]) = this(id, label, name, value, schema, None, None, priority, next)

  override val mismatchMessage : String = {
    if (message == None) {
      "Expecting an HTTP header "+name+" to match "+value
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

  val xsd = new XSDStringValidator(value, schema, id)

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    val headers : Iterator[String] = getHeaders(req, name)
    var last_err : Option[SAXParseException] = None

    //
    //  If there exists at least one header matching the the name AND
    //  the value type in the XSD, then return the uriLevel otherwise
    //  set an error and return -1
    //
    if (headers.exists(v => { last_err = xsd.validate(v);  last_err match { case None => true ; case Some(_) => false } })) {
      uriLevel
    } else {
     last_err match {
        case Some(_) => req.contentError(new Exception(mismatchMessage+value+" "+last_err.get.getMessage(), last_err.get), mismatchCode, priority)
        case None => req.contentError(new Exception(mismatchMessage), mismatchCode, priority)
      }
      -1
    }
  }
}
