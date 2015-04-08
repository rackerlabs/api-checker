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
package com.rackspace.com.papi.components.checker.wadl

import scala.language.reflectiveCalls

import scala.xml._

import java.net.URI
import java.net.URISyntaxException

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.Reader

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._
import javax.xml.validation._

import org.xml.sax.XMLReader
import org.xml.sax.InputSource

import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.util.LogErrorListener


import com.rackspace.com.papi.components.checker.Config

import com.typesafe.scalalogging.slf4j.LazyLogging

import net.sf.saxon.Controller

/**
 * An exception when transating the WADL into a checker.
 */
class WADLException(private val msg : String, private val cause : Throwable) extends Throwable(msg, cause) {}
