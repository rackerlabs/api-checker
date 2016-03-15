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

import javax.xml.transform.stream._
import javax.xml.validation._

class BaseStepSuiteSaxonEE extends BaseStepSuite {
  private val schemaFactorySaxon = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema", "com.saxonica.jaxp.SchemaFactoryImpl", this.getClass.getClassLoader)

  //
  //  Enable 1.1 support in saxon
  //
  schemaFactorySaxon.setProperty("http://saxon.sf.net/feature/xsd-version","1.1")

  val testSchemaSaxon = schemaFactorySaxon.newSchema(new StreamSource(getClass.getResourceAsStream("/xsd/test-urlxsd.xsd")))
}
