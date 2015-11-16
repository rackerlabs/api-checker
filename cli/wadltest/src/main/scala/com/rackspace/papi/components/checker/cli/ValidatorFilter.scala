/***
 *   Copyright 2015 Rackspace US, Inc.
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
package com.rackspace.papi.components.checker.cli

import java.io.File
import javax.servlet.{Filter, FilterChain, FilterConfig, ServletException, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.xml.transform.sax.SAXSource

import com.rackspace.com.papi.components.checker.{Config, Validator, ValidatorException}
import com.rackspace.com.papi.components.checker.handler._
import org.xml.sax.InputSource


object ValidatorFilter {
  val VALIDATOR_ATTRIB = "com.rackspace.papi.components.checker.cli.wadltest.validator"
}

import ValidatorFilter._

/**
 * A filter which can be used to test api-checker.
 *
 * <b>This is not the Repose api-validator filter.</b>
 */
class ValidatorFilter extends Filter {
  private[this] var validator : Validator = null;

  override def init(config : FilterConfig) : Unit = {
    validator = config.getServletContext().getAttribute(VALIDATOR_ATTRIB).asInstanceOf[Validator]
    if (validator == null) {
      throw new ServletException("Could not locate validator!")
    }
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
