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

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterChain

import java.lang.management._
import javax.management._

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
import java.io.StringWriter

import com.rackspace.com.papi.components.checker.step.base.{Step, StepContext}
import com.rackspace.com.papi.components.checker.step.results.Result

import scala.xml._

import com.rackspace.com.papi.components.checker.wadl.StepBuilder
import com.rackspace.com.papi.components.checker.wadl.WADLDotBuilder

import com.rackspace.com.papi.components.checker.handler.ResultHandler

import com.rackspace.com.papi.components.checker.servlet._

import com.rackspace.com.papi.components.checker.util.IdentityTransformPool

import org.w3c.dom.Document

import org.apache.commons.codec.digest.DigestUtils.sha1Hex

import com.yammer.metrics.scala.Instrumented
import com.yammer.metrics.scala.Meter
import com.yammer.metrics.scala.Timer
import com.yammer.metrics.scala.MetricsGroup
import com.yammer.metrics.util.PercentGauge

trait ValidatorMBean {
  def checkerXML : String
  def checkerDOT : String
  def getXmlSHA1 : String
  def getDotSHA1 : String
}
