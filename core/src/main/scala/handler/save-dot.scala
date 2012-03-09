package com.rackspace.com.papi.components.checker.handler

import java.io.File

import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder
import com.rackspace.com.papi.components.checker._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.Result

import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import javax.servlet.FilterChain

import org.w3c.dom.Document

class SaveDotHandler(val out : File, val ignoreSinks : Boolean, nfaMode : Boolean) extends ResultHandler {

  val dotBuilder = new WADLDotBuilder()

  def init (checker : Option[Document]) : Unit = {
    if (checker != None) {
      dotBuilder.buildFromChecker (new DOMSource(checker.get), new StreamResult (out), ignoreSinks, nfaMode)
    }
  }
  def handle (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, result : Result)  : Unit = {}
}
