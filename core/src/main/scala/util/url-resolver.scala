package com.rackspace.com.papi.components.checker.util

import java.net.URI
import java.io.File

//
//  Transform requires absolute URL as input, the url resolver tries
//  to construct an full URI from a system id.
//
object URLResolver {
  def toAbsoluteSystemId(systemId : String) : String = {
    toAbsoluteSystemId(systemId, (new File(System.getProperty("user.dir")).toURI().toString()))
  }

  def toAbsoluteSystemId(systemId : String, base : String) : String = {
    val inURI = new URI(systemId)
    if (!inURI.isAbsolute()) {
      (new URI(base)).resolve(systemId).toString()
    } else {
      systemId
    }
  }
}
