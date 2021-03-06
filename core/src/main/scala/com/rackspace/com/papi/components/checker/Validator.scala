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
package com.rackspace.com.papi.components.checker

import java.io.{InputStream, StringWriter}
import java.lang.management._
import javax.management._
import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.xml.transform._
import javax.xml.transform.dom._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.codahale.metrics.RatioGauge.Ratio
import com.codahale.metrics._
import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.com.papi.components.checker.util.{IdentityTransformPool, Instrumented, JmxObjectNameFactory}
import com.rackspace.com.papi.components.checker.wadl.{StepBuilder, WADLDotBuilder}
import org.apache.commons.codec.digest.DigestUtils.sha1Hex
import org.w3c.dom.Document

object Validator {
  /** The application wide metrics registry. */
  val metricRegistry = new MetricRegistry()
  val metricDomain = getClass.getPackage.getName
  val reporter = JmxReporter
    .forRegistry(metricRegistry)
    .inDomain(metricDomain)
    .createsObjectNamesWith(new JmxObjectNameFactory())
    .build()
  reporter.start()

  def apply (name : String, startStep : Step, config : Config) : Validator = {
    val validator = new Validator(name, startStep, config)
    config.resultHandler.init(validator, None)
    validator
  }

  def apply (name : String, in : Source, info : Option[StreamResult], config : Config) : Validator = {
    val builder = new StepBuilder()
    val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass.getClassLoader)

    if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
      throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
    }

    val transHandler = transformerFactory.asInstanceOf[SAXTransformerFactory].newTransformerHandler()
    val domResult = new DOMResult
    transHandler.setResult(domResult)
    val step = builder.build(in, new SAXResult(transHandler), info, config)

    val checker = Some(domResult.getNode.asInstanceOf[Document])
    val validator = new Validator(name, step, config, checker)
    config.resultHandler.init(validator, checker)

    validator
  }

  def apply (name : String, in : Source, config : Config = new Config) : Validator = {
    apply(name, in, None, config)
  }

  def apply (name : String, in : Source, resultHandler : ResultHandler) : Validator = {
    val config = new Config
    config.resultHandler = resultHandler

    apply(name, in, config)
  }

  def apply (name : String, in : (String, InputStream), config : Config) : Validator = {
    apply (name, new StreamSource(in._2, in._1), config)
  }

  def apply (name : String, in : InputStream, config : Config) : Validator = {
    apply (name, ("test://path/to/mywadl.wadl", in), config)
  }

  //
  // The following are for backward compatability
  //
  def apply (startStep : Step, config : Config) : Validator = apply(null, startStep, config)
  def apply (in : Source, config : Config) : Validator =  apply(null, in, config)
  def apply (in : Source) : Validator =  apply(null, in, new Config)
  def apply (in : Source, resultHandler : ResultHandler) : Validator = apply(null, in, resultHandler)
  def apply (in : (String, InputStream), config : Config) : Validator = apply(null, in, config)
  def apply (in : InputStream, config : Config) : Validator = apply (null, in, config)
}

import Validator._

class Validator private (private val _name : String, val startStep : Step, val config : Config, val checker : Option[Document] = None) extends Instrumented with ValidatorMBean {

  val name = _name match {
    case null => Integer.toHexString(hashCode())
    case _ => _name
  }


  private val platformMBeanServer = ManagementFactory.getPlatformMBeanServer
  private val objectName = new ObjectName(s"$metricDomain:type=Validator,scope=$name,name=checker")

  private val TIMER_NAME       = "validation-timer"
  private val FAIL_METER_NAME  = "fail-meter"
  private val FAIL_METER_EVENT = "fail"
  private val FAIL_RATE_NAME   = "fail-rate"

  //
  //  Register with MBeanServer if the checker document is defined.
  //
  if (checker.isDefined) {
    platformMBeanServer.registerMBean(this, objectName)
  }

  private val xml = {
    checker match {
      case None => null
      case _ =>
        val transformer = IdentityTransformPool.borrowTransformer
        try {
          val writer = new StringWriter()
          transformer.setOutputProperty (OutputKeys.INDENT, "yes")
          transformer.transform (new DOMSource(checker.get), new StreamResult(writer))
          writer.toString
        } finally {
          if (transformer != null) {
            IdentityTransformPool.returnTransformer(transformer)
          }
        }
    }
  }

  private val xmlSHA1 = xml match {
      case null => null
      case _ => sha1Hex(xml)
  }

  private val dot = {
    checker match {
      case None => null
      case _ =>
        val writer = new StringWriter()
        val dotBuilder = new WADLDotBuilder()

        dotBuilder.buildFromChecker (new DOMSource(checker.get), new StreamResult (writer), true, true)
        writer.toString
    }
  }

  private val dotSHA1 = dot match {
    case null => null
    case _ => sha1Hex(dot)
  }

  private class ValidatorFailGauge(private val timer : Timer,
                                   private val failMeter : Meter) extends RatioGauge {
    override def getRatio: Ratio = Ratio.of(failMeter.getOneMinuteRate, timer.getOneMinuteRate)
  }

  private val timer = metricRegistry.timer(MetricRegistry.name(getRegistryClassName(getClass), name, TIMER_NAME))
  private val failMeter = metricRegistry.meter(MetricRegistry.name(getRegistryClassName(getClass), name, FAIL_METER_NAME, FAIL_METER_EVENT))
  gaugeOrAdd(
    MetricRegistry.name(getRegistryClassName(getClass), name, FAIL_RATE_NAME),
    new ValidatorFailGauge(timer, failMeter)
  )

  private val resultHandler = {
    if (config == null) {
      (new Config).resultHandler
    } else {
      config.resultHandler
    }
  }

  def validate (req : HttpServletRequest, res : HttpServletResponse, chain : FilterChain) : Result = {
    val context = timer.time()
    try {
      val creq = new CheckerServletRequest (req)
      val cres = new CheckerServletResponse(res)
      val result = startStep.check (creq, cres, chain, StepContext(handler = Option(resultHandler))).get
      resultHandler.handle(creq, cres, chain, result)
      if (!result.valid) failMeter.mark()
      result
    } catch {
      case v : ValidatorException => throw v
      case e : Exception  => throw new ValidatorException("Error while validating request", e)
    } finally {
      context.stop
    }
  }

  def destroy() : Unit = {
    resultHandler.destroy

    if (checker.isDefined) {
      platformMBeanServer.unregisterMBean(objectName)
    }

    val registryClassName = getRegistryClassName(getClass)
    metricRegistry.remove(MetricRegistry.name(registryClassName, name, TIMER_NAME))
    metricRegistry.remove(MetricRegistry.name(registryClassName, name, FAIL_METER_NAME))
    metricRegistry.remove(MetricRegistry.name(registryClassName, name, FAIL_RATE_NAME))
  }

  //
  // MBean Impl
  //
  override def checkerXML = xml
  override def checkerDOT = dot
  override def getXmlSHA1 = xmlSHA1
  override def getDotSHA1 = dotSHA1
}
