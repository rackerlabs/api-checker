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


package com.rackspace.com.papi.components.checker.servlet

import scala.collection.mutable.Map

import java.util.Set
import java.util.HashSet
import java.util.Collection
import java.util.Enumeration
import java.util.LinkedList
import java.util.AbstractMap.SimpleEntry

import javax.servlet.http.HttpServletRequest

import net.sf.saxon.om.Sequence

import com.fasterxml.jackson.databind.ObjectMapper

import collection.JavaConverters._

import com.rackspace.com.papi.components.checker.util.JSONConverter

object HttpServletRequestMap {
  private val mapper = new ObjectMapper

  //
  // Supported Calls
  //
  protected val HEADERS       = "headers"
  protected val METHOD        = "method"
  protected val REQUEST_URI   = "uri"

  //
  //  Set of supported calls
  //
  protected val SUPPORTED_CALLS : Set[String] = new HashSet[String](
    List(HEADERS, METHOD, REQUEST_URI).asJava.asInstanceOf[Collection[String]])
}

import HttpServletRequestMap._

class HttpServletRequestMap(protected val request : HttpServletRequest) extends UnimplementedMap[String, Object] {
  //
  //  A cache from header name to array of strings to avoid converting
  //  when a header is accessed more than once.
  //
  private val headerValuesCache = Map[String, java.util.List[String]]()
  private lazy val headerNameSet : Set[String] = new HashSet[String](enumToJavaList(request.getHeaderNames()).asScala.map(_.toLowerCase).asJava)

  private lazy val headers = new UnimplementedMap[String, java.util.List[String]] {
    override def containsKey(key : Any) = request.getHeader(key.asInstanceOf[String]) != null
    override def isEmpty = request.getHeaderNames match {
      case null => true
      case enum : Enumeration[String] => !enum.hasMoreElements
    }
    override def size = headerNameSet.size
    override def keySet = headerNameSet
    override def get(key : Any) : java.util.List[String] = {
      val k = key.asInstanceOf[String]
      headerValuesCache.getOrElseUpdate(k, enumToJavaList(request.getHeaders(k)))
    }
    override def entrySet() : Set[java.util.Map.Entry[String,java.util.List[String]]] = {
      new HashSet[java.util.Map.Entry[String,java.util.List[String]]](keySet.toArray(Array[String]()).map((s : String)
                                                                                                  => new SimpleEntry(s, get(s))).toList.asJava)

    }
  }

  //
  // Implementation of a map that gets access to request data.
  //
  override def containsKey(key: Any): Boolean = SUPPORTED_CALLS.contains(key.asInstanceOf[String])
  override def isEmpty(): Boolean = false
  override def size(): Int = SUPPORTED_CALLS.size
  override def keySet(): java.util.Set[String] = SUPPORTED_CALLS
  override def get(key: Any): Object = key match {
    case METHOD       => request.getMethod
    case REQUEST_URI  => request.getRequestURI
    case HEADERS      => headers
    case _ => null
  }
  override def entrySet(): Set[java.util.Map.Entry[String,Object]] = {
    new HashSet[java.util.Map.Entry[String,Object]](keySet.toArray(Array[String]()).map((s : String)
                                                                                        => new SimpleEntry(s, get(s))).toList.asJava)
  }

  private def enumToJavaList (enum : Enumeration[String]) : java.util.List[String] = enum match {
    case null => new LinkedList()
    case _ => enum.asScala.toList.asJava
  }
}
