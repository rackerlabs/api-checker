package com.rackspace.papi.components.checker.filter

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OkayServlet extends HttpServlet {
  override def service(req : HttpServletRequest, resp : HttpServletResponse) {
    resp.setStatus (200)
  }
}
