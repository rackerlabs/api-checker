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

import scala.xml._

import java.io.File
import java.io.ByteArrayInputStream
import java.io.StringWriter

import java.util.Enumeration

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletInputStream
import javax.servlet.FilterChain

import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.Transformer
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

import scala.collection.mutable.HashMap
import scala.collection.immutable.TreeMap

import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.servlet.RequestAttributes._

import com.rackspace.com.papi.components.checker.util.XMLParserPool
import com.rackspace.com.papi.components.checker.util.ObjectMapperPool
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool

import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

import scala.language.implicitConversions
import scala.collection.JavaConversions._

import org.w3c.dom.Document

/**
 * Exception thrown by the assert result handler,
 * if a request fails to validate
 */
class ResultFailedException(val msg : String, val req : CheckerServletRequest,
                            val resp : CheckerServletResponse, val chain : FilterChain, val result : Result)
   extends Exception(msg){}
