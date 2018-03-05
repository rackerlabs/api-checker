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
package com.rackspace.com.papi.components.checker.wadl

import java.io.{InputStream, Reader}
import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.step.StepHandler
import com.rackspace.com.papi.components.checker.step.base.Step

import scala.xml._

class StepBuilder(protected[wadl] var wadl : WADLNormalizer) {

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  def build (in : Source, out : SAXResult, info : Option[StreamResult], config : Config) : Step = {
    //
    //  We use the default config if the config is null.
    //
    var c = config

    if (c == null) {
      c = new Config
    }

    val nextHandler = {
      if (out != null) {
        out.getHandler
      } else {
        null
      }
    }
    val handler = new StepHandler(nextHandler, c)
    checkerBuilder.build(in, new SAXResult(handler), info, c)
    handler.step
  }

  def build (in : Source, out : SAXResult, config : Config) : Step = {
    build (in, out, None, config)
  }

  def build (in : Source, config : Config) : Step = {
    build(in, null, config)
  }

  def build (in : (String, InputStream), out : SAXResult, config : Config) : Step = {
    build (new StreamSource(in._2,in._1), out, config)
  }

  def build(in : InputStream, config : Config) : Step = {
    build(("test://app/mywadl.wadl", in), null, config)
  }

  def build (in : Reader, config : Config) : Step = {
    build(new StreamSource(in), config)
  }

  def build (in : String, config : Config) : Step = {
    build(new StreamSource(in), config)
  }

  def build (in : (String, NodeSeq), config : Config) : Step = {
    build (in, null, config)
  }

  def build (in : NodeSeq, config : Config = null) : Step = {
    build (("test://app/mywadl.wadl",in), config)
  }
}
