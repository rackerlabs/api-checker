package com.rackspace.com.papi.components.checker

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.dom._

import com.rackspace.com.papi.components.checker.wadl.StepBuilder

import com.rackspace.com.papi.components.checker.step.Step
import com.rackspace.com.papi.components.checker.step.Result

import com.rackspace.com.papi.components.checker.handler.ResultHandler
import com.rackspace.com.papi.components.checker.handler.NullHandler

import com.rackspace.com.papi.components.checker.servlet._

import org.w3c.dom.Document

class ValidatorException(msg : String, cause : Throwable) extends Throwable(msg, cause) {}

object Validator {
  def apply (startStep : Step, resultHandler : ResultHandler) : Validator = {
    resultHandler.init(None)
    new Validator(startStep, resultHandler)
  }

  def apply (in : Source, removeDups : Boolean, resultHandler : ResultHandler = new NullHandler) : Validator = {
    val builder = new StepBuilder
    val transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)

    if (!transformerFactory.getFeature(SAXTransformerFactory.FEATURE)) {
      throw new RuntimeException("Need a SAX-compatible TransformerFactory!")
    }

    val transHandler = transformerFactory.asInstanceOf[SAXTransformerFactory].newTransformerHandler()
    val domResult = new DOMResult
    transHandler.setResult(domResult)
    val step = builder.build(in, new SAXResult(transHandler), removeDups)

    resultHandler.init(Some(domResult.getNode.asInstanceOf[Document]))
    new Validator(step, resultHandler)
  }
}

class Validator private (val startStep : Step, val resultHandler : ResultHandler) {
  def validate (req : HttpServletRequest, res : HttpServletResponse) : Result = {
    try {
      val creq = new CheckerServletRequest (req)
      val cres = new CheckerServletResponse(res)
      val result = startStep.check (creq, cres, 0).get
      resultHandler.handle(creq, cres, result)
      result
    } catch {
      case v : ValidatorException => throw v
      case e => throw new ValidatorException("Error while validating request", e)
    }
  }
}
