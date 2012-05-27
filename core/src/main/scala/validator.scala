package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain

import javax.xml.transform._
import javax.xml.transform.sax._
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

class ValidatorException(msg : String, cause : Throwable) extends Throwable(msg, cause) {}

object Validator {
  def apply (startStep : Step, config : Config) : Validator = {
    config.resultHandler.init(None)
    new Validator(startStep, config)
  }

  def apply (in : Source, config : Config = new Config) : Validator = {
    val builder = new StepBuilder(config)
    val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)

    if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
      throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
    }

    val transHandler = transformerFactory.asInstanceOf[SAXTransformerFactory].newTransformerHandler()
    val domResult = new DOMResult
    transHandler.setResult(domResult)
    val step = builder.build(in, new SAXResult(transHandler), config.removeDups)

    config.resultHandler.init(Some(domResult.getNode.asInstanceOf[Document]))
    new Validator(step, config)
  }

  def apply (in : Source, resultHandler : ResultHandler) : Validator = {
    val config = new Config
    config.resultHandler = resultHandler

    apply(in, config)
  }

  def apply (in : (String, InputStream), config : Config) : Validator = {
    val wadlParserFactory = SAXParserFactory.newInstance()
    val schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")

    wadlParserFactory.setNamespaceAware(true)
    wadlParserFactory.setValidating(true)
    wadlParserFactory.setSchema(schemaFactory.newSchema(getClass().getClassLoader().getResource("wadl.xsd")))

    val xmlReader = wadlParserFactory.newSAXParser().getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    apply (new SAXSource(xmlReader, inputSource), config)
  }

  def apply (in : InputStream, config : Config) : Validator = {
    apply (("", in), config)
  }
}

class Validator private (val startStep : Step, val config : Config) {

  private val resultHandler = {
    if (config == null) {
      (new Config).resultHandler
    } else {
      config.resultHandler
    }
  }

  def validate (req : HttpServletRequest, res : HttpServletResponse, chain : FilterChain) : Result = {
    try {
      val creq = new CheckerServletRequest (req)
      val cres = new CheckerServletResponse(res)
      val result = startStep.check (creq, cres, chain, 0).get
      resultHandler.handle(creq, cres, chain, result)
      result
    } catch {
      case v : ValidatorException => throw v
      case e => throw new ValidatorException("Error while validating request", e)
    }
  }
}
