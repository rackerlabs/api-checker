/** *
  * Copyright 2014 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MultiSchemaElementsSuiteSaxonEE extends MultiSchemaElementsBaseSuite {
  val useSaxon = true
  val config = createConfigWithSaxonEE(useSaxon)

  val validator_SimpleSame = Validator((localWADLURI, wadl_SimpleSame), config)
  val validator_SimpleDiff = Validator((localWADLURI, wadl_SimpleDiff), config)
  val validator_ElementSame = Validator((localWADLURI, wadl_ElementSame), config)
  val validator_ElementDiff = Validator((localWADLURI, wadl_ElementDiff), config)
  val validator_ElementRepeat = Validator((localWADLURI, wadl_ElementSame), config)

  assert(validator_SimpleSame.config.disableSaxonByteCodeGen == false)
  assert(validator_SimpleDiff.config.disableSaxonByteCodeGen == false)
  assert(validator_ElementSame.config.disableSaxonByteCodeGen == false)
  assert(validator_ElementDiff.config.disableSaxonByteCodeGen == false)
  assert(validator_ElementRepeat.config.disableSaxonByteCodeGen == false)

  assertions_Simple(validator_SimpleSame, true, useSaxon)
  assertions_Simple(validator_SimpleDiff, false, useSaxon)
  assertions_Element(validator_ElementSame, true, useSaxon)
  assertions_Element(validator_ElementDiff, false, useSaxon)
  assertions_Repeat(validator_ElementRepeat, true, useSaxon) // TODO: update to large enough wadl schema in order to crash saxon when byte code generation is triggered.
}
