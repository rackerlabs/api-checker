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

import scala.collection.immutable.TreeMap

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

object CaseInsensitiveStringOrdering extends Ordering[String] {
  override def compare(x: String, y: String): Int = x compareToIgnoreCase y
}

class HeaderMap private (val headers : TreeMap[String, List[String]])
      extends Map[String, List[String]] {

  def this() = this(new TreeMap[String, List[String]]()(CaseInsensitiveStringOrdering))

  def addHeader (name : String, value : String) : HeaderMap = {
    new HeaderMap(headers + (name -> (headers.getOrElse(name, List()) :+ value)))
  }

  def removeHeader (name : String, value : String) : HeaderMap = {
    get(name) match {
      case Some(l) => l.filter(a => a != value) match {
        case h :: t => removeHeaders(name).addHeaders(name, h::t)
        case Nil => removeHeaders(name)
      }
      case None => this
    }
  }

  def addHeaders (name : String, values : List[String]) : HeaderMap = {
    new HeaderMap(headers + (name -> (headers.getOrElse(name, List()) ::: values)))
  }

  def addHeaders (otherHeaders : HeaderMap) : HeaderMap = {
    var retHeaders = this
    otherHeaders.foreach (h_v => retHeaders = retHeaders.addHeaders(h_v._1, h_v._2))
    retHeaders
  }

  def removeHeaders (name : String) : HeaderMap = {
    (this - name).asInstanceOf[HeaderMap]
  }

  // Members declared in scala.collection.immutable.Map
  override def +[B1 >: List[String]](kv: (String, B1)): scala.collection.immutable.Map[String,B1] = {
    new HeaderMap((headers + kv).asInstanceOf[TreeMap[String, List[String]]])
  }

  // Members declared in scala.collection.MapLike
  override def -(key: String): scala.collection.immutable.Map[String,List[String]] = {
    new HeaderMap ((headers - key).asInstanceOf[TreeMap[String, List[String]]])
  }

  override def get(key: String): Option[List[String]] = headers.get(key)
  override def iterator: Iterator[(String, List[String])] = headers.iterator
 }
