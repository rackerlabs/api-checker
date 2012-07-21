package com.rackspace.papi.components.checker.filter

import java.io.InputStream
import java.io.OutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

import javax.servlet.ServletException

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object OkayServlet {
  private val BUFFER_SIZE : Int = 1024
  private val ECHO_CONTENT : String = "echoContent"
}

import OkayServlet._

class OkayServlet extends HttpServlet {

  override def service(req : HttpServletRequest, resp : HttpServletResponse) {
    //
    //  If the parameter ECHO_CONTENT is set, then we echo the content
    //  on response.
    //
    if (req.getParameter(ECHO_CONTENT) != null) {
      try {
        copy(req.getInputStream(), resp.getOutputStream())
      }catch {
        case e : Exception => throw new ServletException ("Error while coyping data", e)
      }
    }

    resp.setStatus (200)
  }

  private def copy(in : InputStream, out : OutputStream) : Unit = {
    val buffer = new Array[Byte](BUFFER_SIZE)
    val bufIn  = new BufferedInputStream(in, BUFFER_SIZE)
    val bufOut = new BufferedOutputStream(out, BUFFER_SIZE)

    var n : Int = 0

    try {
      n = bufIn.read (buffer, 0, BUFFER_SIZE)
      while (n != -1) {
        out.write (buffer, 0, n)
        n = bufIn.read (buffer, 0, BUFFER_SIZE)
      }
    } finally {
      try {
        bufIn.close()
      } catch {
        case e : Exception => /* Ignore */
      }
      try {
        bufOut.close()
      } catch {
        case e : Exception => /* Ignore */
      }
    }
  }
}
