package com.rackspace.com.papi.components.checker.util

import java.util.Iterator
import java.util.ArrayList
import javax.xml.namespace.NamespaceContext

import scala.collection.mutable.Map

object ImmutableNamespaceContext {
  def apply (inputNS : Map[String, String]) : ImmutableNamespaceContext = {
    new ImmutableNamespaceContext (inputNS)
  }
}

class ImmutableNamespaceContext private (private val inputNS : Map[String, String]) extends NamespaceContext {

  //
  //  A map from prefix to ns URI.
  //
  private val prefixToURI : scala.collection.immutable.Map[String, String] = {
    inputNS.toMap ++ List("xml" -> "http://www.w3.org/XML/1998/namespace",
                          "xmlns" -> "http://www.w3.org/2000/xmlns/")
  }


  //
  //  A map form ns URI to prefix
  //
  private val uriToPrefix : scala.collection.immutable.Map[String, String] = {
    val uriMap = Map.empty[String, String]

    for (key <- prefixToURI.keys) {
      uriMap += (prefixToURI(key) -> key)
    }

    uriMap.toMap
  }

  override def getNamespaceURI(prefix : String) : String = prefixToURI(prefix)
  override def getPrefix(namespaceURI : String) : String = uriToPrefix(namespaceURI)
  override def getPrefixes(namespaceURI : String) : Iterator[String] = {
    val l : ArrayList[String] = new ArrayList[String]()
    val p : String = uriToPrefix(namespaceURI)

    if (p != null) {
      l.add(p)
    }

    l.iterator()
  }
}
