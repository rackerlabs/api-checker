package com.rackspace.com.papi.components.checker.util

import java.util.Iterator
import java.util.ArrayList

import javax.xml.namespace.NamespaceContext
import javax.xml.XMLConstants._

import scala.collection.mutable.Map

import scala.collection.JavaConversions._

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
}
