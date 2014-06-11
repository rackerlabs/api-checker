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
