package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.transform.dom._
import javax.xml.validation._

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.Reader

import scala.xml._

import com.rackspace.com.papi.components.checker.wadl.StepBuilder

import com.rackspace.com.papi.components.checker.step.Step
import com.rackspace.com.papi.components.checker.step.Result

import com.rackspace.com.papi.components.checker.handler.ResultHandler

import com.rackspace.com.papi.components.checker.servlet._

import org.w3c.dom.Document

import com.yammer.metrics.scala.Instrumented

class ValidatorException(msg : String, cause : Throwable) extends Throwable(msg, cause) {}

object Validator {
  def apply (startStep : Step, config : Config) : Validator = {
    config.resultHandler.init(None)
    new Validator(startStep, config)
  }

  def apply (in : Source, config : Config = new Config) : Validator = {
    val builder = new StepBuilder()
    val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)

    if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
      throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
    }

    val transHandler = transformerFactory.asInstanceOf[SAXTransformerFactory].newTransformerHandler()
    val domResult = new DOMResult
    transHandler.setResult(domResult)
    val step = builder.build(in, new SAXResult(transHandler), config)

    config.resultHandler.init(Some(domResult.getNode.asInstanceOf[Document]))
    new Validator(step, config)
  }

  def apply (in : Source, resultHandler : ResultHandler) : Validator = {
    val config = new Config
    config.resultHandler = resultHandler

    apply(in, config)
  }

  def apply (in : (String, InputStream), config : Config) : Validator = {
    apply (new StreamSource(in._2, in._1), config)
  }

  def apply (in : InputStream, config : Config) : Validator = {
    apply (("test://path/to/mywadl.wadl", in), config)
  }
}

class Validator private (val startStep : Step, val config : Config) extends Instrumented {

  private val timer = metrics.timer(Integer.toHexString(hashCode()))

  private val resultHandler = {
    if (config == null) {
      (new Config).resultHandler
    } else {
      config.resultHandler
    }
  }

  def validate (req : HttpServletRequest, res : HttpServletResponse, chain : FilterChain) : Result = {
    val context = timer.timerContext()
    try {
      val creq = new CheckerServletRequest (req)
      val cres = new CheckerServletResponse(res)
      val result = startStep.check (creq, cres, chain, 0).get
      resultHandler.handle(creq, cres, chain, result)
      result
    } catch {
      case v : ValidatorException => throw v
      case e => throw new ValidatorException("Error while validating request", e)
    } finally {
      context.stop
    }
  }
}
