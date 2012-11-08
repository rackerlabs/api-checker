package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.Reader
import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.Converters._

import org.xml.sax.InputSource

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.step.Step
import com.rackspace.com.papi.components.checker.step.StepHandler

class StepBuilder(protected[wadl] var wadl : WADLNormalizer) {

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  def this() = this(null)

  def build (in : Source, out : SAXResult, config : Config) : Step = {
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
    checkerBuilder.build(in, new SAXResult(handler), c)
    handler.step
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
