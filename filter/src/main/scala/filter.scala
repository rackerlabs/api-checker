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
import com.rackspace.com.papi.components.checker.ValidatorException
import com.rackspace.com.papi.components.checker.handler._

class ValidatorFilter extends Filter {
  private[this] var validator : Validator = null;

  override def init(config : FilterConfig) : Unit = {
    val wadlRef = config.getInitParameter("WADLRef")

    if (wadlRef == null) {
      throw new ServletException ("Missing required init paramater WADLRef")
    }

    val dot : File = File.createTempFile("checker", ".dot")

    System.out.println ("Dot file is at: "+dot)

    val resultHandler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(),
                                                                      new SaveDotHandler(dot, true, true),
                                                                      new ServletResultHandler()))

    validator = Validator(new SAXSource(new InputSource(wadlRef)), resultHandler)
  }

  override def doFilter (req : ServletRequest, resp : ServletResponse, chain : FilterChain) : Unit = {
    try {
      validator.validate (req.asInstanceOf[HttpServletRequest], resp.asInstanceOf[HttpServletResponse], chain)
    } catch {
      case v : ValidatorException => throw new ServletException("Error while calling validator", v)
    }
  }

  override def destroy : Unit = {
    validator = null
  }
}
