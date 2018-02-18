/***
 *   Copyright 2017 Rackspace US, Inc.
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

import java.io.{ByteArrayOutputStream, InputStream, Reader, File}
import java.net.URI

import javax.xml.transform._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.util.XSLErrorDispatcher
import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.macros.TimeFunction._

import scala.language.reflectiveCalls
import scala.xml._

import com.typesafe.scalalogging.slf4j.LazyLogging

import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XsltExecutable
import net.sf.saxon.s9api.XdmAtomicValue
import net.sf.saxon.s9api.XdmDestination
import net.sf.saxon.s9api.Destination

import BuilderHelper._

object WADLDotBuilder {
  private lazy val dotXsltExec : XsltExecutable = timeFunction ("compile /xsl/checker2dot.xsl",
                                                                compiler.compile(new StreamSource(getClass.getResource("/xsl/checker2dot.xsl").toString)))

  object Checker2DotXSLParams {
    val IGNORE_SINKS = "ignoreSinks"
    val NFA_MODE     = "nfaMode"
  }

}

import WADLDotBuilder._

class WADLDotBuilder(protected[wadl] var wadl : WADLNormalizer) extends LazyLogging with XSLErrorDispatcher {

  import Checker2DotXSLParams._

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  def buildFromChecker (in : Source, out: StreamResult, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = timeFunction ("checker2dot", {

    val dest : Destination = {
      if (out.getOutputStream != null) {
        processor.newSerializer(out.getOutputStream)
      } else if (out.getWriter != null) {
        processor.newSerializer(out.getWriter)
      } else {
        processor.newSerializer(new File(new URI(out.getSystemId)))
      }
    }

    val dotTransformer = getXsltTransformer(dotXsltExec, wadl.saxTransformerFactory.getURIResolver,
                                            Map(new QName(IGNORE_SINKS)->new XdmAtomicValue(ignoreSinks),
                                                new QName(NFA_MODE)->new XdmAtomicValue(nfaMode)))
    try {
      handleXSLException({
        dotTransformer.setSource(in)
        dotTransformer.setDestination(dest)
        dotTransformer.transform
      })
    } catch {
      case e : Exception => logger.error(e.getMessage)
                            throw new WADLException ("Checker2Dot Processing Error: "+e.getMessage, e)
    }
  })

  def build (in : Source, out: StreamResult, info : Option[StreamResult], config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    val outChecker = new XdmDestination

    checkerBuilder.build(in, outChecker.getReceiver(processor.getUnderlyingConfiguration), info, config)
    buildFromChecker(outChecker.getXdmNode.asSource, out, ignoreSinks, nfaMode)
  }

  def build (in : Source, out: StreamResult, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(in, out, None, config, ignoreSinks, nfaMode)
  }

  def build(in : (String, InputStream), out: StreamResult, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(new StreamSource(in._2,in._1), out, config, ignoreSinks, nfaMode)
  }

  def build(in : InputStream, out: StreamResult, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build (("test://mywadl.wadl",in), out, config, ignoreSinks, nfaMode)
  }

  def build(in : Reader, out: StreamResult, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
    build(new StreamSource(in), out, config, ignoreSinks, nfaMode)
  }

  def build(in : String, out: StreamResult, config : Config, ignoreSinks : Boolean, nfaMode : Boolean) : Unit = {
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
