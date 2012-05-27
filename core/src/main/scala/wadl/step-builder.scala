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

class StepBuilder(protected[wadl] var wadl : WADLNormalizer, private var config : Config) {

  private val checkerBuilder = new WADLCheckerBuilder(wadl)

  if (wadl == null) {
    wadl = checkerBuilder.wadl
  }

  if (config == null) {
    config = new Config
  }

  def this() = this(null, null)

  def this(config : Config) = this(null, config)

  def this(wadl : WADLNormalizer) = this (wadl, null)

  def build (in : Source, out : SAXResult, removeDups : Boolean) : Step = {
    val nextHandler = {
      if (out != null) {
        out.getHandler
      } else {
        null
      }
    }
    val handler = new StepHandler(nextHandler, config)
    checkerBuilder.build(in, new SAXResult(handler), removeDups, true)
    handler.step
  }

  def build (in : Source, removeDups : Boolean) : Step = {
    build(in, null, removeDups)
  }

  def build (in : (String, InputStream), out : SAXResult, removeDups : Boolean) : Step = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    val inputSource = new InputSource(in._2)
    inputSource.setSystemId(in._1)
    build (new SAXSource(xmlReader, inputSource), out, removeDups)
  }

  def build(in : InputStream, removeDups : Boolean) : Step = {
    build(("test://mywadl.wadl", in), null, removeDups)
  }

  def build (in : Reader, removeDups : Boolean) : Step = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), removeDups)
  }

  def build (in : String, removeDups : Boolean) : Step = {
    val xmlReader = wadl.newSAXParser.getXMLReader()
    build(new SAXSource(xmlReader, new InputSource(in)), removeDups)
  }

  def build (in : (String, NodeSeq), removeDups : Boolean) : Step = {
    build (in, null, removeDups)
  }

  def build (in : NodeSeq, removeDups : Boolean = false) : Step = {
    build (("test://mywadl.wadl",in), removeDups)
  }
}
