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
package com.rackspace.papi.components.checker.filter

import java.io.File

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.xml.transform.sax.SAXSource
import org.xml.sax.InputSource

import com.rackspace.com.papi.components.checker.Validator
import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.ValidatorException
import com.rackspace.com.papi.components.checker.handler._

/**
 * A filter which can be used to test api-checker.
 *
 * <b>This is not the Repose api-validator filter.</b>
 */
class ValidatorFilter extends Filter {
  private[this] var validator : Validator = null;

  override def init(config : FilterConfig) : Unit = {
    val wadlRef = config.getInitParameter("WADLRef")

    if (wadlRef == null) {
      throw new ServletException ("Missing required init paramater WADLRef")
    }

    val dot : File = File.createTempFile("checker", ".dot")

    System.out.println ("Dot file is at: "+dot)

    val resultHandler = new DispatchResultHandler(List[ResultHandler](new SaveDotHandler(dot, true, true),
                                                                      new ServletResultHandler(),
                                                                      new InstrumentedHandler()))

    val conf = new Config
    conf.resultHandler = resultHandler
    conf.xsdEngine = "SaxonEE"
    conf.checkWellFormed = true
    conf.checkXSDGrammar = true
    conf.checkElements = true
    conf.checkHeaders = true
    conf.xpathVersion = 2
    conf.checkPlainParams = true
    conf.doXSDGrammarTransform = true
    conf.enablePreProcessExtension = true
    conf.enableMessageExtension = true
    conf.joinXPathChecks = true
    conf.enableIgnoreXSDExtension = true
    conf.xslEngine = "XalanC"
    conf.checkJSONGrammar = true
    conf.enableIgnoreJSONSchemaExtension = true

    validator = Validator("Test Validator",new SAXSource(new InputSource(wadlRef)), conf)
  }

  override def doFilter (req : ServletRequest, resp : ServletResponse, chain : FilterChain) : Unit = {
    try {
      validator.validate (req.asInstanceOf[HttpServletRequest], resp.asInstanceOf[HttpServletResponse], chain)
    } catch {
      case v : ValidatorException => throw new ServletException("Error while calling validator", v)
    }
  }

  override def destroy : Unit = {
    validator.destroy
    validator = null
  }
}
