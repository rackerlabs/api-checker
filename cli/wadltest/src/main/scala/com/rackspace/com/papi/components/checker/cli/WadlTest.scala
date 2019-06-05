/** *
  * Copyright 2015 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker.cli

import java.io.File

import javax.xml.transform._
import javax.xml.transform.stream._
import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.util.URLResolver
import com.rackspace.com.papi.components.checker.{Config, Validator}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{FilterMapping, ServletContextHandler}

object WadlTest {
  val MAX_CONSOLE_WIDTH = 100
  val DEFAULT_NAME = "Test_Validator"
  val DEFAULT_PORT = 9191

  val title = getClass.getPackage.getImplementationTitle

  val implVersion = getClass.getPackage.getImplementationVersion


  def getSource(input: String): Source = {
    var source: Source = null
    if (input.isEmpty) {
      source = new StreamSource(System.in)
    } else {
      source = new StreamSource(URLResolver.toAbsoluteSystemId(input))
    }
    source
  }

  def handleArgs(args: Array[String]): Option[(Boolean, Boolean, Boolean, Boolean, Config, String, Int, String)] = {
    val c = new Config

    // Fix default values for fields with defaults that differ between the API and CLI Config
    c.removeDups = false

    var input: String = ""
    var outputMetadata: Boolean = false
    var showErrors: Boolean = false
    var nfaMode: Boolean = false
    var consoleLog: Boolean = false
    var port: Int = DEFAULT_PORT
    var name: String = DEFAULT_NAME

    val parser = new CheckerParser("wadltest", c) {
      head(s"$title v$implVersion")

      opt[Unit]('O', "output-metadata").text("Display checker metadata")
        .foreach(_ => outputMetadata = true)

      arg[String]("wadl").text("WADL file/uri to read.")
        .optional()
        .foreach(x => input = x)

      opt[Unit]('e', "show-errors").text("Show error nodes in the generated dot. Default: false")
        .foreach(_ => showErrors = true)

      opt[Unit]('n', "nfa-mode").text("Display the generated dot in NFA mode. Default: false")
        .foreach(_ => nfaMode = true)

      opt[Unit]('L', "console-log").text("Display request log in console. Default: false")
        .foreach(_ => consoleLog = true)

      opt[Int]('o', "portNumber").text(s"Port number. Default: $DEFAULT_PORT")
        .foreach(x => port = x)

      opt[String]('N', "name").text(s"The validator name. Default: $DEFAULT_NAME")
        .foreach(x => name = x)
    }

    if (parser.parse(args)) {
      Some((outputMetadata, showErrors, nfaMode, consoleLog, c, name, port, input))
    } else {
      //bad args
      None
    }
  }

  //
  //  Draw a silly console box, cus it's cool...
  //
  def drawBox(title: String, content: String) {
    val allText = title + "\n" + content
    val currMax = allText.split("\n").map(s => s.trim).map(s => s.length()).reduceLeft((x, y) => if (x >= y) x else y)

    def padString(s: String, pad: Int, padString: String = " "): String = {
      s.padTo(pad, padString).map(a => a.toString).foldRight("") { (a, b) => a + b }
    }

    println()
    println(" ╒" + padString("", currMax, "═") + "╕")
    println(" │ " + padString(title, currMax - 1) + "│")
    println(" ╞" + padString("", currMax, "═") + "╡")
    content.split("\n").map(s => s.trim()).foreach(s => {
      if (s.contains(Console.RESET)) {
        println(" │ " + padString(s, currMax + 7) + "│")
      } else {
        println(" │ " + padString(s, currMax - 1) + "│")
      }
    })
    println(" ╘" + padString("", currMax, "═") + "╛")
    println()
  }

  def runServer(name: String, port: Int, metaOut: Option[StreamResult], dot: File, config: Config, input: String): Unit = {
    val source = getSource(input)

    val sourceName = {
      if (source.asInstanceOf[StreamSource].getInputStream != null) {
        "<STDIN>"
      } else {
        source.asInstanceOf[StreamSource].getSystemId
      }
    }

    val echoParam = OkayServlet.ECHO_CONTENT_PARAM
    val respParam = OkayServlet.RESPONSE_TYPE

    //
    //  Initalize the validator, this catches errors early...
    //
    println(s"Loading $sourceName...\n")
    val validator = Validator(name, source, metaOut, config)


    //
    //  Initialize the server....
    //
    val server = new Server(port)
    val servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
    val servletHandler = servletContextHandler.getServletHandler

    servletContextHandler.setContextPath("/")
    servletContextHandler.setResourceBase(System.getProperty("java.io.tmpdir"))
    servletContextHandler.setAttribute(ValidatorFilter.VALIDATOR_ATTRIB, validator)

    server.setHandler(servletContextHandler)

    servletHandler.addServletWithMapping(classOf[OkayServlet], "/*")
    servletHandler.addFilterWithMapping(classOf[ValidatorFilter], "/*", FilterMapping.REQUEST)

    server.start()

    //
    //  Display a nice little text box with important info...
    //
    val B = Console.BOLD
    val R = Console.RESET

    drawBox(s"$title $implVersion",
      s"""
             Running validator $B$name$R

             Port: $B$port$R
             WADL Input: $B$sourceName$R
             Dot File: $B$dot$R

             The service should return a 200 response if the request
             validates against the WADL, it will return a 4xx code
             with an appropriate message otherwise.

             You can pass an '$B$echoParam$R' query paramater to the
             request to have the service echo the body of the request
             in the response.

             You can pass a '$B$respParam$R' query paramater to the
             request to set the ContentType of the response to the value
             of that parameter.
            """)

    //
    // Let the current thread join until the server is don executing...
    //
    server.join()
  }

  def main(args: Array[String]) = {
    handleArgs(args) match {
      case Some((outputMetadata, showErrors, nfaMode, consoleLog, c, name, port, input)) =>
        val metaOutResult = {
          if (outputMetadata) {
            Some(new StreamResult(System.err))
          } else {
            None
          }
        }

        val dot = File.createTempFile("chk", ".dot")
        dot.deleteOnExit()

        val handlerList = {
          val dotHandler = new SaveDotHandler(dot, !showErrors, nfaMode)

          val initList = List[ResultHandler](dotHandler,
            new ServletResultHandler(),
            new InstrumentedHandler())

          if (consoleLog) {
            initList :+ new ConsoleResultHandler()
          } else {
            initList
          }
        }

        c.resultHandler = new DispatchResultHandler(handlerList)

        runServer(name, port, metaOutResult, dot, c, input)
      case None =>
      //Failed to parse
    }

  }
}
