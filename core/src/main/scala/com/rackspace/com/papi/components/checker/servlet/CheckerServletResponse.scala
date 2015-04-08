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
package com.rackspace.com.papi.components.checker.servlet

import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.{URI, URISyntaxException}
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import org.w3c.dom.Document

import com.typesafe.scalalogging.slf4j.LazyLogging

import com.netaporter.uri.encoding.PercentEncoder

import com.rackspace.com.papi.components.checker.util.DateUtils
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool._
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool
import com.rackspace.com.papi.components.checker.util.HeaderMap

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

//
//  An HTTP Response with some additional helper functions
//
class CheckerServletResponse(val request : HttpServletResponse) extends HttpServletResponseWrapper(request) {}
