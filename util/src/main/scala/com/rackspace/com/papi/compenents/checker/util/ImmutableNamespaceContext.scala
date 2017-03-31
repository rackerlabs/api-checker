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

import java.util.Iterator
import javax.xml.XMLConstants._
import javax.xml.namespace.NamespaceContext

import scala.collection.JavaConversions._
import scala.collection.mutable.Map

object ImmutableNamespaceContext {
  def apply (inputNS : Map[String, String]) : ImmutableNamespaceContext = {
    new ImmutableNamespaceContext (inputNS.toMap)
  }

  def apply (inputNS : scala.collection.immutable.Map[String, String]) : ImmutableNamespaceContext = {
    new ImmutableNamespaceContext (inputNS)
  }
}

class ImmutableNamespaceContext private (private val inputNS : scala.collection.immutable.Map[String, String]) extends NamespaceContext {

  //
  //  A map from prefix to ns URI.
  //
  private val prefixToURI : scala.collection.immutable.Map[String, String] = {
    inputNS ++ List("xml" -> "http://www.w3.org/XML/1998/namespace",
                    "xmlns" -> "http://www.w3.org/2000/xmlns/")
  }


  //
  //  A map form ns URI to prefix
  //
  private val uriToPrefix : scala.collection.immutable.Map[String, Set[String]] = {
    val uriMap = Map.empty[String, Set[String]]

    for (prefix <- prefixToURI.keys) {
      val uri = prefixToURI(prefix)
      val uriSet = uriMap.getOrElseUpdate(uri, Set[String]())
      uriMap += (uri -> (uriSet + prefix))
    }

    uriMap.toMap
  }

  override def getNamespaceURI(prefix : String) : String = {
    if (prefix == null) {
      throw new IllegalArgumentException("Can't pass null to getNamespaceURI")
    }
    prefixToURI.getOrElse(prefix, NULL_NS_URI)
  }

  override def getPrefix(namespaceURI : String) : String = {
    if (namespaceURI == null) {
      throw new IllegalArgumentException("Can't pass null to getPrefix")
    }

    if (uriToPrefix.contains(namespaceURI)) {
      uriToPrefix(namespaceURI).head
    } else {
      null
    }
  }

  override def getPrefixes(namespaceURI : String) : Iterator[String] = {
    if (namespaceURI == null) {
      throw new IllegalArgumentException("Can't pass null to getPrefixes")
    }

    if (uriToPrefix.contains(namespaceURI)) {
      uriToPrefix(namespaceURI).toIterator
    } else {
      Set[String]().toIterator //an empty string iterator
    }
  }

  def getAllPrefixes = prefixToURI.keySet

  //
  //  Borrow toString, hashCode, and equals from our immutable
  //  namespace map.
  //
  override def toString = prefixToURI.toString
  override def hashCode = prefixToURI.hashCode
  override def equals(o : Any) = {
    o.asInstanceOf[ImmutableNamespaceContext].prefixToURI.equals(prefixToURI)
  }
}
