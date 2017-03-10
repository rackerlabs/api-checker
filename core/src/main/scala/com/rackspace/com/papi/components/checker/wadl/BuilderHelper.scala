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

import scala.annotation.tailrec

import javax.xml.transform.Source
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.URIResolver

import org.xml.sax.XMLReader
import org.xml.sax.EntityResolver
import org.xml.sax.SAXException

import org.xml.sax.helpers.XMLReaderFactory

import com.rackspace.cloud.api.wadl.util.LogErrorListener

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.macros.TimeFunction._

import com.typesafe.scalalogging.slf4j.LazyLogging

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XsltExecutable
import net.sf.saxon.s9api.XsltTransformer
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XdmValue

import net.sf.saxon.serialize.MessageWarner

/**
 * Common functionality shared by builders
 */

protected object BuilderHelper extends LazyLogging {

  val processor = newProcessor(true)
  val compiler = processor.newXsltCompiler

  compiler.setErrorListener(new LogErrorListener)


  private val transformerFactory = new net.sf.saxon.TransformerFactoryImpl
  def idTransform = {
    val idt = transformerFactory.newTransformer()
    idt.setErrorListener (new LogErrorListener)
    idt
  }

  def newProcessor (licensedEdition : Boolean) : Processor = {
    val p = new Processor(licensedEdition)
    val dynLoader = p.getUnderlyingConfiguration.getDynamicLoader
    dynLoader.setClassLoader(getClass.getClassLoader)
    p
  }

  //
  //  Given XSLTExect and an optional set of XSLT parameters, creates an XsltTransformer
  //
  def getXsltTransformer (xsltExec : XsltExecutable, resolver : URIResolver, params : Map[QName, XdmValue]=Map[QName, XdmValue]()) : XsltTransformer = {
    val t = xsltExec.load
    t.setErrorListener (new LogErrorListener)
    t.getUnderlyingController.setMessageEmitter(new MessageWarner)
    t.setURIResolver(resolver)
    for ((param, value) <- params) {
      t.setParameter(param, value)
    }
    t
  }

  //
  //  Create a reader with xinclude if possible
  //
  def XMLReader(resolver : Option[EntityResolver] = None) : XMLReader = {
    val reader = XMLReaderFactory.createXMLReader()
    if (resolver != None) {
      reader.setEntityResolver(resolver.get)
    }
    try {
      reader.setFeature ("http://apache.org/xml/features/xinclude", true)
    } catch {
      case se: SAXException => logger.warn ("The XML parser does not seem to support XInclude! XIncludes will not be resolved!")
    }
    reader
  }

  //
  // Given a source, returns a source with the appropriate
  // EntityResolver.
  //
  def Source(in : Source, resolver : Option[EntityResolver] = None) : Source = {
    val inputSource = SAXSource.sourceToInputSource(in)
    if (inputSource == null) {
      logger.warn (
        "I couldn't convert the source to a SAX stream, that means that if you used externaly defined "+
        "DTDs or entities I can't reliably report them as dependecies. Continuing to normalize WADL...")
      in
    } else {
      val ss = new SAXSource(inputSource)
      ss.setXMLReader(XMLReader(resolver))
      ss
    }
  }

  //
  //  Checker transform functions take a source and validator config
  //  and produce a source for the next part in the chain -- which is
  //  the result of trannsform.
  //
  type CheckerTransform = (Source, Config) => Source

  @tailrec
  def applyBuildSteps(steps : List[CheckerTransform], current : Source, config : Config) : Source = steps match {
    case transform :: transforms => applyBuildSteps(transforms, transform(current, config), config)
    case _ => current
  }

  //
  //  Serializes on standard error the input source then pass it
  //  along.  This is a helpful debuging CheckerTransform you can
  //  place it between steps in applyBuildSteps to see what's going
  //  on.
  //
  def serialize (in : Source, c : Config) : Source = timeFunction("serialize", {
    idTransform.transform(in, new StreamResult(System.err))
    in
  })
}
