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

import java.io.{ByteArrayOutputStream, InputStream, Reader}
import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.util.XSLErrorDispatcher
import com.rackspace.com.papi.components.checker.Config
import net.sf.saxon.jaxp.TransformerImpl

import scala.language.reflectiveCalls
import scala.xml._


object WADLDotBuilder {
  private val _wadl = new WADLNormalizer // Static WADL normalizer used simply to build templates

  private val dotTemplates : Templates = _wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass.getResource("/xsl/checker2dot.xsl").toString))

  object Checker2DotXSLParams {
    val IGNORE_SINKS = "ignoreSinks"
    val NFA_MODE     = "nfaMode"
  }

}

import WADLDotBuilder._

class WADLDotBuilder(protected[wadl] var wadl : WADLNormalizer) extends XSLErrorDispatcher {

  import Checker2DotXSLParams._

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  def buildFromChecker (in : Source, out: Result, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val dotHandler = wadl.saxTransformerFactory.newTransformerHandler(dotTemplates)
    val transformer = dotHandler.getTransformer
    transformer.setParameter (IGNORE_SINKS, ignoreSinks)
    transformer.setParameter (NFA_MODE, nfaMode)
    transformer.asInstanceOf[TransformerImpl].addLogErrorListener
    transformer.setURIResolver(wadl.saxTransformerFactory.getURIResolver)

    handleXSLException({
      transformer.transform (in, out)
    })
  }

  def build (in : Source, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val dotHandler = wadl.saxTransformerFactory.newTransformerHandler(dotTemplates)
    val transformer = dotHandler.getTransformer
    dotHandler.setResult(out)
    transformer.setParameter (IGNORE_SINKS, ignoreSinks)
    transformer.setParameter (NFA_MODE, nfaMode)
    transformer.asInstanceOf[TransformerImpl].addLogErrorListener
    transformer.setURIResolver(wadl.saxTransformerFactory.getURIResolver)

    handleXSLException({
      checkerBuilder.build(in, new SAXResult(dotHandler), config)
    })
  }

  def build(in : (String, InputStream), out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(new StreamSource(in._2,in._1), out, config, ignoreSinks, nfaMode)
  }

  def build(in : InputStream, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build (("test://mywadl.wadl",in), out, config, ignoreSinks, nfaMode)
  }

  def build(in : Reader, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(new StreamSource(in), out, config, ignoreSinks, nfaMode)
  }

  def build(in : String, out: Result, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(new StreamSource(in), out, config, ignoreSinks, nfaMode)
  }

  def build (in : (String, NodeSeq), config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : String = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut), config, ignoreSinks, nfaMode)
    bytesOut.toString
  }

  def build (in: NodeSeq, config : Config = null,
             ignoreSinks: Boolean = true,
             nfaMode: Boolean = true) : String = {
    build (("test://mywadl.wadl",in), config, ignoreSinks, nfaMode)
  }
}
