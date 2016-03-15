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

import javax.management.{MalformedObjectNameException, ObjectName}

import com.codahale.metrics.ObjectNameFactory
import com.typesafe.scalalogging.slf4j.LazyLogging

class JmxObjectNameFactory extends ObjectNameFactory with LazyLogging {
  override def createName(`type`: String, domain: String, name: String): ObjectName = {
    try {
      val nameDot = name.lastIndexOf('.')
      val nameVal = name.substring(nameDot+1)
      val scopeDot = name.substring(0, nameDot).lastIndexOf('.')
      val scopeVal = name.substring(scopeDot+1, nameDot)
      val typeVal = name.substring(0, scopeDot)
      val nameBuilder = new StringBuilder(domain)
      nameBuilder.append(':')
      if(typeVal.nonEmpty) {
        nameBuilder.append("type=")
        nameBuilder.append(typeVal)
        nameBuilder.append(',')
      }
      if(scopeVal.nonEmpty) {
        nameBuilder.append("scope=")
        nameBuilder.append(scopeVal)
        nameBuilder.append(',')
      }
      nameBuilder.append("name=")
      nameBuilder.append(nameVal)

      val objectName: ObjectName = new ObjectName(nameBuilder.toString)
      if (objectName.isPattern) {
        new ObjectName(domain, "name", ObjectName.quote(name))
      } else {
        objectName
      }
    }
    catch {
      case e: MalformedObjectNameException =>
        try {
          new ObjectName(domain, "name", ObjectName.quote(name))
        }
        catch {
          case e1: MalformedObjectNameException =>
            logger.warn("Unable to register {}.{}", domain, name, e1)
            throw new RuntimeException(e1)
        }
    }
  }
}
