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
package com.rackspace.com.papi.components.checker.cli

import java.io.{BufferedInputStream, BufferedOutputStream, InputStream, OutputStream}
import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

object OkayServlet {
  private val BUFFER_SIZE : Int = 1024
  val ECHO_CONTENT_PARAM : String = "echoContent"
}

import com.rackspace.com.papi.components.checker.cli.OkayServlet._

class OkayServlet extends HttpServlet {

  override def service(req : HttpServletRequest, resp : HttpServletResponse) {
    //
    //  If the parameter ECHO_CONTENT_PARAM is set, then we echo the content
    //  on response.
    //
    if (req.getParameter(ECHO_CONTENT_PARAM) != null) {
      try {
        copy(req.getInputStream, resp.getOutputStream)
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
