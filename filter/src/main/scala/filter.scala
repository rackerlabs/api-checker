package com.rackspace.papi.components.checker.filter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ValidatorFilter extends Filter {
  override def init(config : FilterConfig) : Unit = {}
  override def doFilter (req : ServletRequest, resp : ServletResponse, chain : FilterChain) : Unit = {}
  override def destroy : Unit = {}
}
